package com.vecinapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import com.vecinapp.ScreenSettings


@Composable
fun BottomNavigationBar(navController: NavHostController, user: FirebaseUser?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(146.dp)
            .padding(bottom = 70.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(percent = 20),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                // Botón 1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
//                        navController.navigate(ScreenMap) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = "Inicio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text("Inicio", style = MaterialTheme.typography.bodySmall)
                }

                // Botón 2
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
//                        navController.navigate(ScreenHistory) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
                    }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Historial",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text("Historial", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.width(30.dp))

                // Botón 3
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
//                        navController.navigate(ScreenRewards) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
                    }) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Recompensas",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text("Recompensas", style = MaterialTheme.typography.bodySmall)
                }

                // Botón 4
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        navController.navigate(ScreenSettings) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }) {
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(user.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentScale = ContentScale.Crop,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Text("Perfil", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        FloatingActionButton(
            onClick = {
//                navController.navigate(ScreenQrScanner)
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
        ) {
        }
    }
}



@Preview
@Composable
fun NavBarPreview() {
    BottomNavigationBar(rememberNavController(), null)
}