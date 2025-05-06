package com.vecinapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vecinapp.presentation.BottomNavigationBar
import com.vecinapp.ui.screen.LoginScreen
import com.vecinapp.ui.theme.VecinappTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(applicationContext)

        setContent {
            // Estado de preferencias
            val initDark = isSystemInDarkTheme()
            var darkMode by remember { mutableStateOf(initDark) }
            var dynamicColor by remember { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }
            var seniorMode by remember { mutableStateOf(false) }

            // Escucha continua de DataStore
            LaunchedEffect(Unit) {
                prefs.preferencesFlow.collectLatest { p ->
                    darkMode = p.darkMode
                    dynamicColor = p.dynamicColor
                    seniorMode = p.isSenior
                }
            }

            VecinappTheme(darkTheme = darkMode, dynamicColor = dynamicColor) {
                // NavController y usuario
                val navController = rememberNavController()
                var user by remember { mutableStateOf(Firebase.auth.currentUser) }

                // Listener de autenticación
                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose { Firebase.auth.removeAuthStateListener(listener) }
                }

                // Detectar ruta actual
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val isSettings = currentRoute == ScreenSettings.toRoute()
                val showMainBars = !(seniorMode && isSettings)

                Scaffold(topBar = {
                    if (showMainBars) {
                        TopAppBar(title = { Text("VecinApp") }, navigationIcon = {
                            Image(
                                painter = painterResource(R.drawable.icon_only),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                        })
                    }
                }, bottomBar = {
                    if (showMainBars && !seniorMode) {
                        BottomNavigationBar(navController, user)
                    }
                }) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (user == null) {
                            LoginScreen {
                                navController.navigate(ScreenDashboard)
                            }
                        } else {
                            VecinalNavHost(
                                navController = navController,
                                isSenior = seniorMode,
                                darkMode = darkMode,
                                dynamicColors = dynamicColor,
                                onSeniorChange = { value ->
                                    lifecycleScope.launch { prefs.updateIsSenior(value) }
                                },
                                onDarkChange = { value ->
                                    lifecycleScope.launch { prefs.updateDarkMode(value) }
                                },
                                onDynamicChange = { value ->
                                    lifecycleScope.launch { prefs.updateDynamicColor(value) }
                                },
                                onLoggedOut = {
                                    navController.popBackStack()
                                })
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CompositionLocalProvider {
        VecinappTheme(
            darkTheme = false,
            dynamicColor = false
        ) {
            LoginScreen {

            }
        }
    }
}


