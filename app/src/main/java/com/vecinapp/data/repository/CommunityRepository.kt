package com.vecinapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.vecinapp.data.preferences.UserPreferencesManager
import com.vecinapp.domain.model.Announcement
import com.vecinapp.domain.model.Comment
import com.vecinapp.domain.model.Community
import com.vecinapp.domain.model.DirectiveMember
import com.vecinapp.domain.model.Event
import com.vecinapp.domain.model.Membership
import com.vecinapp.domain.model.MembershipRequest
import com.vecinapp.domain.model.NotificationSubscription
import com.vecinapp.domain.model.Proposal
import com.vecinapp.domain.model.Report
import com.vecinapp.domain.model.Role
import com.vecinapp.domain.model.RsvpStatus
import com.vecinapp.domain.model.Status
import com.vecinapp.domain.model.User
import com.vecinapp.domain.service.ICommunityService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferencesManager: UserPreferencesManager
) : ICommunityService {


    // StateFlows para estados observables
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage

    override val darkMode: Flow<Boolean> = preferencesManager.darkModeFlow
    override val dynamicColors: Flow<Boolean> = preferencesManager.dynamicColorsFlow
    override val fontSize: Flow<String> = preferencesManager.fontSizeFlow
    override val seniorMode: Flow<Boolean> = preferencesManager.seniorModeFlow

    override suspend fun setDarkMode(enabled: Boolean): Result<Boolean> {
        return try {
            preferencesManager.setDarkMode(enabled)
            Result.success(true)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun setDynamicColors(enabled: Boolean): Result<Boolean> {
        return try {
            preferencesManager.setDynamicColors(enabled)
            Result.success(true)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun setFontSize(size: String): Result<Boolean> {
        return try {
            preferencesManager.setFontSize(size)
            Result.success(true)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun setSeniorMode(enabled: Boolean): Result<Boolean> {
        return try {
            preferencesManager.setSeniorMode(enabled)
            Result.success(true)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }


    // Obtener usuario actual como Flow
    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Obtener datos adicionales del usuario desde Firestore
                firestore.collection("users").document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val user =
                                document.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                                    ?: User(id = firebaseUser.uid, email = firebaseUser.email ?: "")
                            trySend(user)
                        } else {
                            // Si no existe en Firestore, crear con datos básicos
                            val newUser = User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                name = firebaseUser.displayName ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString()
                            )
                            trySend(newUser)
                        }
                    }
                    .addOnFailureListener {
                        // En caso de error, enviar usuario básico
                        val basicUser = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = firebaseUser.displayName ?: ""
                        )
                        trySend(basicUser)
                    }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Obtener comunidades del usuario como Flow
    override val userCommunities: Flow<List<Community>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        // Buscar comunidades donde el usuario es miembro
        val listenerRegistration = firestore.collection("communities")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Error al cargar comunidades: ${error.message}"
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val communities = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Community::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(communities)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    // Implementación de métodos de autenticación
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            _isLoading.value = true
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = if (userDoc.exists()) {
                    userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)
                } else {
                    User(id = firebaseUser.uid, email = email)
                }

                _isLoading.value = false
                Result.success(user ?: User(id = firebaseUser.uid, email = email))
            } else {
                _isLoading.value = false
                Result.failure(Exception("Error al iniciar sesión"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String?
    ): Result<User> {
        return try {
            _isLoading.value = true
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    name = name,
                    phone = phone,
                    createdAt = Timestamp.now()
                )

                // Guardar datos adicionales en Firestore
                firestore.collection("users").document(firebaseUser.uid)
                    .set(user)
                    .await()

                _isLoading.value = false
                Result.success(user)
            } else {
                _isLoading.value = false
                Result.failure(Exception("Error al registrar usuario"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Boolean> {
        return try {
            auth.signOut()
            Result.success(true)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            _isLoading.value = true
            auth.sendPasswordResetEmail(email).await()
            _isLoading.value = false
            Result.success(true)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Actualizar en Firestore
            firestore.collection("users").document(currentUserId)
                .set(user)
                .await()

            _isLoading.value = false
            Result.success(user)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    // Implementación de métodos de comunidades
    override suspend fun getCommunity(communityId: String): Result<Community> {
        return try {
            _isLoading.value = true
            val document = firestore.collection("communities").document(communityId).get().await()

            if (document.exists()) {
                val community = document.toObject(Community::class.java)?.copy(id = document.id)
                _isLoading.value = false

                if (community != null) {
                    Result.success(community)
                } else {
                    Result.failure(Exception("Error al convertir documento"))
                }
            } else {
                _isLoading.value = false
                Result.failure(Exception("Comunidad no encontrada"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun createCommunity(community: Community): Result<Community> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Crear comunidad con el usuario actual como creador y miembro
            val newCommunity = community.copy(
                creatorId = currentUserId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                members = listOf(
                    Membership(
                        userId = currentUserId,
                        role = Role.ADMIN,
                        joinedAt = Timestamp.now()
                    )
                ),
                directive = listOf(
                    DirectiveMember(
                        userId = currentUserId,
                        role = Role.ADMIN,
                        appointedAt = Timestamp.now()
                    )
                )
            )

            // Guardar en Firestore
            val docRef = firestore.collection("communities").add(newCommunity).await()
            val createdCommunity = newCommunity.copy(id = docRef.id)

            // Actualizar el documento con su ID
            firestore.collection("communities").document(docRef.id)
                .update("id", docRef.id)
                .await()

            // Actualizar lista de comunidades del usuario
            firestore.collection("users").document(currentUserId)
                .update(
                    "communities",
                    com.google.firebase.firestore.FieldValue.arrayUnion(docRef.id)
                )
                .await()

            _isLoading.value = false
            Result.success(createdCommunity)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun updateCommunity(community: Community): Result<Community> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar que el usuario tenga permisos (admin o moderador)
            val userMembership = community.members.find { it.userId == currentUserId }
            if (userMembership == null || userMembership.role == Role.MEMBER) {
                return Result.failure(Exception("No tienes permisos para actualizar esta comunidad"))
            }

            // Actualizar en Firestore
            val updatedCommunity = community.copy(updatedAt = Timestamp.now())
            firestore.collection("communities").document(community.id)
                .set(updatedCommunity)
                .await()

            _isLoading.value = false
            Result.success(updatedCommunity)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun deleteCommunity(communityId: String): Result<Boolean> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Obtener la comunidad para verificar permisos
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar que el usuario sea el creador
            if (community.creatorId != currentUserId) {
                _isLoading.value = false
                return Result.failure(Exception("Solo el creador puede eliminar la comunidad"))
            }

            // Eliminar de Firestore
            firestore.collection("communities").document(communityId)
                .delete()
                .await()

            // Actualizar listas de comunidades de todos los miembros
            community.members.forEach { member ->
                firestore.collection("users").document(member.userId)
                    .update(
                        "communities",
                        com.google.firebase.firestore.FieldValue.arrayRemove(communityId)
                    )
                    .await()
            }

            _isLoading.value = false
            Result.success(true)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun searchCommunities(query: String): Result<List<Community>> {
        return try {
            _isLoading.value = true

            // Buscar comunidades públicas que coincidan con la consulta
            val snapshot = firestore.collection("communities")
                .whereEqualTo("isPublic", true)
                .get()
                .await()

            val communities = snapshot.documents.mapNotNull { doc ->
                try {
                    val community = doc.toObject(Community::class.java)?.copy(id = doc.id)
                    // Filtrar por nombre o descripción que contenga la consulta
                    if (community != null &&
                        (community.name.contains(query, ignoreCase = true) ||
                                community.description?.contains(query, ignoreCase = true) == true ||
                                community.address.contains(query, ignoreCase = true))
                    ) {
                        community
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            _isLoading.value = false
            Result.success(communities)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun joinCommunityWithInviteCode(inviteCode: String): Result<Community> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Buscar la comunidad con ese código de invitación
            val snapshot = firestore.collection("inviteCodes")
                .whereEqualTo("code", inviteCode)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                _isLoading.value = false
                return Result.failure(Exception("Código de invitación inválido o expirado"))
            }

            val inviteDoc = snapshot.documents.first()
            val communityId = inviteDoc.getString("communityId") ?: ""

            if (communityId.isEmpty()) {
                _isLoading.value = false
                return Result.failure(Exception("Código de invitación inválido"))
            }

            // Obtener la comunidad
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)?.copy(id = communityId)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar si el usuario ya es miembro
            if (community.members.any { it.userId == currentUserId }) {
                _isLoading.value = false
                return Result.success(community) // Ya es miembro, devolver la comunidad
            }

            // Añadir al usuario como miembro
            val newMembership = Membership(
                userId = currentUserId,
                role = Role.MEMBER,
                joinedAt = Timestamp.now()
            )

            val updatedMembers = community.members + newMembership

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update("members", updatedMembers)
                .await()

            // Actualizar lista de comunidades del usuario
            firestore.collection("users").document(currentUserId)
                .update(
                    "communities",
                    com.google.firebase.firestore.FieldValue.arrayUnion(communityId)
                )
                .await()

            // Obtener la comunidad actualizada
            val updatedCommunityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val updatedCommunity =
                updatedCommunityDoc.toObject(Community::class.java)?.copy(id = communityId)
                    ?: community.copy(members = updatedMembers)

            _isLoading.value = false
            Result.success(updatedCommunity)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun generateInviteLink(communityId: String): Result<String> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar que el usuario tenga permisos
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            val userMembership = community.members.find { it.userId == currentUserId }
            if (userMembership == null || userMembership.role == Role.MEMBER) {
                _isLoading.value = false
                return Result.failure(Exception("No tienes permisos para generar invitaciones"))
            }

            // Generar código único
            val inviteCode = UUID.randomUUID().toString().substring(0, 8)

            // Guardar en Firestore con expiración (7 días)
            val expiresAt = java.sql.Timestamp(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
            val inviteData = hashMapOf(
                "code" to inviteCode,
                "communityId" to communityId,
                "createdBy" to currentUserId,
                "createdAt" to Timestamp.now(),
                "expiresAt" to expiresAt
            )

            firestore.collection("inviteCodes")
                .add(inviteData)
                .await()

            _isLoading.value = false
            Result.success(inviteCode)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    // Implementación de métodos de membresía
    override suspend fun requestMembership(communityId: String): Result<MembershipRequest> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar si la comunidad existe
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar si el usuario ya es miembro
            if (community.members.any { it.userId == currentUserId }) {
                _isLoading.value = false
                return Result.failure(Exception("Ya eres miembro de esta comunidad"))
            }

            // Verificar si ya hay una solicitud pendiente
            if (community.membershipRequests.any { it.userId == currentUserId && it.status == Status.PENDING }) {
                _isLoading.value = false
                return Result.failure(Exception("Ya tienes una solicitud pendiente"))
            }

            // Crear solicitud
            val request = MembershipRequest(
                userId = currentUserId,
                requestedAt = Timestamp.now(),
                status = Status.PENDING
            )

            val updatedRequests = community.membershipRequests + request

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update("membershipRequests", updatedRequests)
                .await()

            _isLoading.value = false
            Result.success(request)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun approveMembershipRequest(
        communityId: String,
        userId: String
    ): Result<Membership> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar si la comunidad existe
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar que el usuario actual tenga permisos
            val userMembership = community.members.find { it.userId == currentUserId }
            if (userMembership == null || userMembership.role == Role.MEMBER) {
                _isLoading.value = false
                return Result.failure(Exception("No tienes permisos para aprobar solicitudes"))
            }

            // Buscar la solicitud
            val requestIndex = community.membershipRequests.indexOfFirst {
                it.userId == userId && it.status == Status.PENDING
            }

            if (requestIndex == -1) {
                _isLoading.value = false
                return Result.failure(Exception("Solicitud no encontrada o ya procesada"))
            }

            // Crear nueva membresía
            val newMembership = Membership(
                userId = userId,
                role = Role.MEMBER,
                joinedAt = Timestamp.now()
            )

            // Actualizar listas
            val updatedRequests = community.membershipRequests.toMutableList()
            updatedRequests[requestIndex] =
                updatedRequests[requestIndex].copy(status = Status.APPROVED)

            val updatedMembers = community.members + newMembership

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update(
                    mapOf(
                        "membershipRequests" to updatedRequests,
                        "members" to updatedMembers
                    )
                )
                .await()

            // Actualizar lista de comunidades del usuario
            firestore.collection("users").document(userId)
                .update(
                    "communities",
                    com.google.firebase.firestore.FieldValue.arrayUnion(communityId)
                )
                .await()

            _isLoading.value = false
            Result.success(newMembership)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun rejectMembershipRequest(
        communityId: String,
        userId: String
    ): Result<Boolean> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar si la comunidad existe
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar que el usuario actual tenga permisos
            val userMembership = community.members.find { it.userId == currentUserId }
            if (userMembership == null || userMembership.role == Role.MEMBER) {
                _isLoading.value = false
                return Result.failure(Exception("No tienes permisos para rechazar solicitudes"))
            }

            // Buscar la solicitud
            val requestIndex = community.membershipRequests.indexOfFirst {
                it.userId == userId && it.status == Status.PENDING
            }

            if (requestIndex == -1) {
                _isLoading.value = false
                return Result.failure(Exception("Solicitud no encontrada o ya procesada"))
            }

            // Actualizar la solicitud
            val updatedRequests = community.membershipRequests.toMutableList()
            updatedRequests[requestIndex] =
                updatedRequests[requestIndex].copy(status = Status.REJECTED)

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update("membershipRequests", updatedRequests)
                .await()

            _isLoading.value = false
            Result.success(true)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun leaveCommunity(communityId: String): Result<Boolean> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar si la comunidad existe
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar si el usuario es miembro
            val memberIndex = community.members.indexOfFirst { it.userId == currentUserId }
            if (memberIndex == -1) {
                _isLoading.value = false
                return Result.failure(Exception("No eres miembro de esta comunidad"))
            }

            // Verificar si el usuario es el creador
            if (community.creatorId == currentUserId) {
                _isLoading.value = false
                return Result.failure(Exception("El creador no puede abandonar la comunidad, debe eliminarla o transferir propiedad"))
            }

            // Eliminar al usuario de la lista de miembros
            val updatedMembers = community.members.toMutableList()
            updatedMembers.removeAt(memberIndex)

            // Eliminar al usuario de la directiva si es parte
            val updatedDirective = community.directive.filter { it.userId != currentUserId }

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update(
                    mapOf(
                        "members" to updatedMembers,
                        "directive" to updatedDirective
                    )
                )
                .await()

            // Actualizar lista de comunidades del usuario
            firestore.collection("users").document(currentUserId)
                .update(
                    "communities",
                    com.google.firebase.firestore.FieldValue.arrayRemove(communityId)
                )
                .await()

            _isLoading.value = false
            Result.success(true)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun updateMemberRole(
        communityId: String,
        userId: String,
        role: Role
    ): Result<Boolean> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar si la comunidad existe
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar que el usuario actual sea administrador
            val currentUserMembership = community.members.find { it.userId == currentUserId }
            if (currentUserMembership == null || currentUserMembership.role != Role.ADMIN) {
                _isLoading.value = false
                return Result.failure(Exception("Solo los administradores pueden cambiar roles"))
            }

            // Buscar al miembro a actualizar
            val memberIndex = community.members.indexOfFirst { it.userId == userId }
            if (memberIndex == -1) {
                _isLoading.value = false
                return Result.failure(Exception("Usuario no encontrado en la comunidad"))
            }

            // Actualizar el rol
            val updatedMembers = community.members.toMutableList()
            updatedMembers[memberIndex] = updatedMembers[memberIndex].copy(role = role)

            // Actualizar la directiva si corresponde
            val directiveIndex = community.directive.indexOfFirst { it.userId == userId }
            val updatedDirective = community.directive.toMutableList()

            if (role == Role.ADMIN || role == Role.MODERATOR) {
                // Añadir o actualizar en la directiva
                if (directiveIndex != -1) {
                    updatedDirective[directiveIndex] =
                        updatedDirective[directiveIndex].copy(role = role)
                } else {
                    updatedDirective.add(
                        DirectiveMember(
                            userId = userId,
                            role = role,
                            appointedAt = Timestamp.now()
                        )
                    )
                }
            } else if (directiveIndex != -1) {
                // Remover de la directiva si ya no es admin o moderador
                updatedDirective.removeAt(directiveIndex)
            }

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update(
                    mapOf(
                        "members" to updatedMembers,
                        "directive" to updatedDirective
                    )
                )
                .await()

            _isLoading.value = false
            Result.success(true)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    // Implementación de métodos de directiva
    override suspend fun addDirectiveMember(
        communityId: String,
        userId: String,
        role: Role
    ): Result<Boolean> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar si la comunidad existe
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar que el usuario actual sea administrador
            val currentUserMembership = community.members.find { it.userId == currentUserId }
            if (currentUserMembership == null || currentUserMembership.role != Role.ADMIN) {
                _isLoading.value = false
                return Result.failure(Exception("Solo los administradores pueden modificar la directiva"))
            }

            // Verificar que el usuario a añadir sea miembro
            if (!community.members.any { it.userId == userId }) {
                _isLoading.value = false
                return Result.failure(Exception("El usuario debe ser miembro de la comunidad"))
            }

            // Verificar que el rol sea válido para la directiva
            if (role == Role.MEMBER) {
                _isLoading.value = false
                return Result.failure(Exception("El rol debe ser ADMIN o MODERATOR para la directiva"))
            }

            // Verificar si ya está en la directiva
            val directiveIndex = community.directive.indexOfFirst { it.userId == userId }
            val updatedDirective = community.directive.toMutableList()

            if (directiveIndex != -1) {
                // Actualizar rol
                updatedDirective[directiveIndex] =
                    updatedDirective[directiveIndex].copy(role = role)
            } else {
                // Añadir a la directiva
                updatedDirective.add(
                    DirectiveMember(
                        userId = userId,
                        role = role,
                        appointedAt = Timestamp.now()
                    )
                )
            }

            // Actualizar también el rol en la lista de miembros
            val memberIndex = community.members.indexOfFirst { it.userId == userId }
            val updatedMembers = community.members.toMutableList()
            updatedMembers[memberIndex] = updatedMembers[memberIndex].copy(role = role)

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update(
                    mapOf(
                        "directive" to updatedDirective,
                        "members" to updatedMembers
                    )
                )
                .await()

            _isLoading.value = false
            Result.success(true)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun removeDirectiveMember(
        communityId: String,
        userId: String
    ): Result<Boolean> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar si la comunidad existe
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar que el usuario actual sea administrador
            val currentUserMembership = community.members.find { it.userId == currentUserId }
            if (currentUserMembership == null || currentUserMembership.role != Role.ADMIN) {
                _isLoading.value = false
                return Result.failure(Exception("Solo los administradores pueden modificar la directiva"))
            }

            // Verificar que no se esté intentando remover al creador
            if (community.creatorId == userId) {
                _isLoading.value = false
                return Result.failure(Exception("No se puede remover al creador de la directiva"))
            }

            // Verificar si está en la directiva
            val directiveIndex = community.directive.indexOfFirst { it.userId == userId }
            if (directiveIndex == -1) {
                _isLoading.value = false
                return Result.failure(Exception("El usuario no es parte de la directiva"))
            }

            // Remover de la directiva
            val updatedDirective = community.directive.toMutableList()
            updatedDirective.removeAt(directiveIndex)

            // Actualizar también el rol en la lista de miembros
            val memberIndex = community.members.indexOfFirst { it.userId == userId }
            val updatedMembers = community.members.toMutableList()
            updatedMembers[memberIndex] = updatedMembers[memberIndex].copy(role = Role.MEMBER)

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update(
                    mapOf(
                        "directive" to updatedDirective,
                        "members" to updatedMembers
                    )
                )
                .await()

            _isLoading.value = false
            Result.success(true)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    // Implementación de métodos de anuncios
    override suspend fun getAnnouncements(communityId: String): Result<List<Announcement>> {
        return try {
            _isLoading.value = true

            // Obtener la comunidad
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            _isLoading.value = false
            Result.success(community.announcements)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun createAnnouncement(
        communityId: String,
        announcement: Announcement
    ): Result<Announcement> {
        return try {
            _isLoading.value = true
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar si la comunidad existe
            val communityDoc =
                firestore.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            if (community == null) {
                _isLoading.value = false
                return Result.failure(Exception("Comunidad no encontrada"))
            }

            // Verificar que el usuario sea miembro
            if (!community.members.any { it.userId == currentUserId }) {
                _isLoading.value = false
                return Result.failure(Exception("Debes ser miembro para crear anuncios"))
            }

            // Crear anuncio con ID único
            val announcementId = UUID.randomUUID().toString()
            val now = Timestamp.now()
            val newAnnouncement = announcement.copy(
                id = announcementId,
                authorId = currentUserId,
                createdAt = now,
                updatedAt = now
            )

            // Añadir a la lista de anuncios
            val updatedAnnouncements = community.announcements + newAnnouncement

            // Actualizar la comunidad
            firestore.collection("communities").document(communityId)
                .update("announcements", updatedAnnouncements)
                .await()

            _isLoading.value = false
            Result.success(newAnnouncement)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    // Continuar con el resto de implementaciones...

    // Implementación de métodos de utilidades
    override suspend fun uploadImage(imageBytes: ByteArray, path: String): Result<String> {
        return try {
            _isLoading.value = true

            val storageRef = storage.reference.child(path)
            val uploadTask = storageRef.putBytes(imageBytes).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            _isLoading.value = false
            Result.success(downloadUrl)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun getLocationFromAddress(address: String): Result<GeoPoint> {
        // Implementación simulada - en una app real usarías Geocoding API
        return try {
            Result.success(GeoPoint(0.0, 0.0))
        } catch (e: Exception) {
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun getAddressFromLocation(location: GeoPoint): Result<String> {
        // Implementación simulada - en una app real usarías Reverse Geocoding API
        return try {
            Result.success("Dirección simulada")
        } catch (e: Exception) {
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    override suspend fun startQrCodeScanner(): Flow<String?> = callbackFlow {
        // Implementación simulada - en una app real integrarías con CameraX y ML Kit
        trySend(null)
        awaitClose { }
    }

    // Implementaciones pendientes (para completar según necesidad)
    override suspend fun updateAnnouncement(
        communityId: String,
        announcement: Announcement
    ): Result<Announcement> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun deleteAnnouncement(
        communityId: String,
        announcementId: String
    ): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun voteAnnouncement(
        communityId: String,
        announcementId: String,
        approve: Boolean
    ): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun commentOnAnnouncement(
        communityId: String,
        announcementId: String,
        comment: String
    ): Result<Comment> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun getEvents(communityId: String): Result<List<Event>> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun createEvent(communityId: String, event: Event): Result<Event> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun updateEvent(communityId: String, event: Event): Result<Event> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun deleteEvent(communityId: String, eventId: String): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun respondToEvent(
        communityId: String,
        eventId: String,
        status: RsvpStatus
    ): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun commentOnEvent(
        communityId: String,
        eventId: String,
        comment: String
    ): Result<Comment> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun getProposals(communityId: String): Result<List<Proposal>> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun createProposal(communityId: String, proposal: Proposal): Result<Proposal> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun updateProposal(communityId: String, proposal: Proposal): Result<Proposal> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun deleteProposal(communityId: String, proposalId: String): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun voteOnProposal(
        communityId: String,
        proposalId: String,
        approve: Boolean
    ): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun commentOnProposal(
        communityId: String,
        proposalId: String,
        comment: String
    ): Result<Comment> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun createReport(report: Report): Result<Report> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun resolveReport(reportId: String, approved: Boolean): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun getReports(communityId: String): Result<List<Report>> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun updateNotificationPreferences(
        communityId: String,
        preferences: NotificationSubscription
    ): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun getNotificationPreferences(communityId: String): Result<NotificationSubscription> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }

    override suspend fun registerDeviceToken(token: String): Result<Boolean> {
        // Implementación pendiente
        return Result.failure(Exception("Método no implementado"))
    }
}