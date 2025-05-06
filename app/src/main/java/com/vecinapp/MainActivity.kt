package com.vecinapp

// Rutas serializables
import android.os.Bundle
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

        // 1️⃣ Leemos sincrónicamente la primera emisión de DataStore
        val prefs = PreferencesManager(applicationContext)
        val initial = runBlocking { prefs.preferencesFlow.first() }

        setContent {
            // 2️⃣ Inicializamos estados a partir de esos valores
            var darkMode by remember { mutableStateOf(initial.darkMode) }
            var dynamicColor by remember { mutableStateOf(initial.dynamicColor) }
            var seniorMode by remember { mutableStateOf(initial.isSenior) }
            var isFirstTime by remember { mutableStateOf(initial.isFirstTime) }

            // 3️⃣ Seguimos escuchando DataStore para cambios posteriores
            LaunchedEffect(Unit) {
                prefs.preferencesFlow.collectLatest { p ->
                    darkMode = p.darkMode
                    dynamicColor = p.dynamicColor
                    seniorMode = p.isSenior
                    isFirstTime = p.isFirstTime
                }
            }

            VecinappTheme(darkTheme = darkMode, dynamicColor = dynamicColor) {
                val navController = rememberNavController()
                var user by remember { mutableStateOf(Firebase.auth.currentUser) }

                // Listener de autenticación en FirebaseAuth
                DisposableEffect(Unit) {
                    val listener =
                        FirebaseAuth.AuthStateListener { auth -> user = auth.currentUser }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose { Firebase.auth.removeAuthStateListener(listener) }
                }

                // Determinamos si mostramos la barra inferior
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
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        when {
                            // 1) Sin sesión: login
                            user == null -> {
                                LoginScreen {
                                    // Después del login, vamos a onboarding o dashboard
                                    if (isFirstTime) {
                                        navController.navigate(ScreenOnboarding)
                                    } else {
                                        navController.navigate(ScreenDashboard)
                                    }
                                }
                            }
                            // 2) Primera vez tras login: onboarding
                            isFirstTime -> {
                                OnboardingModeScreen(
                                    onFirstTimeChange = { v ->
                                        lifecycleScope.launch {
                                            prefs.updateIsFirstTime(v)
                                        }
                                        isFirstTime = v
                                    },
                                    onSeniorChange = { v ->
                                        lifecycleScope.launch {
                                            prefs.updateIsSenior(v)
                                        }
                                        seniorMode = v
                                    },
                                    onContinue = {
                                        lifecycleScope.launch {
                                            prefs.updateIsFirstTime(false)
                                        }
                                        isFirstTime = false
                                        navController.navigate(ScreenDashboard) {
                                            popUpTo(ScreenOnboarding) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            // 3) Modo principal
                            else -> {
                                VecinalNavHost(
                                    navController = navController,
                                    modifier = Modifier.padding(innerPadding),
                                    isSenior = seniorMode,
                                    darkMode = darkMode,
                                    dynamicColors = dynamicColor,
                                    isFirstTime = isFirstTime,
                                    onSeniorChange = { v ->
                                        lifecycleScope.launch { prefs.updateIsSenior(v) }
                                        seniorMode = v
                                    },
                                    onDarkChange = { v ->
                                        lifecycleScope.launch { prefs.updateDarkMode(v) }
                                        darkMode = v
                                    },
                                    onDynamicChange = { v ->
                                        lifecycleScope.launch { prefs.updateDynamicColor(v) }
                                        dynamicColor = v
                                    },
                                    onFirstTimeChange = { v ->
                                        lifecycleScope.launch { prefs.updateIsFirstTime(v) }
                                        isFirstTime = v
                                    },
                                    onLoggedOut = {
                                        // Al cerrar sesión, limpiamos y volvemos al login
                                        lifecycleScope.launch { prefs.updateIsFirstTime(true) }
                                        Firebase.auth.signOut()
                                        user = null
                                        navController.popBackStack(
                                            ScreenOnboarding,
                                            inclusive = false
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
