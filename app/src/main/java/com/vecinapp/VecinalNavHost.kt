package com.vecinapp

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.auth.AuthManager
import com.vecinapp.community.CommunityManager
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
import com.vecinapp.ui.screen.RegisterScreenMobile
import com.vecinapp.ui.screen.SettingsScreen
import com.vecinapp.ui.screen.SugerenciasListScreen
import com.vecinapp.ui.screen.TablonListScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun VecinalNavHost(
    navController: NavHostController = rememberNavController(),

    /* Preferencias actuales */
    isSenior: Boolean,
    darkMode: Boolean,
    dynamicColors: Boolean,
    isFirstTime: Boolean,

    /* Callbacks para actualizarlas (DataStore) */
    onSeniorChange: suspend (Boolean) -> Unit,
    onDarkChange: suspend (Boolean) -> Unit,
    onDynamicChange: suspend (Boolean) -> Unit,
    onFirstTimeChange: suspend (Boolean) -> Unit,

    user: Any?,

    /* Sesión */
    onLoggedOut: () -> Unit,
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val scope = rememberCoroutineScope()

    // Store the original senior mode value to restore after profile completion
    val originalSeniorMode = remember { isSenior }

    // Hide navbar during profile completion
    val showNavBar = remember(navController.currentDestination?.route) {
        navController.currentDestination?.route != ScreenProfileCompletion.toRoute()
    }

    val communityManager = remember { CommunityManager(context) }


    // Obtener estados del CommunityManager
    val userCommunities by communityManager.userCommunities.collectAsState()
    val isLoadingCommunities by communityManager.isLoadingCommunities.collectAsState()
    val isCreatingCommunity by communityManager.isCreatingCommunity.collectAsState()
    val communityCreationError by communityManager.communityCreationError.collectAsState()
    val isJoiningCommunity by communityManager.isJoiningCommunity.collectAsState()
    val communityJoinError by communityManager.communityJoinError.collectAsState()



    NavHost(
        navController = navController, startDestination = when {
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
            var verificationId by remember { mutableStateOf<String?>(null) }
            var resendToken by remember {
                mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null)
            }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            if (verificationId == null) {
                RegisterScreenMobile(
                    authManager = authManager,
                    forceResendingToken = resendToken,
                    onVerificationSent = { id, token ->
                        verificationId = id
                        resendToken = token
                    },
                    onError = { error ->
                        errorMessage = error
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            } else {
                OtpVerificationScreen(
                    authManager = authManager,
                    verificationId = verificationId!!,
                    forceResendingToken = resendToken,
                    onVerified = {
                        navController.navigate(ScreenProfileCompletion) {
                            popUpTo(ScreenRegisterPhone) { inclusive = true }
                        }
                    },
                    onResend = {
                        verificationId = null
                    },
                    onError = { error ->
                        errorMessage = error
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }

        /* Completar datos de perfil tras OTP */
        composable<ScreenProfileCompletion> {
            // No need to temporarily change the senior mode here
            // The ProfileCompletionScreen will handle it internally
            ProfileCompletionScreen(
                authManager = authManager,
                onSeniorChange = { newSeniorMode ->
                    // This will be called when the user completes the profile
                    scope.launch {
                        onSeniorChange(newSeniorMode)
                    }
                },
                onFirstTimeChange = onFirstTimeChange,
                onComplete = {
                    // Navigate to dashboard after profile completion
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenRegisterPhone) { inclusive = true }
                    }
                }
            )
        }

        /* Dashboard (normal o senior) */
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
                onCommunityCreated = { community ->
                    Log.d("CommunityManager", "Community created: $community")
                    // Usar el CommunityManager para crear la comunidad
                    CoroutineScope(Dispatchers.IO).launch {
                        communityManager.createCommunity(community).onSuccess {
                            // Navegar de vuelta al dashboard
                            navController.navigate(ScreenDashboard.toString()) {
                                popUpTo(ScreenCreateComunity.toString()) { inclusive = true }
                            }
                        }.onFailure { error ->
                            Log.e("CommunityManager", "Error creating community: $error")
                        }
                    }
                },
                isLoading = isCreatingCommunity,
                error = communityCreationError
            )
        }


        composable<ScreenJoinComunity> {
            JoinComunityScreen(
                onNavigateBack = { navController.popBackStack() },
                onJoinCommunity = { community ->
                    // Usar el CommunityManager para unirse a la comunidad
                    CoroutineScope(Dispatchers.IO).launch {

                        communityManager.joinCommunity(community).onSuccess {
                            // Navegar de vuelta al dashboard
                            navController.navigate(ScreenDashboard.toString()) {
                                popUpTo(ScreenJoinComunity.toString()) { inclusive = true }
                            }
                        }
                    }
                },
                onScanQrCode = {
                    communityManager.startQrCodeScanner()
                },
                isLoading = isJoiningCommunity,
                error = communityJoinError
            )
        }


        /* Anuncios */
        composable<ScreenAnuncios> {
            AnunciosScreen()
        }

        /* Eventos */
        composable<ScreenEventos> {
            EventosListScreen {
                navController.navigate(ScreenEventoDetail(eventId = it.toString()))
            }
        }

        composable<ScreenEventoDetail> { backEntry ->
            val args = backEntry.toRoute<ScreenEventoDetail>()
            EventDetailScreen(
                title = "Evento ${args.eventId}",
                dateTime = "24 MAY 13:00–18:00",
                description = "Detalles del evento…",
                organizer = "Rosita",
                phone = "+56 9 1234 5678",
                lat = -33.45,
                lon = -70.66,
                isSenior = isSenior,
                onBack = { navController.popBackStack() }
            )
        }

        /* Sugerencias, Tablón y Panel Directivo */
        composable<ScreenSugerencias> { SugerenciasListScreen() }
        composable<ScreenTablon> { TablonListScreen() }
        composable<ScreenPanel> { PanelDirectivoScreen() }

        /* Ajustes */
        composable<ScreenSettings> {
            SettingsScreen(
                isSenior = isSenior,
                darkMode = darkMode,
                dynamicColors = dynamicColors,
                onSeniorChange = onSeniorChange,
                onDarkChange = onDarkChange,
                onDynamicChange = onDynamicChange,
                onBack = { navController.popBackStack() },
                onLoggedOut = onLoggedOut
            )
        }
    }

    // You can use this showNavBar boolean to control the visibility of your navbar
    // in your main activity or wherever your navbar is defined
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