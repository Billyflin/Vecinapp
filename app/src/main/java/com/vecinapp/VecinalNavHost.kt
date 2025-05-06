package com.vecinapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.ui.screen.AnunciosScreen
import com.vecinapp.ui.screen.DashboardScreen
import com.vecinapp.ui.screen.EventDetailScreen
import com.vecinapp.ui.screen.EventosListScreen
import com.vecinapp.ui.screen.OnboardingModeScreen
import com.vecinapp.ui.screen.OtpVerificationScreen
import com.vecinapp.ui.screen.PanelDirectivoScreen
import com.vecinapp.ui.screen.ProfileCompletionScreen
import com.vecinapp.ui.screen.RegisterScreenMobile
import com.vecinapp.ui.screen.SettingsScreen
import com.vecinapp.ui.screen.SugerenciasListScreen
import com.vecinapp.ui.screen.TablonListScreen
import kotlinx.serialization.Serializable

@Composable
fun VecinalNavHost(
    /* NavController externo (para pruebas o previews) */
    navController: NavHostController = rememberNavController(),

    /* Layout modifier que llega desde el Scaffold */
    modifier: Modifier = Modifier.fillMaxSize(),

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

    /* Sesión */
    onLoggedOut: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = ScreenDashboard,
        modifier = modifier
    ) {

        composable<ScreenOnboarding> {
            OnboardingModeScreen(onFirstTimeChange = { first ->
                onFirstTimeChange(first)
            }, onSeniorChange = { senior ->
                onSeniorChange(senior)
            }, onContinue = {
                navController.navigate(ScreenRegisterPhone) {
                    popUpTo(ScreenOnboarding) { inclusive = true }
                }
            })
        }

        composable<ScreenRegisterPhone> {
            var verificationId by remember { mutableStateOf<String?>(null) }
            var resendToken by remember {
                mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(
                    null
                )
            }

            if (verificationId == null) {
                RegisterScreenMobile(
                    forceResendingToken = null, onVerificationSent = { id, token ->
                        verificationId = id
                        resendToken = token
                    })
            } else {
                OtpVerificationScreen(
                    verificationId = verificationId!!,
                    forceResendingToken = resendToken,
                    onVerified = {
                        navController.navigate(ScreenProfileCompletion) {
                            popUpTo(ScreenRegisterPhone) { inclusive = true }
                        }
                    },
                    onResend = {
                        // reenvío usando el token que guardamos
                        verificationId = null
                    })
            }
        }


        /* Completar datos de perfil tras OTP */
        composable<ScreenProfileCompletion> {
            ProfileCompletionScreen(
                onComplete = {
                    // una vez completado el perfil, vamos al dashboard
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenRegisterPhone) { inclusive = true }
                    }
                })
        }

        /* Dashboard (normal o senior) */
        composable<ScreenDashboard> {
            DashboardScreen(
                isSenior = isSenior, onNavigate = { dest -> navController.navigate(dest) })
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
                onBack = { navController.popBackStack() })
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
                onLinkPhone = { navController.navigate(ScreenRegisterPhone) },
                onBack = { navController.popBackStack() },
                onLoggedOut = onLoggedOut
            )
        }
    }
}


/* Rutas serializables */
@Serializable
object ScreenOnboarding

@Serializable
object ScreenRegisterPhone

@Serializable
object ScreenProfileCompletion   // ← nueva ruta

@Serializable
object ScreenDashboard

@Serializable
object ScreenAnuncios

@Serializable
object ScreenEventos

@Serializable
data class ScreenEventoDetail(val eventId: String)

@Serializable
object ScreenSugerencias

@Serializable
object ScreenTablon

@Serializable
object ScreenPanel

@Serializable
object ScreenSettings {
    fun toRoute(): String {
        return ("com.vecinapp.ScreenSettings")
    }
}
