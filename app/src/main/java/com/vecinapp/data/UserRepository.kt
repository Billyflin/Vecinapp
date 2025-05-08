package com.vecinapp.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.vecinapp.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val users = db.collection("users")

    private suspend fun ensureUserProfile(): User {
        val fUser = auth.currentUser ?: error("No auth user")
        val doc = users.document(fUser.uid).get().await()

        val base = User(
            id = fUser.uid,
            email = fUser.email ?: "",
            name = fUser.displayName ?: "",
            phone = fUser.phoneNumber,
            photoUrl = fUser.photoUrl?.toString()
        )

        return if (doc.exists()) {
            // merge para mantener nuevos campos
            doc.toObject(User::class.java)!!.copy(
                email = base.email,
                name = base.name,
                phone = base.phone,
                photoUrl = base.photoUrl
            ).also { users.document(fUser.uid).set(it, SetOptions.merge()) }
        } else {
            val nuevo = base.copy(createdAt = Timestamp.now())
            users.document(fUser.uid).set(nuevo).await()
            nuevo
        }
    }
}