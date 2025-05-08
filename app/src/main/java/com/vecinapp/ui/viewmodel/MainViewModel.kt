// ui/viewmodel/MainViewModel.kt
package com.vecinapp.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.vecinapp.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage

) : ViewModel() {

    data class AuthState(
        val isLoggedIn: Boolean = false,
        val user: User? = null,
        val error: String? = null
    )

    data class PhoneState(
        val codeSent: Boolean = false,
        val verificationId: String? = null,
        val error: String? = null
    )

    private val users = db.collection("users")

    private val _auth = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _auth.asStateFlow()

    private val _phone = MutableStateFlow(PhoneState())
    val phoneState: StateFlow<PhoneState> = _phone.asStateFlow()

    fun signInGoogle(idToken: String) = viewModelScope.launch {
        runCatching {
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
            ensureUser()
        }.onFailure { _auth.value = _auth.value.copy(error = it.message) }
    }

    fun startPhone(phone: String, activity: Context) {
        val cb = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(cred: PhoneAuthCredential) {
                signInCredential(cred)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _phone.value = _phone.value.copy(error = e.message)
            }

            override fun onCodeSent(id: String, t: PhoneAuthProvider.ForceResendingToken) {
                _phone.value = PhoneState(codeSent = true, verificationId = id)
            }
        }
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60, TimeUnit.SECONDS)
                .setActivity(activity as Activity)
                .setCallbacks(cb)
                .build()
        )
    }

    fun verifyCode(id: String, code: String) =
        signInCredential(PhoneAuthProvider.getCredential(id, code))

    private fun signInCredential(cred: PhoneAuthCredential) = viewModelScope.launch {
        runCatching {
            auth.signInWithCredential(cred).await()
            _phone.value = PhoneState()
            ensureUser()
        }.onFailure { _phone.value = _phone.value.copy(error = it.message) }
    }

    private suspend fun ensureUser() {
        val f = auth.currentUser ?: return
        Log.e("User", f.uid)
        val doc = users.document(f.uid).get().await()
        val base = User(
            id = f.uid,
            email = f.email.orEmpty(),
            name = f.displayName.orEmpty(),
            phone = f.phoneNumber,
            photoUrl = f.photoUrl?.toString()
        )
        val u = if (doc.exists())
            doc.toObject(User::class.java)!!.copy(
                email = base.email,
                name = base.name,
                phone = base.phone,
                photoUrl = base.photoUrl
            ).also { users.document(f.uid).set(it, SetOptions.merge()) }
        else {
            val n = base.copy(createdAt = Timestamp.now())
            users.document(f.uid).set(n).await(); n
        }
        _auth.value = AuthState(true, u, null)
    }

    fun updateUserProfile(
        userId: String,
        displayName: String,
        photoUri: Uri? = null,
        age: Int = 0,
        isComplete: Boolean = false,
        latitude: Double? = null,
        longitude: Double? = null
    ) = viewModelScope.launch {
        try {
            // 1-a. Subir foto si viene un Uri
            var photoUrl: String? = null
            if (photoUri != null) {
                val ref = storage.reference.child("profiles/$userId.jpg")
                ref.putFile(photoUri).await()
                photoUrl = ref.downloadUrl.await().toString()
            }

            // 1-b. Actualizar displayName / photo en FirebaseAuth
            if (auth.currentUser?.uid == userId) {
                val req = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(photoUrl?.toUri())
                    .build()
                auth.currentUser!!.updateProfile(req).await()
            }

            // 2. Actualizar documento en Firestore
            val updates = mapOf(
                "name" to displayName,
                "photoUrl" to (photoUrl ?: auth.currentUser?.photoUrl?.toString()),
                "isProfileComplete" to isComplete,
                "latitude" to latitude,
                "longitude" to longitude,
                // opcional: sobrescribe edad dentro de un sub-campo
                "age" to age
            )
            db.collection("users").document(userId).set(updates, SetOptions.merge()).await()

            // 3. Refrescar estado local
            _auth.value = _auth.value.copy(
                user = _auth.value.user?.copy(
                    name = displayName,
                    photoUrl = photoUrl ?: _auth.value.user?.photoUrl,
                    isProfileComplete = isComplete,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        } catch (e: Exception) {
            _auth.value = _auth.value.copy(error = e.message)
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _auth.value = AuthState(isLoggedIn = false, user = null, error = null)
        }
    }
}