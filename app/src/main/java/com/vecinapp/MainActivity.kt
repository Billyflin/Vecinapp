package com.vecinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vecinapp.presentation.BottomNavigationBar
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
            val navController = rememberNavController()

            var dynamicColor by remember { mutableStateOf(initial.dynamicColor) }
            var darkMode by remember { mutableStateOf(initial.darkMode) }
            var seniorMode by remember { mutableStateOf(initial.isSenior) }
            var isFirstTime by remember { mutableStateOf(initial.isFirstTime) }
            var user by remember { mutableStateOf(Firebase.auth.currentUser) }

            LaunchedEffect(Unit) {
                prefs.preferencesFlow.collectLatest { p ->
                    darkMode = p.darkMode
                    dynamicColor = p.dynamicColor
                    seniorMode = p.isSenior
                    isFirstTime = p.isFirstTime
                }
            }

            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { auth ->
                    user = auth.currentUser
                }
                Firebase.auth.addAuthStateListener(listener)
                onDispose { Firebase.auth.removeAuthStateListener(listener) }
            }


            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route


            VecinappTheme(darkTheme = darkMode, dynamicColor = dynamicColor) {
                Scaffold(topBar = {
                    if (!isFirstTime && user != null) {
                        TopAppBar(title = { Text("VecinApp") }, navigationIcon = {
                            Image(
                                painter = painterResource(R.drawable.icon_only),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            )
                        })
                    }
                }, bottomBar = {
                    if (!seniorMode && !isFirstTime && user != null) {
                        BottomNavigationBar(navController, user)
                    }
                }) { inner ->
                    Box(modifier = Modifier.padding(inner)) {
                        VecinalNavHost(
                            navController = navController,
                            isSenior = seniorMode,
                            darkMode = darkMode,
                            dynamicColors = dynamicColor,
                            isFirstTime = isFirstTime,
                            user = user,
                            onSeniorChange = { v ->
                                lifecycleScope.launch { prefs.updateIsSenior(v) }
                            },
                            onDarkChange = { v ->
                                lifecycleScope.launch { prefs.updateDarkMode(v) }
                            },
                            onDynamicChange = { v ->
                                lifecycleScope.launch { prefs.updateDynamicColor(v) }
                            },
                            onFirstTimeChange = { v ->
                                lifecycleScope.launch { prefs.updateIsFirstTime(v) }
                            },
                            onLoggedOut = {
                                navController.popBackStack(ScreenOnboarding, inclusive = false)
                                user = null
                            })
                    }
                }
            }
        }
    }
}
