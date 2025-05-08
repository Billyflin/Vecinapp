package com.vecinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vecinapp.auth.AuthManager
import com.vecinapp.ui.theme.VecinappTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


class MainActivity : ComponentActivity() {

    private val prefs = PreferencesManager(applicationContext)
    private val initial = runBlocking { prefs.preferencesFlow.first() }
    private val authManager = AuthManager(applicationContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var dynamicColor by remember { mutableStateOf(initial.dynamicColor) }
            var darkMode by remember { mutableStateOf(initial.darkMode) }
            var user by remember { mutableStateOf<FirebaseUser?>(null) }

            LaunchedEffect(Unit) {
                prefs.preferencesFlow.collectLatest { p ->
                    darkMode = p.darkMode
                    dynamicColor = p.dynamicColor
                }
            }
            LaunchedEffect(Unit) {
                authManager.currentUser.collect { currentUser ->
                    user = currentUser
                }
            }

            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { auth ->
                    user = auth.currentUser
                }
                Firebase.auth.addAuthStateListener(listener)
                onDispose { Firebase.auth.removeAuthStateListener(listener) }
            }


            VecinappTheme(darkTheme = darkMode, dynamicColor = dynamicColor) {
                VecinalNavHost()
            }
        }
    }
}

