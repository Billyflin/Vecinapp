package com.vecinapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.auth.AuthManager
import com.vecinapp.ui.screen.LoginScreen
import com.vecinapp.ui.screen.OtpVerificationScreen
import com.vecinapp.ui.screen.ProfileCompletionScreen
import com.vecinapp.ui.screen.RegisterScreenMobile
import com.vecinapp.ui.screen.SettingsScreen

@Composable
fun VecinalNavHost(
    navController: NavHostController = rememberNavController()
) {

    /* ────── 1.  Crear dependencias UNA sola vez ────── */
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }          // singleton en Composable
    val prefs = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


    /* ────── 2.  Observar usuario actual ────── */
    val user by authManager.currentUser.collectAsState(null)


    /* profile se observa solo cuando hay uid */
    val profile by user?.uid?.let { uid ->
        authManager.profile(uid).collectAsState(null)
    } ?: remember { mutableStateOf(null) }

    val start = remember(user, profile) {
        when {
            user == null -> ScreenLogin
            !authManager.isPhoneLinked() -> ScreenRegisterPhone
            profile?.isProfileComplete != true -> ScreenProfileCompletion
            else -> ScreenDashboard
        }
    }

    /* ────── 3.  Router ────── */
    NavHost(
        navController = navController, startDestination = start,

        modifier = Modifier.fillMaxSize()
    ) {

        composable<ScreenLogin> {
            LoginScreen(
                authManager = authManager,
                snackbarHostState = snackbarHostState,
                onSignInSuccess = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenLogin) { inclusive = true }
                    }
                },
                onProfileIncomplete = {
                    navController.navigate(ScreenProfileCompletion) {
                        popUpTo(ScreenLogin) { inclusive = true }
                    }
                },
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
                    })
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
                    })
            }
        }

        /* Completar datos de perfil tras OTP */
        composable<ScreenProfileCompletion> {
            ProfileCompletionScreen(
                authManager = authManager, onComplete = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenRegisterPhone) { inclusive = true }
                    }
                })
        }

        composable<ScreenDashboard> {
            Text(text = "Dashboard")
            Button(onClick = {
                authManager.signOut()
                navController.navigate(ScreenLogin) {
                    popUpTo(ScreenDashboard) { inclusive = true }
                }
            }) {

            }
        }


        /* Anuncios */
        composable<ScreenAnuncios> {
//            AnunciosScreen()
        }


        /* Ajustes */
        composable<ScreenSettings> {
            SettingsScreen(
                prefs = prefs, onBack = { navController.popBackStack() })
        }
    }
}

