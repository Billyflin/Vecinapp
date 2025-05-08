package com.vecinapp

import kotlinx.serialization.Serializable

interface Screen {
    /**
     * Por defecto usa el nombre cualificado de la clase
     */
    fun toRoute(): String = this::class.qualifiedName!!
}


@Serializable
object ScreenLogin : Screen

@Serializable
object ScreenSplash : Screen

@Serializable
object ScreenRegisterPhone : Screen

@Serializable
object ScreenProfileCompletion : Screen

@Serializable
object ScreenDashboard : Screen

@Serializable
object ScreenAnuncios : Screen

@Serializable
object ScreenEventos : Screen

@Serializable
object ScreenSugerencias : Screen

@Serializable
object ScreenTablon : Screen

@Serializable
object ScreenPanel : Screen

@Serializable
object ScreenSettings : Screen

@Serializable
data class ScreenEventoDetail(val eventId: String) : Screen {
    override fun toRoute(): String =
        "${this::class.qualifiedName}/$eventId"
}