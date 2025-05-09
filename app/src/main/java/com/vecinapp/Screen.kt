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
object ScreenHome : Screen

@Serializable
object ScreenNotifications : Screen

@Serializable
object ScreenCommunities : Screen


@Serializable
object ScreenEventos : Screen

@Serializable
object ScreenSettings : Screen

@Serializable
data class ScreenEventoDetail(val eventId: String) : Screen {
    override fun toRoute(): String =
        "${this::class.qualifiedName}/$eventId"
}