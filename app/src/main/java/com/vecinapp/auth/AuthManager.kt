package com.vecinapp.auth

import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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
    fun getGoogleSignInRequest(): BeginSignInRequest {
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
}