package com.vecinapp.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.vecinapp.auth.AuthManager
import com.vecinapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Auth state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Phone verification state
    private val _phoneVerificationState = MutableStateFlow<PhoneVerificationState>(PhoneVerificationState.Initial)
    val phoneVerificationState: StateFlow<PhoneVerificationState> = _phoneVerificationState.asStateFlow()

    // Current user
    val currentUser = authRepository.currentUser

    // Profile state
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    // ... otros métodos existentes ...

    /**
     * Verify phone number with code
     */
    fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val credentialResult = authRepository.verifyPhoneNumberWithCode(verificationId, code)

            credentialResult.fold(
                onSuccess = { credential ->
                    signInWithPhoneCredential(credential)
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(e.message ?: "Verification failed")
                }
            )
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {

    }

    /**
     * Start phone verification process
     */
    fun startPhoneVerification(phoneNumber: String, activity: Context) {
        _phoneVerificationState.value = PhoneVerificationState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification completed (rare on most devices)
                viewModelScope.launch {
                    signInWithPhoneCredential(credential)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _phoneVerificationState.value = PhoneVerificationState.Error(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _phoneVerificationState.value = PhoneVerificationState.CodeSent(verificationId, token)
            }
        }

        authRepository.startPhoneVerification(phoneNumber, activity, callbacks)
    }

    /**
     * Resend verification code
     */
    fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken,
        activity: Context
    ) {
        _phoneVerificationState.value = PhoneVerificationState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification completed (rare on most devices)
                viewModelScope.launch {
                    signInWithPhoneCredential(credential)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _phoneVerificationState.value = PhoneVerificationState.Error(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _phoneVerificationState.value = PhoneVerificationState.CodeSent(verificationId, token)
            }
        }

        authRepository.resendVerificationCode(phoneNumber, token, activity, callbacks)
    }

    // Auth state sealed class
    sealed class AuthState {
        object Initial : AuthState()
        object Loading : AuthState()
        data class Success(val user: FirebaseUser) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // Phone verification state sealed class
    sealed class PhoneVerificationState {
        object Initial : PhoneVerificationState()
        object Loading : PhoneVerificationState()
        data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) : PhoneVerificationState()
        data class Error(val message: String) : PhoneVerificationState()
    }

    // Profile state sealed class
    sealed class ProfileState {
        object Initial : ProfileState()
        object Loading : ProfileState()
        object Incomplete : ProfileState()
        data class Complete(val profile: AuthRepository.UserProfile) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }
}