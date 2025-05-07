package com.vecinapp.community

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vecinapp.domain.model.Community
import com.vecinapp.domain.model.Membership
import com.vecinapp.domain.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class CommunityManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Estado para las comunidades del usuario
    private val _userCommunities = MutableStateFlow<List<Community>?>(null)
    val userCommunities: StateFlow<List<Community>?> = _userCommunities.asStateFlow()

    // Estado para la carga de comunidades
    private val _isLoadingCommunities = MutableStateFlow(false)
    val isLoadingCommunities: StateFlow<Boolean> = _isLoadingCommunities.asStateFlow()

    // Estado para la creación de comunidades
    private val _isCreatingCommunity = MutableStateFlow(false)
    val isCreatingCommunity: StateFlow<Boolean> = _isCreatingCommunity.asStateFlow()

    // Estado para errores de creación
    private val _communityCreationError = MutableStateFlow<String?>(null)
    val communityCreationError: StateFlow<String?> = _communityCreationError.asStateFlow()

    // Estado para unirse a comunidades
    private val _isJoiningCommunity = MutableStateFlow(false)
    val isJoiningCommunity: StateFlow<Boolean> = _isJoiningCommunity.asStateFlow()

    // Estado para errores al unirse
    private val _communityJoinError = MutableStateFlow<String?>(null)
    val communityJoinError: StateFlow<String?> = _communityJoinError.asStateFlow()

    init {
        // Cargar comunidades del usuario cuando se inicializa
        auth.currentUser?.let { fetchUserCommunities() }

        // Escuchar cambios en la autenticación
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                fetchUserCommunities()
            } else {
                _userCommunities.value = null
            }
        }
    }

    /**
     * Obtiene las comunidades del usuario actual
     */
    fun fetchUserCommunities() {
        val userId = auth.currentUser?.uid ?: return

        _isLoadingCommunities.value = true

        db.collection("users")
            .document(userId)
            .collection("communities")
            .get()
            .addOnSuccessListener { documents ->
                val communityIds = documents.documents.mapNotNull { it.id }

                if (communityIds.isEmpty()) {
                    _userCommunities.value = emptyList()
                    _isLoadingCommunities.value = false
                    return@addOnSuccessListener
                }

                // Obtener detalles de cada comunidad
                db.collection("communities")
                    .whereIn("id", communityIds)
                    .get()
                    .addOnSuccessListener { communityDocs ->
                        val communities = communityDocs.mapNotNull { doc ->
                            doc.toObject(Community::class.java)
                        }
                        _userCommunities.value = communities
                        _isLoadingCommunities.value = false
                    }
                    .addOnFailureListener {
                        _communityJoinError.value = "Error al cargar comunidades: ${it.message}"
                        _isLoadingCommunities.value = false
                    }
            }
            .addOnFailureListener {
                _communityJoinError.value = "Error al cargar comunidades: ${it.message}"
                _isLoadingCommunities.value = false
            }
    }

    /**
     * Crea una nueva comunidad
     */
    suspend fun createCommunity(community: Community): Result<Community> {
        return try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")

            _isCreatingCommunity.value = true
            _communityCreationError.value = null

            // Crear la comunidad en Firestore
            val communityRef = db.collection("communities").document()
            val communityId = communityRef.id

            // Asignar ID y creador
            val newCommunity = community.copy(
                id = communityId,
                creatorId = userId,
                members = listOf(Membership(userId = userId, role = Role.MODERATOR))
            )

            // Guardar la comunidad
            communityRef.set(newCommunity).await()

            // Añadir la comunidad al usuario
            db.collection("users")
                .document(userId)
                .collection("communities")
                .document(communityId)
                .set(mapOf(
                    "role" to "admin",
                    "joinedAt" to System.currentTimeMillis()
                )).await()

            // Actualizar la lista de comunidades
            val updatedCommunities = (_userCommunities.value ?: emptyList()) + newCommunity
            _userCommunities.value = updatedCommunities

            _isCreatingCommunity.value = false
            Result.success(newCommunity)
        } catch (e: Exception) {
            _communityCreationError.value = "Error al crear comunidad: ${e.message}"
            _isCreatingCommunity.value = false
            Result.failure(e)
        }
    }

    /**
     * Une al usuario a una comunidad existente
     */
    suspend fun joinCommunity(community: Community): Result<Community> {
        return try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")

            _isJoiningCommunity.value = true
            _communityJoinError.value = null

            // Verificar si el usuario ya es miembro
            val userCommunityRef = db.collection("users")
                .document(userId)
                .collection("communities")
                .document(community.id)
                .get().await()

            if (userCommunityRef.exists()) {
                throw IllegalStateException("Ya eres miembro de esta comunidad")
            }

            // Añadir al usuario como miembro de la comunidad
            db.collection("communities")
                .document(community.id)
                .update("members", community.members + userId)
                .await()

            // Añadir la comunidad al usuario
            db.collection("users")
                .document(userId)
                .collection("communities")
                .document(community.id)
                .set(mapOf(
                    "role" to "member",
                    "joinedAt" to System.currentTimeMillis()
                )).await()

            // Actualizar la comunidad con el nuevo miembro
            val updatedCommunity = community.copy(
                members = community.members + Membership( userId = userId, role = Role.MEMBER)
            )

            // Actualizar la lista de comunidades
            val updatedCommunities = (_userCommunities.value ?: emptyList()) + updatedCommunity
            _userCommunities.value = updatedCommunities

            _isJoiningCommunity.value = false
            Result.success(updatedCommunity)
        } catch (e: Exception) {
            _communityJoinError.value = "Error al unirse a la comunidad: ${e.message}"
            _isJoiningCommunity.value = false
            Result.failure(e)
        }
    }

    /**
     * Busca comunidades por nombre o dirección
     */
    suspend fun searchCommunities(query: String): Result<List<Community>> {
        return try {
            if (query.length < 3) {
                return Result.success(emptyList())
            }

            val queryLower = query.lowercase()

            // Buscar por nombre
            val nameResults = db.collection("communities")
                .whereGreaterThanOrEqualTo("name", queryLower)
                .whereLessThanOrEqualTo("name", queryLower + "\uf8ff")
                .get().await()

            // Buscar por dirección
            val addressResults = db.collection("communities")
                .whereGreaterThanOrEqualTo("address", queryLower)
                .whereLessThanOrEqualTo("address", queryLower + "\uf8ff")
                .get().await()

            // Combinar resultados y eliminar duplicados
            val communities = (nameResults.documents + addressResults.documents)
                .distinctBy { it.id }
                .mapNotNull { it.toObject(Community::class.java) }

            Result.success(communities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia el escáner de códigos QR
     * Nota: Esta es una función de marcador de posición. La implementación real
     * dependerá de cómo quieras manejar el escaneo de QR en tu aplicación.
     */
    fun startQrCodeScanner() {
        // Implementación del escáner de QR
    }
}
