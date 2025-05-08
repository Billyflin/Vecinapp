package com.vecinapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vecinapp.ui.screen.AnunciosScreen
import com.vecinapp.ui.screen.CreateComunityScreen
import com.vecinapp.ui.screen.LoginScreen
import com.vecinapp.ui.screen.PanelDirectivoScreen
import com.vecinapp.ui.screen.SettingsScreen
import com.vecinapp.ui.screen.TablonListScreen
import com.vecinapp.ui.viewmodel.CommunityViewModel
import kotlinx.serialization.Serializable

@Composable
fun VecinalNavHost(
    navController: NavHostController = rememberNavController(),
) {
    // Obtenemos el ViewModel monolítico
    val viewModel: CommunityViewModel = hiltViewModel()

    // Observamos los estados relevantes
    val authState by viewModel.authState.collectAsState()
    val preferencesState by viewModel.preferencesState.collectAsState()
    val communityState by viewModel.communityState.collectAsState()

    // Extraemos los valores del estado para mayor claridad
    val isSenior = preferencesState.seniorMode
    val user = authState.user

    NavHost(
        navController = navController, startDestination = when {
            user == null -> ScreenLogin
            else -> ScreenDashboard
        }, modifier = Modifier.fillMaxSize()
    ) {
        composable<ScreenLogin> {
            LoginScreen(
                onSignInSuccess = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenLogin) { inclusive = true }
                    }
                },
                onProfileIncomplete = {
                    navController.navigate(ScreenProfileCompletion) {
                        popUpTo(ScreenLogin) { inclusive = true }
                    }
                }
            )
        }

        composable<ScreenCreateComunity> {
            CreateComunityScreen(
                onNavigateBack = { navController.popBackStack() },
                onCommunityCreated = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenCreateComunity) { inclusive = true }
                    }
                }
            )
        }



        composable<ScreenAnuncios> {
            // Pasar el ID de la comunidad seleccionada
            val selectedCommunityId = communityState.selectedCommunity?.id ?: ""
            AnunciosScreen(
            )
        }





        composable<ScreenTablon> {
            TablonListScreen()
        }

        composable<ScreenPanel> {
            PanelDirectivoScreen()
        }

        composable<ScreenSettings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.popBackStack(ScreenLogin, inclusive = false)

                }
            )
        }
    }
}


interface Screen {
    /**
     * Por defecto usa el nombre cualificado de la clase
     */
    fun toRoute(): String = this::class.qualifiedName!!
}

@Serializable
object ScreenCreateComunity : Screen

@Serializable
object ScreenJoinComunity : Screen

@Serializable
object ScreenCreateEvent : Screen

@Serializable
object ScreenLogin : Screen

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
    override fun toRoute(): String = "${this::class.qualifiedName}/$eventId"
}