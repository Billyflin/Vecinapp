package com.vecinapp.domain.model

import com.google.firebase.firestore.GeoPoint

data class Community(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val location: GeoPoint? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val memberCount: Int = 0
)
