package com.vecinapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.vecinapp.data.preferences.UserPreferencesManager
import com.vecinapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    // Estado combinado de la UI
    data class UiState(
        val user: FirebaseUser? = null,
        val isLoading: Boolean = true,
        val darkMode: Boolean = false,
        val dynamicColor: Boolean = true,
        val isSenior: Boolean = false,
        val isFirstTime: Boolean = true
    )

    // Combinamos los flujos de autenticación y preferencias en un único estado de UI
    val uiState: StateFlow<UiState> = combine(
        authRepository.currentUser,
        userPreferencesManager.darkModeFlow,
        userPreferencesManager.dynamicColorsFlow,
        userPreferencesManager.seniorModeFlow,
        flow { emit(true) } // Placeholder para isFirstTime, reemplazar con la fuente real
    ) { user, darkMode, dynamicColor, isSenior, isFirstTime ->
        UiState(
            user = user,
            isLoading = false,
            darkMode = darkMode,
            dynamicColor = dynamicColor,
            isSenior = isSenior,
            isFirstTime = isFirstTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(isLoading = true)
    )

    // Acciones para actualizar preferencias
    suspend fun updateDarkMode(enabled: Boolean) {
        userPreferencesManager.setDarkMode(enabled)
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        userPreferencesManager.setDynamicColors(enabled)
    }

    suspend fun updateIsSenior(enabled: Boolean) {
        userPreferencesManager.setSeniorMode(enabled)
    }

    suspend fun updateIsFirstTime(isFirstTime: Boolean) {
        // Implementar cuando tengas la fuente real
    }

    // Acciones de autenticación
    fun signOut() {
        authRepository.signOut()
    }
}