package com.vecinapp.domain.model

data class User(
    val uid: String = "",
    val phone: String = "",
    val name: String = "",
    val role: String = "VECINO", // VECINO, PRESIDENTE, SECRETARIO, TESORERO
    val communities: List<String> = emptyList(),
    val isApproved: Boolean = false,
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
