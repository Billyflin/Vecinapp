package com.vecinapp


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.ui.screen.AnunciosScreen
import com.vecinapp.ui.screen.CreateComunityScreen
import com.vecinapp.ui.screen.DashboardScreen
import com.vecinapp.ui.screen.EventDetailScreen
import com.vecinapp.ui.screen.EventosListScreen
import com.vecinapp.ui.screen.JoinComunityScreen
import com.vecinapp.ui.screen.LoginScreen
import com.vecinapp.ui.screen.OtpVerificationScreen
import com.vecinapp.ui.screen.PanelDirectivoScreen
import com.vecinapp.ui.screen.ProfileCompletionScreen
import com.vecinapp.ui.screen.RegisterPhoneScreen
import com.vecinapp.ui.screen.RegisterScreenMobile
import com.vecinapp.ui.screen.SettingsScreen
import com.vecinapp.ui.screen.SugerenciasListScreen
import com.vecinapp.ui.screen.TablonListScreen
import com.vecinapp.ui.viewmodel.AuthViewModel
import com.vecinapp.ui.viewmodel.CommunityViewModel
import com.vecinapp.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun VecinalNavHost(
    navController: NavHostController = rememberNavController(),
    onLoggedOut: () -> Unit,
) {
    // Obtenemos el MainViewModel directamente en el NavHost
    val mainViewModel: MainViewModel = hiltViewModel()
    val uiState by mainViewModel.uiState.collectAsState()

    // Extraemos los valores del estado para mayor claridad
    val isSenior = uiState.isSenior
    val user = uiState.user

    NavHost(
        navController = navController,
        startDestination = when {
            user == null -> ScreenLogin
            else -> ScreenDashboard
        },
        modifier = Modifier.fillMaxSize()
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

        composable<ScreenRegisterPhone> {
            RegisterPhoneScreen(
                onVerified = {
                    navController.navigate(ScreenProfileCompletion) {
                        popUpTo(ScreenRegisterPhone) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable<ScreenProfileCompletion> {
            ProfileCompletionScreen(
                onComplete = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenRegisterPhone) { inclusive = true }
                    }
                }
            )
        }

        composable<ScreenDashboard> {
            DashboardScreen(
                isSenior = isSenior,
                onNavigate = { dest -> navController.navigate(dest) },
                onJoinCommunity = { navController.navigate(ScreenJoinComunity) },
                onCreateCommunity = { navController.navigate(ScreenCreateComunity) },
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

        composable<ScreenJoinComunity> {
            JoinComunityScreen(
                onNavigateBack = { navController.popBackStack() },
                onJoinSuccess = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenJoinComunity) { inclusive = true }
                    }
                }
            )
        }

        composable<ScreenAnuncios> {
            AnunciosScreen()
        }

        composable<ScreenEventos> {
            EventosListScreen {
                navController.navigate(ScreenEventoDetail(eventId = it.toString()))
            }
        }

        composable<ScreenEventoDetail> { backEntry ->
            val args = backEntry.toRoute<ScreenEventoDetail>()
            EventDetailScreen(
                eventId = args.eventId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ScreenSugerencias> { SugerenciasListScreen() }
        composable<ScreenTablon> { TablonListScreen() }
        composable<ScreenPanel> { PanelDirectivoScreen() }

        composable<ScreenSettings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    mainViewModel.signOut()
                    onLoggedOut()
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
    override fun toRoute(): String =
        "${this::class.qualifiedName}/$eventId"
}