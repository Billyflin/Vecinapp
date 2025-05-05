package com.vecinapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
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
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vecinapp.ui.VecinalNavHost
import com.vecinapp.ui.theme.VecinappTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            val initColor = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(initColor) }
            var isDynamicColor by remember { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }

            LaunchedEffect(Unit) {
                Log.d("MainActivity", "LaunchedEffect: Loading user preferences")
                preferencesManager.preferencesFlow.collect { userPreferences ->
                    isDarkMode = userPreferences.darkMode
                    isDynamicColor = userPreferences.dynamicColor
                    Log.d("MainActivity", "User preferences loaded: darkMode=$isDarkMode, dynamicColor=$isDynamicColor")
                }
            }

            VecinappTheme(
                darkTheme = isDarkMode,
                dynamicColor = isDynamicColor
            ) {
                val navController = rememberNavController()
                val colorScheme = MaterialTheme.colorScheme
                var user by remember { mutableStateOf(Firebase.auth.currentUser) }
                DisposableEffect(Unit) {
                    Log.d("MainActivity", "DisposableEffect: Adding Firebase AuthStateListener")
                    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                        user = auth.currentUser
                        Log.d("MainActivity", "AuthStateListener: User state changed, user=${user?.uid}")
                    }
                    Firebase.auth.addAuthStateListener(authStateListener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(authStateListener)
                        Log.d("MainActivity", "DisposableEffect: AuthStateListener removed")
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {  }
                    },
                    topBar = {
                        TopAppBar(
                            title = { Text("VecinApp") },
                            navigationIcon = {
                                Image(
                                    painter = painterResource(id = R.drawable.icon_only),
                                    contentDescription = "Logo",
                                    colorFilter = ColorFilter.tint(colorScheme.primary),
                                    modifier = Modifier
                                        .size(150.dp)
                                        .padding(8.dp)
                                )
                            }
                        )
                    }) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        VecinalNavHost(
                            navController
                        )
                    }
                }
            }
        }
    }
}
