package com.vecinapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vecinapp.presentation.BottomNavigationBar
import com.vecinapp.ui.screen.LoginScreen
import com.vecinapp.ui.screen.OnboardingModeScreen
import com.vecinapp.ui.theme.VecinappTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(applicationContext)

        val initial = runBlocking { prefs.preferencesFlow.first() }



        setContent {
            var dynamicColor by remember { mutableStateOf(initial.dynamicColor) }
            var darkMode by remember { mutableStateOf(initial.darkMode) }
            var seniorMode by remember { mutableStateOf(initial.isSenior) }
            var isFirstTime by remember { mutableStateOf(initial.isFirstTime) }

            // Escucha continua de DataStore
            LaunchedEffect(Unit) {
                prefs.preferencesFlow.collectLatest { p ->
                    darkMode = p.darkMode
                    dynamicColor = p.dynamicColor
                    seniorMode = p.isSenior
                    isFirstTime = p.isFirstTime
                }
            }
            Log.e("dsada", isFirstTime.toString())
            VecinappTheme(darkTheme = darkMode, dynamicColor = dynamicColor) {
                val navController = rememberNavController()
                var user by remember { mutableStateOf(Firebase.auth.currentUser) }

                // Listener de auth
                DisposableEffect(Unit) {
                    val listener =
                        FirebaseAuth.AuthStateListener { auth -> user = auth.currentUser }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose { Firebase.auth.removeAuthStateListener(listener) }
                }

                // Sólo mostramos barra inferior si:
                // • no es modo senior
                // • ¡y! ya pasó el onboarding (isFirstTime == false)
                val showBottomBar = !seniorMode && !isFirstTime

                Scaffold(
                    topBar = {
                        if (!isFirstTime) {
                            TopAppBar(
                                title = { Text("VecinApp") },
                                navigationIcon = {
                                    Image(
                                        painter = painterResource(R.drawable.icon_only),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(8.dp)
                                    )
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController, user)
                        }
                    }
                ) { inner ->
                    Box(Modifier.padding(inner)) {
                        // 1) Si no hay user: login
                        if (user == null) {
                            LoginScreen {
                                // tras login por Google o SMS navegamos al onboarding o al dashboard si ya no es primera vez
                                if (isFirstTime) {
                                    navController.navigate(ScreenOnboarding)
                                } else {
                                    navController.navigate(ScreenDashboard)
                                }
                            }

                            // 2) Si hay user y es primera vez: onboarding
                        } else if (isFirstTime) {
                            OnboardingModeScreen(
                                onFirstTimeChange = { v ->
                                    lifecycleScope.launch { prefs.updateIsFirstTime(v) }
                                },
                                onSeniorChange = { v ->
                                    lifecycleScope.launch { prefs.updateIsSenior(v) }
                                },
                                onContinue = {
                                    // al pulsar continuar:
                                    lifecycleScope.launch { prefs.updateIsFirstTime(false) }
                                    navController.navigate(ScreenDashboard) {
                                        popUpTo(ScreenOnboarding) { inclusive = true }
                                    }
                                }
                            )

                            // 3) Ya pasó onboarding, mostramos el nav host normal
                        } else {
                            VecinalNavHost(
                                navController = navController,
                                modifier = Modifier.padding(inner),
                                isSenior = seniorMode,
                                darkMode = darkMode,
                                dynamicColors = dynamicColor,
                                isFirstTime = isFirstTime,
                                onSeniorChange = { v ->
                                    lifecycleScope.launch {
                                        prefs.updateIsSenior(
                                            v
                                        )
                                    }
                                },
                                onDarkChange = { v -> lifecycleScope.launch { prefs.updateDarkMode(v) } },
                                onDynamicChange = { v ->
                                    lifecycleScope.launch {
                                        prefs.updateDynamicColor(
                                            v
                                        )
                                    }
                                },
                                onFirstTimeChange = { v ->
                                    lifecycleScope.launch {
                                        prefs.updateIsFirstTime(
                                            v
                                        )
                                    }
                                },
                                onLoggedOut = {
                                    // al cerrar sesión, limpiamos el backstack y volvemos al login
                                    navController.popBackStack(ScreenOnboarding, inclusive = false)
                                    user = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
