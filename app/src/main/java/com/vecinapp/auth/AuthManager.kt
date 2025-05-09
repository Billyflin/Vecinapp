// file: com/vecinapp/auth/AuthManager.kt
package com.vecinapp.auth

import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.core.net.toUri
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "AuthManager"

/* ------------------------------------------------------------------------- */
/*  Serializador para que KotlinX + Firestore manejen Uri de forma transparente */
/* ------------------------------------------------------------------------- */
object UriSerializer : KSerializer<Uri> {
    override val descriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Uri) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Uri = decoder.decodeString().toUri()
}


// antes
private val json = Json {
    ignoreUnknownKeys = true      // ← ¡importante!
    coerceInputValues = true
    encodeDefaults = true
}

/* ------------------------------------------------------------------------- */
/*                       Modelo persistente de perfil                         */
/* ------------------------------------------------------------------------- */
@Serializable
data class UserProfile(
    val displayName: String? = null,
    @Serializable(with = UriSerializer::class) val photoUrl: Uri? = null,
    val age: Int? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isProfileComplete: Boolean = false,
    val isSenior: Boolean = false,
    val communities: List<String> = emptyList(),
    val notificationTokens: List<String> = emptyList()
)


/* ----------------------------- helpers Json <-> Map ---------------------- */
private fun JsonPrimitive.asAny(): Any = when {
    isString -> content
    booleanOrNull != null -> boolean
    intOrNull != null -> int
    longOrNull != null -> long
    doubleOrNull != null -> double
    else -> content
}

private fun JsonElement.toMap(): Any? = when (this) {
    JsonNull -> null
    is JsonPrimitive -> asAny()
    is JsonArray -> map { it.toMap() }
    is JsonObject -> mapValues { (_, v) -> v.toMap() }
}

private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Map<*, *> -> JsonObject(entries.associate { (k, v) -> k.toString() to v.toJsonElement() })
    is Iterable<*> -> JsonArray(map { it.toJsonElement() })
    is Number, is Boolean -> JsonPrimitive(this.toString())
    else -> JsonPrimitive(toString())
}

/* ------------------------------- HTTP cliente ---------------------------- */
private val http by lazy {
    OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
}

