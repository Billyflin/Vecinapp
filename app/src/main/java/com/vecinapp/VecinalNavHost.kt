package com.vecinapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vecinapp.ui.AnunciosScreen
import com.vecinapp.ui.DashboardScreen
import com.vecinapp.ui.EventosListScreen
import com.vecinapp.ui.OnboardingModeScreen
import com.vecinapp.ui.PanelDirectivoScreen
import com.vecinapp.ui.SugerenciasListScreen
import com.vecinapp.ui.TablonListScreen
import com.vecinapp.ui.screen.EventDetailScreen
import com.vecinapp.ui.screen.SettingsScreen
import kotlinx.serialization.Serializable

/* -------------------------------------------------------------
 *  Navigation type‑safe con rutas @Serializable
 * ----------------------------------------------------------- */
@Composable
fun VecinalNavHost(
    /** NavController externo (para pruebas o previews) */
    navController: NavHostController = rememberNavController(),

    /** Layout modifier que llega desde el Scaffold */
    modifier: Modifier = Modifier,

    /* ---------- Preferencias actuales ---------- */
    isSenior: Boolean,
    darkMode: Boolean,
    dynamicColors: Boolean,

    /* ---------- Callbacks para actualizarlas (DataStore) ------ */
    onSeniorChange: suspend (Boolean) -> Unit,
    onDarkChange: suspend (Boolean) -> Unit,
    onDynamicChange: suspend (Boolean) -> Unit,

    /* ---------- Sesión ---------- */
    onLoggedOut: () -> Unit,
) {

    NavHost(
        navController = navController,
        startDestination = ScreenOnboarding,
        modifier = modifier
    ) {

        /* -------- Onboarding – elegir modo visual -------- */
        composable<ScreenOnboarding> {
            OnboardingModeScreen { senior ->
                // guardamos la elección y continuamos
                navController.navigate(ScreenDashboard) {
                    popUpTo(ScreenOnboarding) { inclusive = true }
                }
            }
        }


        /* --------------  Anuncios  ----------------------- */
        composable<ScreenAnuncios> { AnunciosScreen() }

        /* --------------  Eventos  ------------------------ */
        composable<ScreenEventos> {
            EventosListScreen {
                navController.navigate(ScreenEventoDetail(eventId = "1"))
            }
        }
        composable<ScreenEventoDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<ScreenEventoDetail>()
            EventDetailScreen(
                title = "Evento ${args.eventId}",
                dateTime = "24 MAY 13:00‑18:00",
                description = "Detalles del evento…",
                organizer = "Rosita",
                phone = "+56 9 1234 5678",
                lat = -33.45,
                lon = -70.66,
                isSenior = isSenior
            )
        }
        /* ---------- DASHBOARD (normal o senior) ---------------- */
        composable<ScreenDashboard> {
            DashboardScreen(
                isSenior = isSenior,
                onNavigate = { dest -> navController.navigate(dest) }
            )
        }


        /* -----------  Más módulos / stubs  --------------- */
        composable<ScreenSugerencias> { SugerenciasListScreen() }
        composable<ScreenTablon> { TablonListScreen() }
        composable<ScreenPanel> { PanelDirectivoScreen() }

        /* ---------------- Ajustes ------------------------ */
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
}


/* -------------------- Rutas serializables -------------------- */
@Serializable
object ScreenOnboarding
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
object ScreenSettings
