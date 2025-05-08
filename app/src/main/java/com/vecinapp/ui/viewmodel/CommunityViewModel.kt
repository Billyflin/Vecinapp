package com.vecinapp.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.vecinapp.domain.model.Announcement
import com.vecinapp.domain.model.Community
import com.vecinapp.domain.model.Event
import com.vecinapp.domain.model.MembershipRequest
import com.vecinapp.domain.model.NotificationSubscription
import com.vecinapp.domain.model.Proposal
import com.vecinapp.domain.model.Report
import com.vecinapp.domain.model.Role
import com.vecinapp.domain.model.RsvpStatus
import com.vecinapp.domain.model.User
import com.vecinapp.domain.service.ICommunityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityService: ICommunityService
) : ViewModel() {

    // Estados observables desde el servicio
    val isLoading = communityService.isLoading
    val errorMessage = communityService.errorMessage
    val currentUser = communityService.currentUser
    val userCommunities = communityService.userCommunities

    // Estados UI para diferentes secciones

    // Estado de autenticación
    data class AuthState(
        val isLoggedIn: Boolean = false,
        val isRegistering: Boolean = false,
        val loginError: String? = null,
        val registerError: String? = null,
        val user: User? = null
    )

    data class PreferencesState(
        val darkMode: Boolean = false,
        val dynamicColors: Boolean = true,
        val fontSize: String = "large",
        val seniorMode: Boolean = false
    )

    private val _preferencesState = MutableStateFlow(PreferencesState())
    val preferencesState: StateFlow<PreferencesState> = _preferencesState.asStateFlow()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Estado de comunidad
    data class CommunityState(
        val selectedCommunity: Community? = null,
        val searchResults: List<Community> = emptyList(),
        val isSearching: Boolean = false,
        val searchError: String? = null,
        val inviteCode: String? = null,
        val membershipRequests: List<MembershipRequest> = emptyList(),
        val userRole: Role = Role.MEMBER
    )

    private val _communityState = MutableStateFlow(CommunityState())
    val communityState: StateFlow<CommunityState> = _communityState.asStateFlow()

    // Estado de anuncios
    data class AnnouncementState(
        val announcements: List<Announcement> = emptyList(),
        val selectedAnnouncement: Announcement? = null,
        val isCreatingAnnouncement: Boolean = false,
        val announcementError: String? = null
    )

    private val _announcementState = MutableStateFlow(AnnouncementState())
    val announcementState: StateFlow<AnnouncementState> = _announcementState.asStateFlow()

    // Estado de eventos
    data class EventState(
        val events: List<Event> = emptyList(),
        val selectedEvent: Event? = null,
        val isCreatingEvent: Boolean = false,
        val eventError: String? = null,
        val userRsvpStatus: RsvpStatus = RsvpStatus.UNDECIDED
    )

    private val _eventState = MutableStateFlow(EventState())
    val eventState: StateFlow<EventState> = _eventState.asStateFlow()

    // Estado de propuestas
    data class ProposalState(
        val proposals: List<Proposal> = emptyList(),
        val selectedProposal: Proposal? = null,
        val isCreatingProposal: Boolean = false,
        val proposalError: String? = null,
        val userHasVoted: Boolean = false,
        val userVoteApproves: Boolean = false
    )

    private val _proposalState = MutableStateFlow(ProposalState())
    val proposalState: StateFlow<ProposalState> = _proposalState.asStateFlow()

    // Inicialización
    init {

        // Observar preferencias
        viewModelScope.launch {
            communityService.darkMode.collect { darkMode ->
                _preferencesState.update { it.copy(darkMode = darkMode) }
            }
        }

        viewModelScope.launch {
            communityService.dynamicColors.collect { dynamicColors ->
                _preferencesState.update { it.copy(dynamicColors = dynamicColors) }
            }
        }

        viewModelScope.launch {
            communityService.fontSize.collect { fontSize ->
                _preferencesState.update { it.copy(fontSize = fontSize) }
            }
        }

        viewModelScope.launch {
            communityService.seniorMode.collect { seniorMode ->
                _preferencesState.update { it.copy(seniorMode = seniorMode) }
            }
        }
        // Observar usuario actual
        viewModelScope.launch {
            currentUser.collect { user ->
                _authState.update {
                    it.copy(
                        isLoggedIn = user != null,
                        user = user
                    )
                }
            }
        }
    }

    // Métodos de autenticación
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = communityService.login(email, password)
                result.fold(
                    onSuccess = { user ->
                        _authState.update {
                            it.copy(
                                isLoggedIn = true,
                                loginError = null,
                                user = user
                            )
                        }
                    },
                    onFailure = { exception ->
                        _authState.update {
                            it.copy(
                                loginError = exception.message ?: "Error al iniciar sesión"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _authState.update {
                    it.copy(
                        loginError = e.message ?: "Error inesperado al iniciar sesión"
                    )
                }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            communityService.setDarkMode(enabled)
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            communityService.setDynamicColors(enabled)
        }
    }

    fun setFontSize(size: String) {
        viewModelScope.launch {
            communityService.setFontSize(size)
        }
    }

    fun setSeniorMode(enabled: Boolean) {
        viewModelScope.launch {
            communityService.setSeniorMode(enabled)
        }
    }

    fun register(email: String, password: String, name: String, phone: String? = null) {
        viewModelScope.launch {
            _authState.update { it.copy(isRegistering = true) }

            try {
                val result = communityService.register(email, password, name, phone)
                result.fold(
                    onSuccess = { user ->
                        _authState.update {
                            it.copy(
                                isRegistering = false,
                                isLoggedIn = true,
                                registerError = null,
                                user = user
                            )
                        }
                    },
                    onFailure = { exception ->
                        _authState.update {
                            it.copy(
                                isRegistering = false,
                                registerError = exception.message ?: "Error al registrarse"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _authState.update {
                    it.copy(
                        isRegistering = false,
                        registerError = e.message ?: "Error inesperado al registrarse"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                communityService.logout()
                _authState.update {
                    AuthState() // Resetear estado
                }
                _communityState.update {
                    CommunityState() // Resetear estado
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                communityService.resetPassword(email)
                // Manejar éxito
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    // Métodos de comunidad
    fun loadCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                val result = communityService.getCommunity(communityId)
                result.fold(
                    onSuccess = { community ->
                        // Determinar rol del usuario
                        val userId = _authState.value.user?.id ?: ""
                        val userMembership = community.members.find { it.userId == userId }
                        val userRole = userMembership?.role ?: Role.MEMBER

                        _communityState.update {
                            it.copy(
                                selectedCommunity = community,
                                membershipRequests = community.membershipRequests,
                                userRole = userRole
                            )
                        }

                        // Actualizar anuncios, eventos y propuestas
                        _announcementState.update {
                            it.copy(announcements = community.announcements)
                        }

                        _eventState.update {
                            it.copy(events = community.events)
                        }

                        _proposalState.update {
                            it.copy(proposals = community.proposals)
                        }
                    },
                    onFailure = { exception ->
                        // Manejar error
                    }
                )
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun createCommunity(
        name: String,
        description: String,
        address: String,
        isPublic: Boolean,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                // Subir imagen si existe
                var imageUrl: String? = null
                if (imageUri != null) {
                    // Convertir Uri a ByteArray (implementación simplificada)
                    val imageBytes = ByteArray(0) // En una app real, leerías los bytes del Uri
                    val path = "communities/${UUID.randomUUID()}.jpg"
                    val uploadResult = communityService.uploadImage(imageBytes, path)

                    uploadResult.fold(
                        onSuccess = { url -> imageUrl = url },
                        onFailure = { /* Manejar error */ }
                    )
                }

                // Crear comunidad
                val community = Community(
                    name = name,
                    description = description,
                    address = address,
                    isPublic = isPublic,
                    imageUrl = imageUrl
                )

                val result = communityService.createCommunity(community)
                result.fold(
                    onSuccess = { newCommunity ->
                        // Actualizar estado
                        _communityState.update {
                            it.copy(selectedCommunity = newCommunity)
                        }
                    },
                    onFailure = { exception ->
                        // Manejar error
                    }
                )
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun searchCommunities(query: String) {
        viewModelScope.launch {
            _communityState.update { it.copy(isSearching = true) }

            try {
                val result = communityService.searchCommunities(query)
                result.fold(
                    onSuccess = { communities ->
                        _communityState.update {
                            it.copy(
                                isSearching = false,
                                searchResults = communities,
                                searchError = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _communityState.update {
                            it.copy(
                                isSearching = false,
                                searchError = exception.message ?: "Error en la búsqueda"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                // Maneja cualquier excepción inesperada
                _communityState.update {
                    it.copy(
                        isSearching = false,
                        searchError = e.message ?: "Error inesperado en la búsqueda"
                    )
                }
            }
        }
    }

    fun generateInviteLink(communityId: String) {
        viewModelScope.launch {
            try {
                val result = communityService.generateInviteLink(communityId)
                result.fold(
                    onSuccess = { code ->
                        _communityState.update {
                            it.copy(inviteCode = code)
                        }
                    },
                    onFailure = { exception ->
                        // Manejar error
                    }
                )
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun joinCommunityWithInviteCode(inviteCode: String) {
        viewModelScope.launch {
            try {
                val result = communityService.joinCommunityWithInviteCode(inviteCode)
                result.fold(
                    onSuccess = { community ->
                        _communityState.update {
                            it.copy(selectedCommunity = community)
                        }
                    },
                    onFailure = { exception ->
                        // Manejar error
                    }
                )
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    // Métodos de membresía
    fun requestMembership(communityId: String) {
        viewModelScope.launch {
            try {
                communityService.requestMembership(communityId)
                // Manejar éxito
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun approveMembershipRequest(communityId: String, userId: String) {
        viewModelScope.launch {
            try {
                val result = communityService.approveMembershipRequest(communityId, userId)
                if (result.isSuccess) {
                    // Recargar comunidad para actualizar miembros
                    loadCommunity(communityId)
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun rejectMembershipRequest(communityId: String, userId: String) {
        viewModelScope.launch {
            try {
                val result = communityService.rejectMembershipRequest(communityId, userId)
                if (result.isSuccess) {
                    // Recargar comunidad para actualizar solicitudes
                    loadCommunity(communityId)
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun leaveCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                val result = communityService.leaveCommunity(communityId)
                if (result.isSuccess) {
                    _communityState.update {
                        it.copy(selectedCommunity = null)
                    }
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    // Métodos de anuncios
    fun loadAnnouncements(communityId: String) {
        viewModelScope.launch {
            try {
                val result = communityService.getAnnouncements(communityId)
                result.fold(
                    onSuccess = { announcements ->
                        _announcementState.update {
                            it.copy(announcements = announcements)
                        }
                    },
                    onFailure = { exception ->
                        _announcementState.update {
                            it.copy(announcementError = exception.message)
                        }
                    }
                )
            } catch (e: Exception) {
                _announcementState.update {
                    it.copy(announcementError = e.message)
                }
            }
        }
    }

    fun createAnnouncement(
        communityId: String,
        title: String,
        body: String,
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _announcementState.update { it.copy(isCreatingAnnouncement = true) }

            try {
                // Subir imagen si existe
                var imageUrl: String? = null
                if (imageUri != null) {
                    // Convertir Uri a ByteArray (implementación simplificada)
                    val imageBytes = ByteArray(0) // En una app real, leerías los bytes del Uri
                    val path = "announcements/${UUID.randomUUID()}.jpg"
                    val uploadResult = communityService.uploadImage(imageBytes, path)

                    uploadResult.fold(
                        onSuccess = { url -> imageUrl = url },
                        onFailure = { /* Manejar error */ }
                    )
                }

                // Crear anuncio
                val announcement = Announcement(
                    title = title,
                    body = body,
                    imageUrl = imageUrl
                )

                val result = communityService.createAnnouncement(communityId, announcement)
                result.fold(
                    onSuccess = { newAnnouncement ->
                        _announcementState.update {
                            it.copy(
                                isCreatingAnnouncement = false,
                                selectedAnnouncement = newAnnouncement,
                                announcementError = null
                            )
                        }

                        // Recargar anuncios
                        loadAnnouncements(communityId)
                    },
                    onFailure = { exception ->
                        _announcementState.update {
                            it.copy(
                                isCreatingAnnouncement = false,
                                announcementError = exception.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _announcementState.update {
                    it.copy(
                        isCreatingAnnouncement = false,
                        announcementError = e.message
                    )
                }
            }
        }
    }

    // Métodos de eventos
    fun loadEvents(communityId: String) {
        viewModelScope.launch {
            try {
                val result = communityService.getEvents(communityId)
                result.fold(
                    onSuccess = { events ->
                        _eventState.update {
                            it.copy(events = events)
                        }
                    },
                    onFailure = { exception ->
                        _eventState.update {
                            it.copy(eventError = exception.message)
                        }
                    }
                )
            } catch (e: Exception) {
                _eventState.update {
                    it.copy(eventError = e.message)
                }
            }
        }
    }

    fun createEvent(
        communityId: String,
        title: String,
        description: String,
        startDate: Timestamp,
        endDate: Timestamp,
        location: GeoPoint? = null,
        imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            _eventState.update { it.copy(isCreatingEvent = true) }

            try {
                // Subir imagen si existe
                var imageUrl: String? = null
                if (imageUri != null) {
                    // Convertir Uri a ByteArray (implementación simplificada)
                    val imageBytes = ByteArray(0) // En una app real, leerías los bytes del Uri
                    val path = "events/${UUID.randomUUID()}.jpg"
                    val uploadResult = communityService.uploadImage(imageBytes, path)

                    uploadResult.fold(
                        onSuccess = { url -> imageUrl = url },
                        onFailure = { /* Manejar error */ }
                    )
                }

                // Crear evento
                val event = Event(
                    title = title,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    location = location,
                    imageUrl = imageUrl
                )

                val result = communityService.createEvent(communityId, event)
                result.fold(
                    onSuccess = { newEvent ->
                        _eventState.update {
                            it.copy(
                                isCreatingEvent = false,
                                selectedEvent = newEvent,
                                eventError = null
                            )
                        }

                        // Recargar eventos
                        loadEvents(communityId)
                    },
                    onFailure = { exception ->
                        _eventState.update {
                            it.copy(
                                isCreatingEvent = false,
                                eventError = exception.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _eventState.update {
                    it.copy(
                        isCreatingEvent = false,
                        eventError = e.message
                    )
                }
            }
        }
    }

    fun respondToEvent(communityId: String, eventId: String, status: RsvpStatus) {
        viewModelScope.launch {
            try {
                val result = communityService.respondToEvent(communityId, eventId, status)
                result.fold(
                    onSuccess = { success ->
                        _eventState.update {
                            it.copy(userRsvpStatus = status)
                        }

                        // Recargar eventos
                        loadEvents(communityId)
                    },
                    onFailure = { exception ->
                        // Manejar error
                    }
                )
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    // Métodos de propuestas
    fun loadProposals(communityId: String) {
        viewModelScope.launch {
            try {
                val result = communityService.getProposals(communityId)
                result.fold(
                    onSuccess = { proposals ->
                        _proposalState.update {
                            it.copy(proposals = proposals)
                        }
                    },
                    onFailure = { exception ->
                        _proposalState.update {
                            it.copy(proposalError = exception.message)
                        }
                    }
                )
            } catch (e: Exception) {
                _proposalState.update {
                    it.copy(proposalError = e.message)
                }
            }
        }
    }

    fun createProposal(
        communityId: String,
        title: String,
        description: String,
        expiresAt: Timestamp? = null
    ) {
        viewModelScope.launch {
            _proposalState.update { it.copy(isCreatingProposal = true) }

            try {
                // Crear propuesta
                val proposal = Proposal(
                    title = title,
                    description = description,
                    expiresAt = expiresAt
                )

                val result = communityService.createProposal(communityId, proposal)
                result.fold(
                    onSuccess = { newProposal ->
                        _proposalState.update {
                            it.copy(
                                isCreatingProposal = false,
                                selectedProposal = newProposal,
                                proposalError = null
                            )
                        }

                        // Recargar propuestas
                        loadProposals(communityId)
                    },
                    onFailure = { exception ->
                        _proposalState.update {
                            it.copy(
                                isCreatingProposal = false,
                                proposalError = exception.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _proposalState.update {
                    it.copy(
                        isCreatingProposal = false,
                        proposalError = e.message
                    )
                }
            }
        }
    }

    fun voteOnProposal(communityId: String, proposalId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                val result = communityService.voteOnProposal(communityId, proposalId, approve)
                result.fold(
                    onSuccess = { success ->
                        _proposalState.update {
                            it.copy(
                                userHasVoted = true,
                                userVoteApproves = approve
                            )
                        }

                        // Recargar propuestas
                        loadProposals(communityId)
                    },
                    onFailure = { exception ->
                        // Manejar error
                    }
                )
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    // Métodos de reportes
    fun createReport(communityId: String, entityType: String, entityId: String, reason: String) {
        viewModelScope.launch {
            try {
                val report = Report(
                    communityId = communityId,
                    entityType = entityType,
                    entityId = entityId,
                    reportedBy = _authState.value.user?.id ?: "",
                    reason = reason
                )

                communityService.createReport(report)
                // Manejar éxito
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    // Métodos de notificaciones
    fun updateNotificationPreferences(
        communityId: String,
        announcements: Boolean,
        events: Boolean,
        proposals: Boolean
    ) {
        viewModelScope.launch {
            try {
                val preferences = NotificationSubscription(
                    communityId = communityId,
                    announcements = announcements,
                    events = events,
                    proposals = proposals
                )

                communityService.updateNotificationPreferences(communityId, preferences)
                // Manejar éxito
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}