/* ------------------------------------------------------------------------- */
/*                                MANAGER                                    */
/* ------------------------------------------------------------------------- */
class AuthManager(private val ctx: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val oneTap = Identity.getSignInClient(ctx)
    private val fs = Firebase.firestore
    private val storage = Firebase.storage

    /* --------- flujo de usuario ---------------- */
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(l); awaitClose { auth.removeAuthStateListener(l) }
    }

    /* --------- flujo de perfil ----------------- */
    fun profile(uid: String): Flow<UserProfile?> = callbackFlow {
        val l = fs.collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                val profile = snap?.data?.let { data ->
                    json.decodeFromJsonElement<UserProfile>(
                        JsonObject(data.mapValues { (_, v) -> v.toJsonElement() })
                    )
                }
                trySend(profile)
            }
        awaitClose { l.remove() }
    }

    /* ----------------- Google One-Tap ---------- */
    private fun googleReq(): BeginSignInRequest =
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(ctx.getString(com.vecinapp.R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

    suspend fun getGoogleOneTapIntent(): Result<IntentSenderRequest> = runCatching {
        val res = oneTap.beginSignIn(googleReq()).await()
        IntentSenderRequest.Builder(res.pendingIntent.intentSender).build()
    }

    /* ----------------- Auth -------------------- */
    suspend fun firebaseAuthWithGoogle(idToken: String) = runCatching {
        val cred = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(cred).await().user!!
    }

    suspend fun signInWithCredential(c: AuthCredential) =
        runCatching { auth.signInWithCredential(c).await().user!! }

    suspend fun signInAnonymously() = runCatching { auth.signInAnonymously().await().user!! }

    fun signOut() = auth.signOut()
    fun isPhoneLinked() = auth.currentUser?.phoneNumber != null

    /* ----------------- Perfil (1-shot) --------- */
    suspend fun getUserProfile(uid: String): UserProfile = fs.collection("users")
        .document(uid).get().await().let { snap ->
            if (!snap.exists()) UserProfile()
            else json.decodeFromJsonElement(
                JsonObject(snap.data!!.mapValues { it.value.toJsonElement() })
            )
        }
    /* ---------------------------------------------------------------- */
    /* ------------  funcionalidades específicas de Phone OTP ----------*/
    /* ---------------------------------------------------------------- */

    fun startPhoneVerification(
        phoneNumber: String,
        activity: Context,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity as Activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken,
        activity: Context,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity as Activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Convierte el par verification-ID + code en un PhoneAuthCredential
     * para luego autenticar o vincular la cuenta.
     */
    suspend fun verifyPhoneNumberWithCode(
        verificationId: String,
        code: String
    ): Result<PhoneAuthCredential> = runCatching {
        PhoneAuthProvider.getCredential(verificationId, code)
    }

    /* ----------------- Perfil (update) --------- */
    suspend fun updateUserProfile(
        userId: String,
        displayName: String? = null,
        photoUri: Uri? = null,
        age: Int? = null,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        isProfileComplete: Boolean = false,
        isSenior: Boolean = false
    ): Result<UserProfile> = runCatching {
        val u = auth.currentUser ?: error("No user signed in")

        /* 1. Auth profile */
        if (displayName != null || photoUri != null) {
            u.updateProfile(
                UserProfileChangeRequest.Builder().apply {
                    displayName?.let(::setDisplayName)
                    photoUri?.let(::setPhotoUri)
                }.build()
            ).await()
        }

        /* 2. Firestore */
        val map = mutableMapOf<String, Any?>(
            "isProfileComplete" to isProfileComplete,
            "isSenior" to isSenior,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        age?.let { map["age"] = it }
        location?.let { map["location"] = it }
        latitude?.let { map["latitude"] = it }
        longitude?.let { map["longitude"] = it }
        photoUri?.let { map["photoUrl"] = it.toString() }

        fs.collection("users").document(userId).set(map, SetOptions.merge()).await()

        /* 3. DTO resultante */
        UserProfile(
            displayName = u.displayName,
            photoUrl = photoUri,
            age = age,
            location = location,
            latitude = latitude,
            longitude = longitude,
            isProfileComplete = isProfileComplete,
            isSenior = isSenior
        )
    }


    /* ▼▼▼  OVERLOAD “patch” – la cómoda –  ▼▼▼ */
    suspend fun updateUserProfile(
        displayName: String? = null,
        photoUri: Uri? = null,
        patch: UserProfile
    ): Result<UserProfile> = runCatching {

        val u = auth.currentUser ?: error("No user")
        val id = u.uid

        /* 1) Auth (solo si cambian nombre o foto) */
        if (displayName != null || photoUri != null) {
            u.updateProfile(
                UserProfileChangeRequest.Builder().apply {
                    displayName?.let(::setDisplayName)
                    photoUri?.let(::setPhotoUri)
                }.build()
            ).await()
        }

        /* 2) Firestore → todo el objeto (merge)  */
        json.encodeToJsonElement(patch).toMap()?.let {
            fs.collection("users").document(id)
                .set(it, SetOptions.merge())
                .await()
        }

        /* 3) retorno */
        patch.copy(
            displayName = displayName ?: patch.displayName,
            photoUrl = photoUri ?: patch.photoUrl
        )
    }


    /* ------- Subida de foto de perfil -------- */
    suspend fun uploadProfilePhoto(local: Uri): Result<Uri> = runCatching {
        val uid = auth.uid ?: error("No user")
        val ref = storage.reference.child("profile_photos/$uid/${UUID.randomUUID()}")
        ref.putFile(local).await()
        ref.downloadUrl.await()
    }

    /* ---------- geocodificación --------------- */
    suspend fun getCityFromLocation(lat: Double, lon: Double): String = runCatching {
        Geocoder(ctx, Locale.getDefault())
            .getFromLocation(lat, lon, 1)
            ?.firstOrNull()?.locality
    }.getOrNull()?.takeIf { it.isNotBlank() }
        ?: reverseGeocode(lat, lon)
        ?: approximate(lat, lon)

    /* ---------- helpers privados -------------- */
    private suspend fun reverseGeocode(lat: Double, lon: Double): String? = runCatching {
        val url =
            "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=10"
        val resp = http.newCall(
            Request.Builder().url(url).header("User-Agent", "VecinApp Android").build()
        ).execute()
        if (!resp.isSuccessful) return null
        val address = JSONObject(resp.body!!.string()).optJSONObject("address") ?: return null
        listOf("city", "town", "village", "municipality", "county", "state")
            .firstNotNullOfOrNull { key ->
                address.optString(key).takeIf { it.isNotBlank() }
            }
    }.getOrNull()

    private fun approximate(lat: Double, lon: Double): String {
        val cities = mapOf(
            "Santiago" to (-33.4489 to -70.6693),
            "Concepción" to (-36.8201 to -73.0440),
            "Valparaíso" to (-33.0472 to -71.6127),
            "Temuco" to (-38.7359 to -72.5904)
        )
        return cities.minBy { (_, c) -> haversine(lat, lon, c.first, c.second) }.key
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        return 2 * 6371 * asin(sqrt(a))
    }

    /* ---------- helper público --------------- */
    suspend fun isProfileComplete(uid: String): Boolean = runCatching {
        getUserProfile(uid).isProfileComplete
    }.getOrDefault(false)
}
