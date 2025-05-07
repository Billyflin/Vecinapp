package com.vecinapp.auth

import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.activity.result.IntentSenderRequest
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Centralized manager for all authentication operations
 */
class AuthManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val oneTapClient = Identity.getSignInClient(context)

    /**
     * Get current user as a flow
     */
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Get current user synchronously
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Add auth state listener
     */
    fun addAuthStateListener(listener: (FirebaseUser?) -> Unit): FirebaseAuth.AuthStateListener {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            listener(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        return authStateListener
    }

    /**
     * Remove auth state listener
     */
    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }

    /**
     * Get Google Sign-In request configuration
     */
    private fun getGoogleSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(com.vecinapp.R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    /**
     * Begin Google Sign-In process
     */
    fun beginGoogleSignIn(
        onSuccess: (IntentSenderRequest) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        oneTapClient.beginSignIn(getGoogleSignInRequest())
            .addOnSuccessListener { result ->
                val intentSenderRequest =
                    IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                onSuccess(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "One-Tap failed: ${e.localizedMessage}")
                onFailure(e)
            }
    }

    /**
     * Authenticate with Google ID token
     */
    suspend fun firebaseAuthWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e(TAG, "GoogleAuth error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Start phone verification process
     */
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

    /**
     * Resend verification code
     */
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
     * Verify phone number with code
     */
    suspend fun verifyPhoneNumberWithCode(
        verificationId: String,
        code: String
    ): Result<PhoneAuthCredential> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            Result.success(credential)
        } catch (e: Exception) {
            Log.e(TAG, "Verification code error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Link phone number to existing account
     */
    suspend fun linkPhoneNumberToAccount(
        credential: PhoneAuthCredential
    ): Result<FirebaseUser> {
        val currentUser = auth.currentUser

        return if (currentUser != null) {
            try {
                val result = currentUser.linkWithCredential(credential).await()
                Result.success(result.user!!)
            } catch (e: Exception) {
                Log.e(TAG, "Error linking phone: ${e.message}")
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("No user is currently signed in"))
        }
    }

    /**
     * Sign in anonymously
     */
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous auth error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sign out
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Check if user has phone linked
     */
    fun isPhoneLinked(): Boolean {
        return auth.currentUser?.phoneNumber != null
    }


    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in with credential error: ${e.message}")
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AuthManager"
    }

    /**
     * User profile data class
     */
    data class UserProfile(
        val displayName: String? = null,
        val photoUrl: Uri? = null,
        val age: Int? = null,
        val location: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val isProfileComplete: Boolean = false
    )

    /**
     * Check if user profile is complete
     * Returns true if the user has all required profile data
     */
    suspend fun isProfileComplete(userId: String): Boolean {
        try {
            // Get user profile data from Firestore
            val userProfile = getUserProfile(userId)

            // Define what constitutes a "complete" profile
            // For example, we require displayName, age, and location
            return userProfile.displayName != null &&
                    userProfile.age != null &&
                    userProfile.location != null &&
                    userProfile.latitude != null &&
                    userProfile.longitude != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking profile completeness: ${e.message}")
            return false
        }
    }

    /**
     * Get user profile data from Firestore
     */
    suspend fun getUserProfile(userId: String): UserProfile {
        return try {
            // Get user document from Firestore
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            // Get Firebase Auth user for basic profile info
            val authUser = getCurrentUser()

            if (userDoc.exists()) {
                // User has a profile document in Firestore
                val age = userDoc.getLong("age")?.toInt()
                val location = userDoc.getString("location")
                val latitude = userDoc.getDouble("latitude")
                val longitude = userDoc.getDouble("longitude")
                val isComplete = userDoc.getBoolean("isProfileComplete") ?: false

                UserProfile(
                    displayName = authUser?.displayName,
                    photoUrl = authUser?.photoUrl?.let { Uri.parse(it.toString()) },
                    age = age,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    isProfileComplete = isComplete
                )
            } else {
                // User doesn't have a profile document yet
                UserProfile(
                    displayName = authUser?.displayName,
                    photoUrl = authUser?.photoUrl?.let { Uri.parse(it.toString()) },
                    isProfileComplete = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile: ${e.message}")
            UserProfile(isProfileComplete = false)
        }
    }

    /**
     * Update user profile data
     */
    suspend fun updateUserProfile(
        userId: String,
        displayName: String? = null,
        photoUri: Uri? = null,
        age: Int? = null,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        isComplete: Boolean = false
    ): Result<UserProfile> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No user is currently signed in")

            // Update Firebase Auth profile if name or photo changed
            if (displayName != null || photoUri != null) {
                val profileUpdates = UserProfileChangeRequest.Builder().apply {
                    displayName?.let { setDisplayName(it) }
                    photoUri?.let { setPhotoUri(it) }
                }.build()

                user.updateProfile(profileUpdates).await()
            }

            // Prepare data for Firestore
            val profileData = mutableMapOf<String, Any>()
            age?.let { profileData["age"] = it }
            location?.let { profileData["location"] = it }
            latitude?.let { profileData["latitude"] = it }
            longitude?.let { profileData["longitude"] = it }
            profileData["isProfileComplete"] = isComplete
            profileData["updatedAt"] = FieldValue.serverTimestamp()

            // Update Firestore document
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(profileData, SetOptions.merge())
                .await()

            // Return updated profile
            val updatedProfile = UserProfile(
                displayName = user.displayName,
                photoUrl = user.photoUrl?.let { Uri.parse(it.toString()) },
                age = age,
                location = location,
                latitude = latitude,
                longitude = longitude,
                isProfileComplete = isComplete
            )

            Result.success(updatedProfile)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Upload profile photo and return the download URL
     */
    suspend fun uploadProfilePhoto(photoUri: Uri): Result<Uri> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No user is currently signed in")
            val storageRef = FirebaseStorage.getInstance().reference
            val photoRef = storageRef.child("profile_photos/${user.uid}/${UUID.randomUUID()}")

            val uploadTask = photoRef.putFile(photoUri).await()
            val downloadUrl = photoRef.downloadUrl.await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile photo: ${e.message}")
            Result.failure(e)
        }
    }
    /**
     * Get only the city name from latitude and longitude
     * Makes multiple attempts to get a valid city name
     */
    suspend fun getCityFromLocation(latitude: Double, longitude: Double): Result<String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())

            // For Android 13+ (API 33+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                var cityResult = ""
                val latch = CountDownLatch(1)

                geocoder.getFromLocation(latitude, longitude, 5) { addresses ->
                    if (addresses.isNotEmpty()) {
                        // Try multiple addresses to find one with a valid locality
                        for (address in addresses) {
                            // First priority: locality (city)
                            if (!address.locality.isNullOrEmpty()) {
                                cityResult = address.locality
                                break
                            }

                            // Second priority: subAdminArea (county/district)
                            if (!address.subAdminArea.isNullOrEmpty()) {
                                cityResult = address.subAdminArea
                                // Don't break, keep looking for a locality
                            }

                            // Third priority: adminArea (state/province)
                            if (cityResult.isEmpty() && !address.adminArea.isNullOrEmpty()) {
                                cityResult = address.adminArea
                            }
                        }

                        // If still no result, try to parse from address line
                        if (cityResult.isEmpty() && addresses[0].maxAddressLineIndex >= 0) {
                            val addressLine = addresses[0].getAddressLine(0)
                            // Try to extract city from address line
                            val parts = addressLine.split(',')
                            if (parts.size >= 2) {
                                // Usually the city is the second component in the address
                                cityResult = parts[1].trim()
                            } else if (parts.isNotEmpty()) {
                                cityResult = parts[0].trim()
                            }
                        }
                    }
                    latch.countDown()
                }

                // Wait for the geocoder callback to complete (with timeout)
                latch.await(2, TimeUnit.SECONDS)

                if (cityResult.isNotEmpty()) {
                    Result.success(cityResult)
                } else {
                    // If geocoder failed, try reverse geocoding with a different approach
                    val result = reverseGeocodeFallback(latitude, longitude)
                    if (result.isNotEmpty()) {
                        Result.success(result)
                    } else {
                        // Last resort: use a geographic region approximation
                        Result.success(approximateLocationToCity(latitude, longitude))
                    }
                }
            } else {
                // For older Android versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 5)

                if (addresses != null && addresses.isNotEmpty()) {
                    var cityResult = ""

                    // Try multiple addresses to find one with a valid locality
                    for (address in addresses) {
                        // First priority: locality (city)
                        if (!address.locality.isNullOrEmpty()) {
                            cityResult = address.locality
                            break
                        }

                        // Second priority: subAdminArea (county/district)
                        if (!address.subAdminArea.isNullOrEmpty()) {
                            cityResult = address.subAdminArea
                            // Don't break, keep looking for a locality
                        }

                        // Third priority: adminArea (state/province)
                        if (cityResult.isEmpty() && !address.adminArea.isNullOrEmpty()) {
                            cityResult = address.adminArea
                        }
                    }

                    // If still no result, try to parse from address line
                    if (cityResult.isEmpty() && addresses[0].maxAddressLineIndex >= 0) {
                        val addressLine = addresses[0].getAddressLine(0)
                        // Try to extract city from address line
                        val parts = addressLine.split(',')
                        if (parts.size >= 2) {
                            // Usually the city is the second component in the address
                            cityResult = parts[1].trim()
                        } else if (parts.isNotEmpty()) {
                            cityResult = parts[0].trim()
                        }
                    }

                    if (cityResult.isNotEmpty()) {
                        Result.success(cityResult)
                    } else {
                        // If geocoder failed, try reverse geocoding with a different approach
                        val result = reverseGeocodeFallback(latitude, longitude)
                        if (result.isNotEmpty()) {
                            Result.success(result)
                        } else {
                            // Last resort: use a geographic region approximation
                            Result.success(approximateLocationToCity(latitude, longitude))
                        }
                    }
                } else {
                    // If geocoder returned no addresses, try fallback methods
                    val result = reverseGeocodeFallback(latitude, longitude)
                    if (result.isNotEmpty()) {
                        Result.success(result)
                    } else {
                        // Last resort: use a geographic region approximation
                        Result.success(approximateLocationToCity(latitude, longitude))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting city from location: ${e.message}")
            // Try fallback methods even on exception
            try {
                val result = reverseGeocodeFallback(latitude, longitude)
                if (result.isNotEmpty()) {
                    Result.success(result)
                } else {
                    // Last resort: use a geographic region approximation
                    Result.success(approximateLocationToCity(latitude, longitude))
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Error in fallback geocoding: ${e2.message}")
                Result.success(approximateLocationToCity(latitude, longitude))
            }
        }
    }

    /**
     * Fallback method to get city name using a network request to a geocoding service
     * This is used when the Android Geocoder fails
     */
    private suspend fun reverseGeocodeFallback(latitude: Double, longitude: Double): String {
        return try {
            // Use a simple HTTP request to OpenStreetMap Nominatim API
            // Note: In a production app, you should use a proper geocoding service with an API key
            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=10"

            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "VecinApp Android Client")
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonString = response.body?.string() ?: ""
                    val jsonObject = JSONObject(jsonString)

                    // Try to extract city from the response
                    val address = jsonObject.optJSONObject("address")
                    if (address != null) {
                        // Try different fields that might contain the city name
                        when {
                            address.has("city") -> address.getString("city")
                            address.has("town") -> address.getString("town")
                            address.has("village") -> address.getString("village")
                            address.has("municipality") -> address.getString("municipality")
                            address.has("county") -> address.getString("county")
                            address.has("state") -> address.getString("state")
                            else -> ""
                        }
                    } else {
                        ""
                    }
                } else {
                    ""
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in reverseGeocodeFallback: ${e.message}")
            ""
        }
    }

    /**
     * Approximate a location to a city based on geographic regions
     * This is a last resort when all geocoding methods fail
     */
    private fun approximateLocationToCity(latitude: Double, longitude: Double): String {
        // Define some major cities with their approximate coordinates
        val cities = listOf(
            Pair("Santiago", Pair(-33.4489, -70.6693)),
            Pair("Temuco", Pair(-38.7359, -72.5904)),
            Pair("Concepción", Pair(-36.8201, -73.0440)),
            Pair("Valparaíso", Pair(-33.0472, -71.6127)),
            Pair("Antofagasta", Pair(-23.6509, -70.3975)),
            Pair("La Serena", Pair(-29.9027, -71.2525)),
            Pair("Puerto Montt", Pair(-41.4693, -72.9424)),
            Pair("Arica", Pair(-18.4783, -70.3126)),
            Pair("Iquique", Pair(-20.2208, -70.1431)),
            Pair("Rancagua", Pair(-34.1708, -70.7444)),
            Pair("Talca", Pair(-35.4264, -71.6553)),
            Pair("Chillán", Pair(-36.6064, -72.1034)),
            Pair("Calama", Pair(-22.4524, -68.9204)),
            Pair("Osorno", Pair(-40.5714, -73.1392)),
            Pair("Valdivia", Pair(-39.8142, -73.2459))
        )

        // Find the closest city based on distance
        var closestCity = "Santiago" // Default to Santiago if no match
        var minDistance = Double.MAX_VALUE

        for ((cityName, coords) in cities) {
            val cityLat = coords.first
            val cityLon = coords.second

            // Calculate distance using Haversine formula
            val distance = calculateDistance(latitude, longitude, cityLat, cityLon)

            if (distance < minDistance) {
                minDistance = distance
                closestCity = cityName
            }
        }

        return closestCity
    }

    /**
     * Calculate distance between two points using Haversine formula
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

}