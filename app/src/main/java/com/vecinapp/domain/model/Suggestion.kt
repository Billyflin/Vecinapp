package com.vecinapp.domain.model

import com.google.firebase.firestore.GeoPoint

data class Suggestion(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val geoPoint: GeoPoint? = null,
    val supportCount: Int = 0,
    val status: String = "NUEVA", // NUEVA, EN_AGENDA, RESUELTA, REAGENDADA
    val meetingId: String? = null,
    val resolutionVote: Int = 0, // Positivos - Negativos
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
