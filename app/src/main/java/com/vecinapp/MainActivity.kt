package com.vecinapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vecinapp.presentation.BottomNavigationBar
import com.vecinapp.ui.theme.VecinappTheme
import com.vecinapp.ui.viewmodel.CommunityViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inyectamos el ViewModel monolítico con Hilt
    private val viewModel: CommunityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            // Observamos los estados desde el ViewModel monolítico
            val authState by viewModel.authState.collectAsState()
            val preferencesState by viewModel.preferencesState.collectAsState()

            // Determinamos si es la primera vez (podría ser una propiedad en preferencesState)
            val isFirstTime = false // Esto debería venir de preferencias

            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            // Usamos los valores del estado de la UI
            VecinappTheme(
                darkTheme = preferencesState.darkMode,
                dynamicColor = preferencesState.dynamicColors
            ) {
                Scaffold(
                    topBar = {
                        if (!isFirstTime && authState.user != null) {
                            TopAppBar(
                                title = { Text("VecinApp") },
                                navigationIcon = {
                                    Image(
                                        painter = painterResource(R.drawable.icon_only),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!preferencesState.seniorMode && !isFirstTime && authState.user != null) {
                            BottomNavigationBar(navController, authState.user)
                        }
                    }
                ) { inner ->
                    Box(modifier = Modifier.padding(inner)) {
                        VecinalNavHost(
                            navController = navController,

                        )
                    }
                }
            }
        }
    }
}


@HiltAndroidApp
class VecinAppApplication : Application() {
    // Aquí puedes inicializar módulos globales si los necesitas
}
