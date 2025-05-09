package com.vecinapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.auth.AuthManager
import com.vecinapp.auth.UserProfile
import com.vecinapp.presentation.BottomNavigationBar
import com.vecinapp.ui.screen.HomeScreen
import com.vecinapp.ui.screen.LoginScreen
import com.vecinapp.ui.screen.OtpVerificationScreen
import com.vecinapp.ui.screen.ProfileCompletionScreen
import com.vecinapp.ui.screen.RegisterScreenMobile
import com.vecinapp.ui.screen.SettingsScreen

@Composable
fun VecinalNavHost(
    navController: NavHostController = rememberNavController()
) {/* 1.  dependencias una sola vez */
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val prefs = remember { PreferencesManager(context) }
    val snackbar = remember { SnackbarHostState() }

    /* 2.  cálculo FINITO del destino -------------------- */


    val user by authManager.currentUser.collectAsState(null)

    // produceState ES suspend -> aquí sí se puede llamar a funciones suspend
    val profile by produceState<UserProfile?>(null, user?.uid) {
        value = user?.let { authManager.getUserProfile(it.uid) }      // 1 sola query
    }

    /* -------- 3. pantalla inicial -------- */
    val start = remember(user, profile) {
        when {
            user == null -> ScreenLogin/* user.phoneNumber == null      -> ScreenRegisterPhone  // si lo necesitas */
            profile?.isProfileComplete != true -> ScreenProfileCompletion
            else -> ScreenHome
        }
    }

    /* -------- 4. UI -------- */
    Scaffold(
        bottomBar = {
            profile?.takeIf { it.isProfileComplete }?.let { p ->
                BottomNavigationBar(
                    navController = navController, user = p
                )
            }
        },
    ) {


        /* 3.  NavHost fijo ---------------------------------- */
        NavHost(
            navController = navController,
            startDestination = start,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {

            composable<ScreenSplash> {
                ScreenSplash()
            }

            /* ------------------ Login ------------------ */
            composable<ScreenLogin> {
                LoginScreen(
                    authManager = authManager,
                    snackbarHostState = snackbar,
                    onSignInSuccess = {
                        navController.navigate(start) {
                            popUpTo(ScreenLogin) { inclusive = true }
                        }
                    },
                    onProfileIncomplete = {
                        navController.navigate(ScreenProfileCompletion) {
                            popUpTo(ScreenLogin) { inclusive = true }
                        }
                    },
                    onSmsVerification = {
                        navController.navigate(ScreenRegisterPhone)
                    })
            }

            /* -------------- Registro teléfono ---------- */
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

            /* -------------- Completar perfil ----------- */
            composable<ScreenProfileCompletion> {
                ProfileCompletionScreen(
                    authManager = authManager, onComplete = {
                        navController.navigate(ScreenHome) {
                            popUpTo(ScreenProfileCompletion) { inclusive = true }
                        }
                    })
            }

            /* ---------------- Home ---------------- */
            composable<ScreenHome> {
                HomeScreen(
                    authManager = authManager,
                    onBack = {
                        navController.navigate(ScreenLogin) {
                            popUpTo(ScreenHome) { inclusive = true }
                        }
                    }
                )
            }

            /*--------------- Notifications --------------- */
            composable<ScreenNotifications> {
                Text("Notifications")
            }


            /* -------------- Comunidades -------------- */
            composable<ScreenCommunities> {
                Text("Communities")
            }

            /* -------------- Eventos -------------- */
            composable<ScreenEventos> {
                Text("Eventos")
            }


            /* ----------- Configuraciones --------------- */
            composable<ScreenSettings> {
                SettingsScreen(
                    authManager,
                    prefs
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}


/* Pantalla de espera muy simple */
@Composable
private fun ScreenSplash() {
    Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
