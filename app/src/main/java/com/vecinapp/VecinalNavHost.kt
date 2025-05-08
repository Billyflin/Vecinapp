package com.vecinapp

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.toRoute
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.auth.AuthManager
import com.vecinapp.ui.screen.*
import kotlinx.coroutines.launch

@Composable
fun VecinalNavHost(
    navController: NavHostController = rememberNavController()
) {

    /* ────── 1.  Crear dependencias UNA sola vez ────── */
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }          // singleton en Composable
    val prefs = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    /* ────── 2.  Observar usuario actual ────── */
    val user by authManager.currentUser.collectAsState(null)

    /* ────── 3.  Router ────── */
    NavHost(
        navController = navController, startDestination = when {
            user == null -> ScreenLogin
            else -> ScreenDashboard
        },

        modifier = Modifier.fillMaxSize()
    ) {

        composable<ScreenLogin> {
            LoginScreen(authManager = authManager, onSignInSuccess = {
                navController.navigate(ScreenDashboard) {
                    popUpTo(ScreenLogin) { inclusive = true }
                }
            }, onProfileIncomplete = {
                navController.navigate(ScreenProfileCompletion) {
                    popUpTo(ScreenLogin) { inclusive = true }
                }
            })
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
                authManager = authManager,
                onComplete = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenRegisterPhone) { inclusive = true }
                    }
                })
        }


        /* Anuncios */
        composable<ScreenAnuncios> {
            AnunciosScreen()
        }


        /* Sugerencias, Tablón y Panel Directivo */
        composable<ScreenSugerencias> { SugerenciasListScreen() }
        composable<ScreenTablon> { TablonListScreen() }
        composable<ScreenPanel> { PanelDirectivoScreen() }

        /* Ajustes */
        composable<ScreenSettings> {
            SettingsScreen(
                prefs = prefs, onBack = { navController.popBackStack() })
        }
    }

    // You can use this showNavBar boolean to control the visibility of your navbar
    // in your main activity or wherever your navbar is defined
}