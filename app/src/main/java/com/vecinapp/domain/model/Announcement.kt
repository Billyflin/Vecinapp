package com.vecinapp.domain.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "ANUNCIO", // ANUNCIO, ALERTA
    val photoUrl: String? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
