package com.vecinapp.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Roles que pueden tener los usuarios dentro de la directiva de una comunidad.
 */
@Serializable
enum class Role { ADMIN, MODERATOR, MEMBER }

/**
 * Estados genéricos (p.ej. para MembershipRequest o Report).
 */
@Serializable
enum class Status { PENDING, APPROVED, REJECTED }

/**
 * Estado de asistencia a un evento.
 */
@Serializable
enum class RsvpStatus { GOING, MAYBE, DECLINED, UNDECIDED }

/**
 * Suscripción a notificaciones de un usuario para una comunidad.
 */
@Serializable
data class NotificationSubscription(
    val communityId: String = "",
    val announcements: Boolean = true,
    val events: Boolean = true,
    val proposals: Boolean = true
)

/**
 * Miembro aprobado de una comunidad.
 */
@Serializable
data class Membership(
    val userId: String = "",
    val role: Role = Role.MEMBER,
    @Contextual @ServerTimestamp
    val joinedAt: Timestamp? = null
)

/**
 * Solicitud de membresía pendiente.
 */
@Serializable
data class MembershipRequest(
    val userId: String = "",
    @Contextual @ServerTimestamp
    val requestedAt: Timestamp? = null,
    val status: Status = Status.PENDING
)

/**
 * Miembro de la directiva (board) de la comunidad.
 */
@Serializable
data class DirectiveMember(
    val userId: String = "",
    val role: Role = Role.ADMIN,
    @Contextual @ServerTimestamp
    val appointedAt: Timestamp? = null
)

/**
 * Comentario en anuncio/propósito.
 */
@Serializable
data class Comment(
    val id: String = "",
    val authorId: String = "",
    val content: String = "",
    @Contextual @ServerTimestamp
    val createdAt: Timestamp? = null
)

/**
 * Adjuntos multimedia genéricos.
 */
@Serializable
data class Attachment(
    val url: String = "",
    val type: String = "image" // p.ej. "image", "document", etc.
)

/**
 * RSVP (asistencia) a un evento.
 */
@Serializable
data class Rsvp(
    val userId: String = "",
    val status: RsvpStatus = RsvpStatus.UNDECIDED,
    @Contextual @ServerTimestamp
    val respondedAt: Timestamp? = null
)

/**
 * Voto / like en anuncio o propuesta.
 */
@Serializable
data class Vote(
    val userId: String = "",
    @Contextual @ServerTimestamp
    val votedAt: Timestamp? = null
)

/**
 * Reporte para moderación.
 */
@Serializable
data class Report(
    val id: String = "",
    val communityId: String = "",
    val entityType: String = "", // "announcement", "proposal", etc.
    val entityId: String = "",
    val reportedBy: String = "",
    val reason: String = "",
    val status: Status = Status.PENDING,
    @Contextual @ServerTimestamp
    val createdAt: Timestamp? = null
)

/**
 * Anuncio dentro de una comunidad.
 */
@Serializable
data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val authorId: String = "",
    @Contextual @ServerTimestamp
    val createdAt: Timestamp? = null,
    @Contextual @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val attachments: List<Attachment> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val rsvps: List<Rsvp> = emptyList(),
    val reports: List<Report> = emptyList(),
    val voteCount: Int = 0
)

/**
 * Evento en la comunidad.
 */
@Serializable
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    @Contextual @ServerTimestamp
    val dateTime: Timestamp? = null,
    @Contextual
    val location: GeoPoint? = null,
    val imageUrl: String? = null,
    val organizerId: String = "",
    @Contextual @ServerTimestamp
    val createdAt: Timestamp? = null,
    @Contextual @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val attachments: List<Attachment> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val rsvps: List<Rsvp> = emptyList(),
    val reports: List<Report> = emptyList(),
    val voteCount: Int = 0
)

/**
 * Propuesta/votación dentro de la comunidad.
 */
@Serializable
data class Proposal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val proposerId: String = "",
    @Contextual @ServerTimestamp
    val createdAt: Timestamp? = null,
    @Contextual @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val attachments: List<Attachment> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val votes: List<Vote> = emptyList(),
    val reports: List<Report> = emptyList(),
    val voteCount: Int = 0
)

/**
 * Entidad raíz: Comunidad, que agrupa todos los módulos.
 */
@Serializable
data class Community(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val creatorId: String = "",
    @Contextual @ServerTimestamp
    val createdAt: Timestamp? = null,
    @Contextual @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val members: List<Membership> = emptyList(),
    val membershipRequests: List<MembershipRequest> = emptyList(),
    val directive: List<DirectiveMember> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val events: List<Event> = emptyList(),
    val proposals: List<Proposal> = emptyList(),
    val address: String,
    val isPublic: Boolean,
    val imageUrl: String?
)
