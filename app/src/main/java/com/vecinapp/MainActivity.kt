package com.vecinapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.vecinapp.ui.theme.VecinappTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/* …imports… */
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(applicationContext)

        setContent {
            /* -------- estado de preferencias -------- */
            val initDark = isSystemInDarkTheme()
            var darkMode      by remember { mutableStateOf(initDark) }
            var dynamicColor  by remember { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }
            var seniorMode    by remember { mutableStateOf(false) }

            // Escucha continua del DataStore
            LaunchedEffect(Unit) {
                prefs.preferencesFlow.collectLatest {
                    darkMode     = it.darkMode
                    dynamicColor = it.dynamicColor
                    seniorMode   = it.isSenior
                }
            }

            VecinappTheme(darkTheme = darkMode, dynamicColor = dynamicColor) {

                val nav = rememberNavController()
                var user by remember { mutableStateOf(Firebase.auth.currentUser) }

                // Listener de autenticación
                DisposableEffect(Unit) {
                    val l = FirebaseAuth.AuthStateListener { user = it.currentUser }
                    Firebase.auth.addAuthStateListener(l)
                    onDispose { Firebase.auth.removeAuthStateListener(l) }
                }

                /* ------------- UI ------------- */
                if (user == null) {
                    LoginScreen { nav.navigate(ScreenDashboard) }
                } else {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("VecinApp") },
                                navigationIcon = {
                                    Image(
                                        painter = painterResource(R.drawable.icon_only),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.size(48.dp).padding(8.dp)
                                    )
                                }
                            )
                        },
                        bottomBar = { if (!seniorMode) BottomNavigationBar(nav, user) }
                    ) { inner ->
                        VecinalNavHost(
                            navController   = nav,
                            modifier        = Modifier.padding(inner),

                            /* ----------  preferencias  ---------- */
                            isSenior        = seniorMode,
                            darkMode        = darkMode,          //  ←  faltaba
                            dynamicColors   = dynamicColor,      //  ←  faltaba

                            /* ---------- callbacks  ---------- */
                            onSeniorChange  = { value ->
                                lifecycleScope.launch { prefs.updateIsSenior(value) }
                            },
                            onDarkChange    = { value ->
                                lifecycleScope.launch { prefs.updateDarkMode(value) }
                            },
                            onDynamicChange = { value ->
                                lifecycleScope.launch { prefs.updateDynamicColor(value) }
                            },

                            /* ---------- sesión  ---------- */
                            onLoggedOut     = {
                                nav.popBackStack(ScreenOnboarding, inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }
}
