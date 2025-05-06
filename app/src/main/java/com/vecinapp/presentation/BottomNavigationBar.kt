package com.vecinapp.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import com.vecinapp.ScreenAnuncios
import com.vecinapp.ScreenEventos
import com.vecinapp.ScreenSettings
import com.vecinapp.ScreenSugerencias
import kotlinx.coroutines.delay

@Composable
fun BottomNavigationBar(navController: NavHostController, user: FirebaseUser?) {
    val haptic = LocalHapticFeedback.current

    // Get current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Animation for the FAB
    var showFab by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showFab = true
    }

    val fabScale by animateFloatAsState(
        targetValue = if (showFab) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "fabScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main navigation bar
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 12.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                // Botón 1 - Chat Grupal
                NavBarItem(
                    icon = Icons.Rounded.Forum,
                    title = "Chat Grupal",
                    isSelected = currentRoute == ScreenAnuncios.toRoute(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate(ScreenAnuncios) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                // Botón 2 - Sugerencias
                NavBarItem(
                    icon = Icons.Default.Lightbulb,
                    title = "Sugerencias",
                    isSelected = currentRoute == ScreenSugerencias.toRoute(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate(ScreenSugerencias) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                // Espacio para el FAB
                Spacer(modifier = Modifier.width(64.dp))

                // Botón 3 - Eventos
                NavBarItem(
                    icon = Icons.Default.Event,
                    title = "Eventos",
                    isSelected = currentRoute == ScreenEventos.toRoute(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate(ScreenEventos) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                // Botón 4 - Cuenta (con foto de perfil si está disponible)
                if (user?.photoUrl != null) {
                    ProfileNavItem(
                        photoUrl = user.photoUrl.toString(),
                        title = "Cuenta",
                        isSelected = currentRoute == ScreenSettings.toRoute(),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(ScreenSettings) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    NavBarItem(
                        icon = Icons.Default.Person,
                        title = "Cuenta",
                        isSelected = currentRoute == ScreenSettings.toRoute(),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(ScreenSettings) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

        // Community FAB
        AnimatedVisibility(
            visible = showFab,
            enter = fadeIn(animationSpec = tween(500)) +
                    slideInVertically(
                        animationSpec = tween(500),
                        initialOffsetY = { it }
                    ),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        ) {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate(ScreenEventos) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .scale(fabScale)
            ) {
                Icon(
                    imageVector = Icons.Filled.Diversity3,
                    contentDescription = "Comunidad",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animations for selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    val indicatorHeight by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "indicatorHeight"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        Color.Transparent
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )

        // Selection indicator
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .width(20.dp)
                .height(indicatorHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun ProfileNavItem(
    photoUrl: String,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animations for selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    val indicatorHeight by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "indicatorHeight"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        Color.Transparent
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )

        // Selection indicator
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .width(20.dp)
                .height(indicatorHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Preview
@Composable
fun NavBarPreview() {
    MaterialTheme {
        BottomNavigationBar(rememberNavController(), null)
    }
}