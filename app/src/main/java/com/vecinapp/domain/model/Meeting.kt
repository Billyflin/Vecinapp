package com.vecinapp.domain.model

data class Meeting(
    val id: String = "",
    val date: Long = 0L,
    val location: String = "",
    val agenda: List<String> = emptyList(), // IDs de sugerencias
    val minutesUrl: String? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)
