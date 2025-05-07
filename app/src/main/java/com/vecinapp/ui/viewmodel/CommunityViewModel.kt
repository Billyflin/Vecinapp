package com.vecinapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vecinapp.community.CommunityManager
import com.vecinapp.domain.model.Community
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityManager: CommunityManager
) : ViewModel() {

    // Comunidades del usuario
    val userCommunities = communityManager.userCommunities

    // Estados de carga y error
    val isLoadingCommunities = communityManager.isLoadingCommunities

    // Estado de creación de comunidad
    val isCreatingCommunity = communityManager.isCreatingCommunity
    val communityCreationError = communityManager.communityCreationError

    // Estado de unirse a comunidad
    val isJoiningCommunity = communityManager.isJoiningCommunity
    val communityJoinError = communityManager.communityJoinError

    // Estado de búsqueda de comunidades
    private val _searchResults = MutableStateFlow<List<Community>>(emptyList())
    val searchResults: StateFlow<List<Community>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    init {
        // Cargar comunidades del usuario al inicializar el ViewModel
        fetchUserCommunities()
    }

    /**
     * Obtiene las comunidades del usuario actual
     */
    fun fetchUserCommunities() {
        communityManager.fetchUserCommunities()
    }

    /**
     * Crea una nueva comunidad
     */
    fun createCommunity(community: Community, onResult: (Result<Community>) -> Unit = {}) {
        viewModelScope.launch {
            val result = communityManager.createCommunity(community)
            onResult(result)
        }
    }

    /**
     * Une al usuario a una comunidad existente
     */
    fun joinCommunity(community: Community, onResult: (Result<Community>) -> Unit = {}) {
        viewModelScope.launch {
            val result = communityManager.joinCommunity(community)
            onResult(result)
        }
    }

    /**
     * Busca comunidades por nombre o dirección
     */
    fun searchCommunities(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null

            try {
                val result = communityManager.searchCommunities(query)
                result.onSuccess { communities ->
                    _searchResults.value = communities
                }.onFailure { error ->
                    _searchError.value = "Error en la búsqueda: ${error.message}"
                }
            } catch (e: Exception) {
                _searchError.value = "Error en la búsqueda: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * Inicia el escáner de códigos QR
     */
    fun startQrCodeScanner() {
        communityManager.startQrCodeScanner()
    }
}