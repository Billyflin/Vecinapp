package com.vecinapp.domain.model

data class BoardPost(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val authorId: String = "",
    val authorName: String = "",
    val status: String = "PENDIENTE", // PENDIENTE, PUBLICADO, RECHAZADO
    val approvedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
