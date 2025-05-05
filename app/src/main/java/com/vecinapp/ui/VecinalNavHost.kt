package com.vecinapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil.compose.rememberAsyncImagePainter
import kotlinx.serialization.Serializable

/* -------------------------------------------------------------
 *  Tipo‑seguro Navigation con rutas @Serializable
 * ----------------------------------------------------------- */
@Composable
fun VecinalNavHost(navController: NavHostController = rememberNavController()) {

    NavHost(navController, startDestination = ScreenOnboarding) {

        composable<ScreenOnboarding> {
            OnboardingModeScreen { senior ->
                setSeniorMode(senior)
                navController.navigate(ScreenDashboard) {
                    popUpTo(ScreenOnboarding) { inclusive = true }
                }
            }
        }

        composable<ScreenDashboard> {
            DashboardSeniorScreen { destination -> navController.navigate(destination) }
        }

        /* ------------------ Módulo Anuncios ------------------ */
        composable<ScreenAnuncios> { AnunciosScreen() }

        /* ------------------ Módulo Eventos ------------------- */
        composable<ScreenEventos> {
            EventosListScreen { navController.navigate(ScreenEventoDetail(eventId = "1")) }
        }
        composable<ScreenEventoDetail> {
            val args = it.toRoute<ScreenEventoDetail>()
            EventDetailScreen(
                title = "Evento ${args.eventId}",
                dateTime = "24 MAY 13:00 – 18:00",
                description = "Detalles del evento…",
                organizer = "Rosita",
                phone = "+56912345678",
                lat = -33.45,
                lon = -70.66
            )
        }

        /* -------------- Sugerencias & otros ------------------ */
        composable<ScreenSugerencias> { SugerenciasListScreen() }
        composable<ScreenTablon>      { TablonListScreen()      }
        composable<ScreenPanel>       { PanelDirectivoScreen()  }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    title: String,
    dateTime: String,
    description: String,
    organizer: String,
    phone: String,
    lat: Double,
    lon: Double
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Imagen del evento
                Image(
                    painter = rememberAsyncImagePainter("https://picsum.photos/800/400"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            item {
                // Título y fecha
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp * Dimens.scale
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                        Text(
                            text = dateTime,
                            fontSize = 16.sp * Dimens.scale,
                            color = Gray700
                        )
                    }
                }
            }

            item {
                // Descripción
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp * Dimens.scale,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = description,
                            fontSize = 16.sp * Dimens.scale
                        )
                    }
                }
            }

            item {
                // Organizador
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Organizador",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp * Dimens.scale,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                            Text(
                                text = organizer,
                                fontSize = 16.sp * Dimens.scale
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Acción para llamar */ },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = PrimaryColor
                            )
                            Text(
                                text = phone,
                                fontSize = 16.sp * Dimens.scale,
                                color = PrimaryColor
                            )
                        }
                    }
                }
            }

            item {
                // Ubicación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ubicación",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp * Dimens.scale,
                            fontWeight = FontWeight.Bold
                        )

                        // Aquí iría un mapa, pero por simplicidad mostramos un placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Gray100, RoundedCornerShape(8.dp))
                                .clickable { /* Abrir mapa */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = PrimaryColor
                                )
                                Text(
                                    text = "Ver en mapa",
                                    fontSize = 16.sp * Dimens.scale,
                                    color = PrimaryColor
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutBtn(
                        label = "Compartir",
                        onClick = { /* Compartir evento */ },
                        modifier = Modifier.weight(1f)
                    )

                    FilledBtn(
                        label = "Asistiré",
                        onClick = { /* Marcar asistencia */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/* -------------------- Rutas serializables ------------------- */
@Serializable object ScreenOnboarding
@Serializable object ScreenDashboard
@Serializable object ScreenAnuncios
@Serializable object ScreenEventos
@Serializable data class ScreenEventoDetail(val eventId: String)
@Serializable object ScreenSugerencias
@Serializable object ScreenTablon
@Serializable object ScreenPanel