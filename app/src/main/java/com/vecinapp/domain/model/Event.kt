package com.vecinapp.domain.model

import com.google.firebase.firestore.GeoPoint

data class Event(
    val id: String = "",
    val title: String = "",
    val date: Long = 0L,
    val timeStart: String = "",
    val timeEnd: String = "",
    val location: String = "",
    val geoPoint: GeoPoint? = null,
    val organizerName: String = "",
    val organizerPhone: String = "",
    val details: String = "",
    val photoUrl: String? = null,
    val repeat: String? = null, // NONE, WEEKLY, MONTHLY
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
