package com.vecinapp

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vecinapp.ui.screen.DashboardScreen
import com.vecinapp.ui.screen.LoginScreen
import com.vecinapp.ui.screen.ProfileCompletionScreen
import com.vecinapp.ui.viewmodel.MainViewModel
import kotlinx.serialization.Serializable

@Composable
fun VecinalNavHost(
    navController: NavHostController = rememberNavController()
) {
    val mainVM: MainViewModel = hiltViewModel(activity)

    val user = mainVM.authState.collectAsState().value.user
    Log.e("User", user.toString())

    NavHost(
        navController = navController, startDestination = when {
            user == null -> ScreenLogin
            else -> ScreenDashboard
        }, modifier = Modifier.fillMaxSize()
    ) {
        composable<ScreenLogin> {
            LoginScreen(
                onSuccess = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenLogin) { inclusive = true }
                    }
                },
                onProfileIncomplete = {
                    navController.navigate(ScreenProfileCompletion) {
                        popUpTo(ScreenLogin) { inclusive = true }
                    }
                }
            )
        }
        composable<ScreenProfileCompletion> {
            ProfileCompletionScreen(
                onDone = {
                    navController.navigate(ScreenDashboard) {
                        popUpTo(ScreenProfileCompletion) { inclusive = true }
                    }
                }
            )
        }

        composable<ScreenDashboard> {
            if (user != null) {
                DashboardScreen(
                    userName = user.name.ifBlank { "Vecino" },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(ScreenLogin) {
                            popUpTo(ScreenDashboard) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}




interface Screen {
    /**
     * Por defecto usa el nombre cualificado de la clase
     */
    fun toRoute(): String = this::class.qualifiedName!!
}

@Serializable
object ScreenLogin : Screen

@Serializable
object ScreenDashboard : Screen

@Serializable
object ScreenProfileCompletion : Screen


@Serializable
object ScreenSettings : Screen

@Serializable
data class ScreenEventoDetail(val eventId: String) : Screen {
    override fun toRoute(): String = "${this::class.qualifiedName}/$eventId"
}