package com.vecinapp.domain.service

import com.google.firebase.firestore.GeoPoint
import com.vecinapp.domain.model.Announcement
import com.vecinapp.domain.model.Comment
import com.vecinapp.domain.model.Community
import com.vecinapp.domain.model.Event
import com.vecinapp.domain.model.Membership
import com.vecinapp.domain.model.MembershipRequest
import com.vecinapp.domain.model.NotificationSubscription
import com.vecinapp.domain.model.Proposal
import com.vecinapp.domain.model.Report
import com.vecinapp.domain.model.Role
import com.vecinapp.domain.model.RsvpStatus
import com.vecinapp.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interfaz principal que define todas las operaciones de la aplicación
 */
interface ICommunityService {
    // Propiedades observables
    val currentUser: Flow<User?>
    val userCommunities: Flow<List<Community>>
    val isLoading: StateFlow<Boolean>
    val errorMessage: StateFlow<String?>

    // Preferencias de usuario
    val darkMode: Flow<Boolean>
    val dynamicColors: Flow<Boolean>
    val fontSize: Flow<String>
    val seniorMode: Flow<Boolean>

    suspend fun setDarkMode(enabled: Boolean): Result<Boolean>
    suspend fun setDynamicColors(enabled: Boolean): Result<Boolean>
    suspend fun setFontSize(size: String): Result<Boolean>
    suspend fun setSeniorMode(enabled: Boolean): Result<Boolean>

    // Autenticación
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String?
    ): Result<User>

    suspend fun logout(): Result<Boolean>
    suspend fun resetPassword(email: String): Result<Boolean>
    suspend fun updateUserProfile(user: User): Result<User>

    // Comunidades
    suspend fun getCommunity(communityId: String): Result<Community>
    suspend fun createCommunity(community: Community): Result<Community>
    suspend fun updateCommunity(community: Community): Result<Community>
    suspend fun deleteCommunity(communityId: String): Result<Boolean>
    suspend fun searchCommunities(query: String): Result<List<Community>>
    suspend fun joinCommunityWithInviteCode(inviteCode: String): Result<Community>
    suspend fun generateInviteLink(communityId: String): Result<String>

    // Membresía
    suspend fun requestMembership(communityId: String): Result<MembershipRequest>
    suspend fun approveMembershipRequest(communityId: String, userId: String): Result<Membership>
    suspend fun rejectMembershipRequest(communityId: String, userId: String): Result<Boolean>
    suspend fun leaveCommunity(communityId: String): Result<Boolean>
    suspend fun updateMemberRole(communityId: String, userId: String, role: Role): Result<Boolean>

    // Directiva
    suspend fun addDirectiveMember(communityId: String, userId: String, role: Role): Result<Boolean>
    suspend fun removeDirectiveMember(communityId: String, userId: String): Result<Boolean>

    // Anuncios
    suspend fun getAnnouncements(communityId: String): Result<List<Announcement>>
    suspend fun createAnnouncement(
        communityId: String,
        announcement: Announcement
    ): Result<Announcement>

    suspend fun updateAnnouncement(
        communityId: String,
        announcement: Announcement
    ): Result<Announcement>

    suspend fun deleteAnnouncement(communityId: String, announcementId: String): Result<Boolean>
    suspend fun voteAnnouncement(
        communityId: String,
        announcementId: String,
        approve: Boolean
    ): Result<Boolean>

    suspend fun commentOnAnnouncement(
        communityId: String,
        announcementId: String,
        comment: String
    ): Result<Comment>

    // Eventos
    suspend fun getEvents(communityId: String): Result<List<Event>>
    suspend fun createEvent(communityId: String, event: Event): Result<Event>
    suspend fun updateEvent(communityId: String, event: Event): Result<Event>
    suspend fun deleteEvent(communityId: String, eventId: String): Result<Boolean>
    suspend fun respondToEvent(
        communityId: String,
        eventId: String,
        status: RsvpStatus
    ): Result<Boolean>

    suspend fun commentOnEvent(
        communityId: String,
        eventId: String,
        comment: String
    ): Result<Comment>

    // Propuestas
    suspend fun getProposals(communityId: String): Result<List<Proposal>>
    suspend fun createProposal(communityId: String, proposal: Proposal): Result<Proposal>
    suspend fun updateProposal(communityId: String, proposal: Proposal): Result<Proposal>
    suspend fun deleteProposal(communityId: String, proposalId: String): Result<Boolean>
    suspend fun voteOnProposal(
        communityId: String,
        proposalId: String,
        approve: Boolean
    ): Result<Boolean>

    suspend fun commentOnProposal(
        communityId: String,
        proposalId: String,
        comment: String
    ): Result<Comment>

    // Reportes
    suspend fun createReport(report: Report): Result<Report>
    suspend fun resolveReport(reportId: String, approved: Boolean): Result<Boolean>
    suspend fun getReports(communityId: String): Result<List<Report>>

    // Notificaciones
    suspend fun updateNotificationPreferences(
        communityId: String,
        preferences: NotificationSubscription
    ): Result<Boolean>

    suspend fun getNotificationPreferences(communityId: String): Result<NotificationSubscription>
    suspend fun registerDeviceToken(token: String): Result<Boolean>

    // Utilidades
    suspend fun uploadImage(imageBytes: ByteArray, path: String): Result<String>
    suspend fun getLocationFromAddress(address: String): Result<GeoPoint>
    suspend fun getAddressFromLocation(location: GeoPoint): Result<String>
    suspend fun startQrCodeScanner(): Flow<String?>
}

