package com.vecinapp

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
import com.vecinapp.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inyectamos el ViewModel con Hilt
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            // Observamos el estado de la UI desde el ViewModel
            val uiState by viewModel.uiState.collectAsState()

            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            // Usamos los valores del estado de la UI
            VecinappTheme(
                darkTheme = uiState.darkMode,
                dynamicColor = uiState.dynamicColor
            ) {
                Scaffold(
                    topBar = {
                        if (!uiState.isFirstTime && uiState.user != null) {
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
                        if ((uiState.isSenior == false) && !uiState.isFirstTime && uiState.user != null) {
                            BottomNavigationBar(navController, uiState.user)
                        }
                    }
                ) { inner ->
                    Box(modifier = Modifier.padding(inner)) {
                        // Pasamos el MainViewModel directamente al NavHost
                        VecinalNavHost(
                            navController = navController,
                            mainViewModel = viewModel,
                            onLoggedOut = {
                                navController.popBackStack(ScreenLogin, inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }
}