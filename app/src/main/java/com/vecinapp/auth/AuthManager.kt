package com.vecinapp.auth

import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit

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
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
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
    // Add these methods to your AuthManager.kt file

    /**
     * User profile data class
     */
    data class UserProfile(
        val displayName: String? = null,
        val photoUrl: Uri? = null,
        val age: Int? = null,
        val location: String? = null,
        // Add other profile fields as needed
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
                    userProfile.location != null
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
                val isComplete = userDoc.getBoolean("isProfileComplete") ?: false

                UserProfile(
                    displayName = authUser?.displayName,
                    photoUrl = authUser?.photoUrl?.let { Uri.parse(it.toString()) },
                    age = age,
                    location = location,
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
}