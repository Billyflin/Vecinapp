// file: com/vecinapp/ui/screen/HomeScreen.kt
package com.vecinapp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.vecinapp.auth.AuthManager
import com.vecinapp.auth.UserProfile
import com.vecinapp.domain.model.Community
import com.vecinapp.domain.model.Event
import com.vecinapp.domain.model.Proposal
import com.vecinapp.domain.model.RsvpStatus
import com.vecinapp.domain.model.Status
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authManager: AuthManager,
    onNavigateToEvents: () -> Unit = {},
    onNavigateToCommunities: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToEventDetails: (String) -> Unit = {},
    onNavigateToCommunityDetails: (String) -> Unit = {},
    onNavigateToProposalDetails: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Datos de ejemplo
    val upcomingEvents = createMockEvents(4)
    val userCommunities = createMockCommunities(3)
    val pendingProposals = createMockProposals(2)
    val recentNotifications = createMockNotifications(5)

    // Estado para controlar si se muestra el saludo
    var showGreeting by remember { mutableStateOf(false) }

    // Mostrar el saludo después de un breve retraso para una animación agradable
    LaunchedEffect(Unit) {
        showGreeting = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VecinApp",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Notificaciones
                    BadgedBox(
                        badge = {
                            if (recentNotifications.any { !it.read }) {
                                Badge {
                                    Text(
                                        text = recentNotifications.count { !it.read }.toString()
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notificaciones"
                            )
                        }
                    }

                    // Perfil
                    IconButton(onClick = onNavigateToProfile) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Saludo animado
            item {
                AnimatedVisibility(
                    visible = showGreeting,
                    enter = fadeIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {

                        // Obtener el usuario actual
                        val firebaseUserState =
                            authManager.currentUser.collectAsState(initial = null)
                        val firebaseUser = firebaseUserState.value

// Obtener el perfil del usuario si el uid es válido
                        val profileState = if (firebaseUser != null) {
                            authManager.profile.collectAsState(initial = null)
                        } else {
                            remember { mutableStateOf<UserProfile?>(null) }
                        }
                        val profile = profileState.value

// Mostrar nombre del usuario si está disponible, o "Vecino" por defecto
                        val nombre = profile?.displayName ?: "Vecino"

                        Text(
                            text = "¡Hola, $nombre!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Aquí tienes un resumen de lo más importante",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Accesos rápidos
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Accesos rápidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickAccessButton(
                            icon = Icons.Default.Event,
                            label = "Eventos",
                            onClick = onNavigateToEvents
                        )

                        QuickAccessButton(
                            icon = Icons.Default.Group,
                            label = "Comunidades",
                            onClick = onNavigateToCommunities
                        )

                        QuickAccessButton(
                            icon = Icons.Default.HowToVote,
                            label = "Votaciones",
                            onClick = { /* Navegar a votaciones */ }
                        )

                        QuickAccessButton(
                            icon = Icons.Default.Settings,
                            label = "Ajustes",
                            onClick = onNavigateToSettings
                        )
                    }
                }
            }

            // Próximos eventos
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    SectionHeader(
                        title = "Próximos eventos",
                        actionText = "Ver todos",
                        onActionClick = onNavigateToEvents
                    )

                    if (upcomingEvents.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Default.Event,
                            message = "No tienes eventos próximos",
                            actionText = "Explorar eventos",
                            onActionClick = onNavigateToEvents
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(upcomingEvents) { event ->
                                EventCard(
                                    event = event,
                                    onClick = { onNavigateToEventDetails(event.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Mis comunidades
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    SectionHeader(
                        title = "Mis comunidades",
                        actionText = "Ver todas",
                        onActionClick = onNavigateToCommunities
                    )

                    if (userCommunities.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Default.Group,
                            message = "No perteneces a ninguna comunidad",
                            actionText = "Unirse a comunidades",
                            onActionClick = onNavigateToCommunities
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(userCommunities) { community ->
                                CommunityCard(
                                    community = community,
                                    onClick = { onNavigateToCommunityDetails(community.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Propuestas pendientes
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    SectionHeader(
                        title = "Propuestas pendientes",
                        actionText = "Ver todas",
                        onActionClick = { /* Navegar a todas las propuestas */ }
                    )

                    if (pendingProposals.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Default.HowToVote,
                            message = "No hay propuestas pendientes",
                            actionText = "Crear propuesta",
                            onActionClick = { /* Navegar a crear propuesta */ }
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pendingProposals.forEach { proposal ->
                                ProposalCard(
                                    proposal = proposal,
                                    onClick = { onNavigateToProposalDetails(proposal.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Actividad reciente
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    SectionHeader(
                        title = "Actividad reciente",
                        actionText = "Ver todo",
                        onActionClick = onNavigateToNotifications
                    )

                    if (recentNotifications.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Default.Notifications,
                            message = "No hay actividad reciente",
                            actionText = null,
                            onActionClick = {}
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentNotifications.take(3).forEach { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onClick = {
                                        // Navegar según el tipo de notificación
                                        when (notification.type) {
                                            NotificationType.EVENT -> onNavigateToEventDetails(
                                                notification.relatedId ?: ""
                                            )

                                            NotificationType.COMMUNITY -> onNavigateToCommunityDetails(
                                                notification.relatedId ?: ""
                                            )

                                            NotificationType.PROPOSAL -> onNavigateToProposalDetails(
                                                notification.relatedId ?: ""
                                            )

                                            else -> {}
                                        }
                                    }
                                )
                            }

                            if (recentNotifications.size > 3) {
                                TextButton(
                                    onClick = onNavigateToNotifications,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Ver más notificaciones")
                                }
                            }
                        }
                    }
                }
            }

            // Botón de cerrar sesión
            item {
                Button(
                    onClick = {
                        authManager.signOut()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun QuickAccessButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        TextButton(onClick = onActionClick) {
            Text(actionText)
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Imagen del evento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            event.imageUrl
                                ?: "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?q=80&w=2669&auto=format&fit=crop"
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay para mejorar legibilidad
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // Fecha en formato de chip
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatDate(event.startDate?.toDate()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Estado de RSVP si existe
                val isGoing =
                    event.rsvps.any { it.userId == "user123" && it.status == RsvpStatus.GOING }
                if (isGoing) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Asistiré",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Contenido del evento
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Fecha y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    val time = SimpleDateFormat(
                        "HH:mm",
                        Locale("es", "ES")
                    ).format(event.startDate?.toDate())
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Ubicación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Ubicación del evento",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Asistentes
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${event.rsvps.count { it.status == RsvpStatus.GOING }} asistentes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityCard(
    community: Community,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Imagen de la comunidad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(community.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = community.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay para mejorar legibilidad
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // Nombre de la comunidad
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            // Estadísticas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Miembros
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${community.members.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Miembros",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp
                    )
                }

                // Eventos
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${community.events.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Eventos",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp
                    )
                }

                // Propuestas
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${community.proposals.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Propuestas",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProposalCard(
    proposal: Proposal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título y comunidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = proposal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Chip de estado
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "En votación",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = proposal.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progreso de la votación
            val votesInFavor = proposal.votes.count { it.approve }
            val totalVotes = proposal.votes.size
            val progress = if (totalVotes > 0) votesInFavor.toFloat() / totalVotes else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "A favor: $votesInFavor",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Total: $totalVotes votos",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha límite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Finaliza: ${formatDate(proposal.expiresAt?.toDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según el tipo de notificación
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when (notification.type) {
                        NotificationType.EVENT -> MaterialTheme.colorScheme.primaryContainer
                        NotificationType.COMMUNITY -> MaterialTheme.colorScheme.secondaryContainer
                        NotificationType.PROPOSAL -> MaterialTheme.colorScheme.tertiaryContainer
                        NotificationType.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (notification.type) {
                    NotificationType.EVENT -> Icons.Default.Event
                    NotificationType.COMMUNITY -> Icons.Default.Group
                    NotificationType.PROPOSAL -> Icons.Default.HowToVote
                    NotificationType.SYSTEM -> Icons.Default.Notifications
                },
                contentDescription = null,
                tint = when (notification.type) {
                    NotificationType.EVENT -> MaterialTheme.colorScheme.onPrimaryContainer
                    NotificationType.COMMUNITY -> MaterialTheme.colorScheme.onSecondaryContainer
                    NotificationType.PROPOSAL -> MaterialTheme.colorScheme.onTertiaryContainer
                    NotificationType.SYSTEM -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Contenido de la notificación
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Tiempo
        Text(
            text = formatTimeAgo(notification.timestamp?.toDate()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Indicador de no leído
        if (!notification.read) {
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    actionText: String?,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            if (actionText != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onActionClick
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

// Funciones de utilidad

private fun formatDate(date: Date?): String {
    if (date == null) return "Fecha desconocida"
    val formatter = SimpleDateFormat("dd MMM", Locale("es", "ES"))
    return formatter.format(date)
}

private fun formatTimeAgo(date: Date?): String {
    if (date == null) return ""

    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInMinutes = diffInMillis / (60 * 1000)
    val diffInHours = diffInMillis / (60 * 60 * 1000)
    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

    return when {
        diffInMinutes < 1 -> "Ahora"
        diffInMinutes < 60 -> "${diffInMinutes}m"
        diffInHours < 24 -> "${diffInHours}h"
        diffInDays < 7 -> "${diffInDays}d"
        else -> formatDate(date)
    }
}

// Funciones para crear datos de ejemplo

private fun createMockEvents(count: Int): List<Event> {
    val calendar = Calendar.getInstance()
    val now = Date()
    val events = mutableListOf<Event>()

    val eventTitles = listOf(
        "Reunión de vecinos",
        "Limpieza comunitaria",
        "Fiesta de vecinos",
        "Taller de jardinería",
        "Asamblea general",
        "Actividad deportiva"
    )

    for (i in 0 until count) {
        // Eventos futuros
        calendar.time = now
        calendar.add(Calendar.DAY_OF_MONTH, i + 1)
        calendar.set(Calendar.HOUR_OF_DAY, 18 + (i % 3))
        calendar.set(Calendar.MINUTE, 0)

        val startDate = calendar.time
        calendar.add(Calendar.HOUR_OF_DAY, 2)
        val endDate = calendar.time

        val titleIndex = i % eventTitles.size

        events.add(
            Event(
                id = "event$i",
                title = eventTitles[titleIndex],
                description = "Descripción del evento ${i + 1}",
                dateTime = Timestamp(startDate),
                location = null,
                imageUrl = when (i % 4) {
                    0 -> "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?q=80&w=2574&auto=format&fit=crop"
                    1 -> "https://images.unsplash.com/photo-1617450365226-9bf28c04e130?q=80&w=2670&auto=format&fit=crop"
                    2 -> "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?q=80&w=2670&auto=format&fit=crop"
                    else -> "https://images.unsplash.com/photo-1599685315640-4a9ba2613f46?q=80&w=2670&auto=format&fit=crop"
                },
                organizerId = "user${100 + i}",
                createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 7)),
                updatedAt = Timestamp(now),
                attachments = emptyList(),
                comments = emptyList(),
                rsvps = createMockRsvps(5 + i),
                reports = emptyList(),
                voteCount = (5..15).random(),
                attendees = emptyList(),
                startDate = Timestamp(startDate),
                endDate = Timestamp(endDate)
            )
        )
    }

    return events
}

private fun createMockRsvps(count: Int): List<com.vecinapp.domain.model.Rsvp> {
    val rsvps = mutableListOf<com.vecinapp.domain.model.Rsvp>()
    val now = Date()

    // Asegurar que el usuario actual tenga un RSVP
    rsvps.add(
        com.vecinapp.domain.model.Rsvp(
            userId = "user123",
            status = RsvpStatus.GOING,
            respondedAt = Timestamp(now)
        )
    )

    // Añadir otros RSVPs
    for (i in 1 until count) {
        val status = when (i % 3) {
            0 -> RsvpStatus.GOING
            1 -> RsvpStatus.MAYBE
            else -> RsvpStatus.DECLINED
        }

        rsvps.add(
            com.vecinapp.domain.model.Rsvp(
                userId = "user${200 + i}",
                status = status,
                respondedAt = Timestamp(Date(now.time - 1000 * 60 * 60 * i))
            )
        )
    }

    return rsvps
}

private fun createMockCommunities(count: Int): List<Community> {
    val communities = mutableListOf<Community>()
    val now = Date()

    val communityNames = listOf(
        "Edificio Alameda",
        "Condominio Las Palmas",
        "Barrio El Bosque",
        "Plaza Central",
        "Edificio Mirador"
    )

    val communityImages = listOf(
        "https://images.unsplash.com/photo-1560518883-ce09059eeffa?q=80&w=2573&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?q=80&w=2835&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1625602812206-5ec545ca1231?q=80&w=2670&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1444653389962-8149286c578a?q=80&w=2574&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1460317442991-0ec209397118?q=80&w=2670&auto=format&fit=crop"
    )

    for (i in 0 until count) {
        communities.add(
            Community(
                id = "community$i",
                name = communityNames[i % communityNames.size],
                description = "Comunidad de vecinos ${i + 1}",
                creatorId = "user${100 + i}",
                createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 365)),
                updatedAt = Timestamp(now),
                members = createMockMembers(5 + i),
                directive = emptyList(),
                events = createMockEvents(2),
                proposals = createMockProposals(2),
                address = "Dirección ${i + 1}",
                isPublic = true,
                imageUrl = communityImages[i % communityImages.size]
            )
        )
    }

    return communities
}

private fun createMockMembers(count: Int): List<com.vecinapp.domain.model.Membership> {
    val members = mutableListOf<com.vecinapp.domain.model.Membership>()
    val now = Date()

    // Asegurar que el usuario actual sea miembro
    members.add(
        com.vecinapp.domain.model.Membership(
            userId = "user123",
            role = com.vecinapp.domain.model.Role.MEMBER,
            joinedAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 30))
        )
    )

    // Añadir otros miembros
    for (i in 1 until count) {
        members.add(
            com.vecinapp.domain.model.Membership(
                userId = "user${300 + i}",
                role = com.vecinapp.domain.model.Role.MEMBER,
                joinedAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * (30 + i)))
            )
        )
    }

    return members
}

private fun createMockProposals(count: Int): List<Proposal> {
    val proposals = mutableListOf<Proposal>()
    val now = Date()
    val calendar = Calendar.getInstance()

    val proposalTitles = listOf(
        "Instalación de cámaras de seguridad",
        "Renovación de áreas verdes",
        "Cambio de horario de reuniones",
        "Organización de evento comunitario",
        "Mejora del sistema de reciclaje"
    )

    for (i in 0 until count) {
        // Fecha de expiración futura
        calendar.time = now
        calendar.add(Calendar.DAY_OF_MONTH, 10 + i)

        // Crear votos
        val votes = mutableListOf<com.vecinapp.domain.model.Vote>()
        val voteCount = (3..8).random()

        // Asegurar que el usuario actual tenga un voto
        votes.add(
            com.vecinapp.domain.model.Vote(
                userId = "user123",
                votedAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24)),
                approve = i % 2 == 0 // Alternar entre aprobar y rechazar
            )
        )

        // Añadir otros votos
        for (j in 1 until voteCount) {
            votes.add(
                com.vecinapp.domain.model.Vote(
                    userId = "user${300 + j}",
                    votedAt = Timestamp(Date(now.time - 1000 * 60 * 60 * (24 - j))),
                    approve = j % 3 != 0 // Mayoría a favor
                )
            )
        }

        proposals.add(
            Proposal(
                id = "proposal$i",
                title = proposalTitles[i % proposalTitles.size],
                description = "Propuesta para mejorar nuestra comunidad. Esta propuesta busca implementar cambios que beneficien a todos los vecinos.",
                proposerId = "user${100 + i}",
                createdAt = Timestamp(Date(now.time - 1000 * 60 * 60 * 24 * 15)),
                updatedAt = Timestamp(now),
                attachments = emptyList(),
                comments = emptyList(),
                votes = votes,
                reports = emptyList(),
                voteCount = votes.size,
                expiresAt = Timestamp(calendar.time),
                status = Status.PENDING
            )
        )
    }

    return proposals
}

private fun createMockNotifications(count: Int): List<Notification> {
    val notifications = mutableListOf<Notification>()
    val now = Date()

    val notificationTitles = listOf(
        "Nuevo evento en tu comunidad",
        "Propuesta pendiente de votación",
        "Bienvenido a una nueva comunidad",
        "Recordatorio de evento",
        "Resultado de votación"
    )

    val notificationMessages = listOf(
        "Se ha creado un nuevo evento al que podrías estar interesado en asistir.",
        "Hay una nueva propuesta que requiere tu voto.",
        "Te has unido exitosamente a una nueva comunidad.",
        "El evento al que confirmaste asistencia comienza mañana.",
        "La propuesta en la que votaste ha sido aprobada."
    )

    val notificationTypes = listOf(
        NotificationType.EVENT,
        NotificationType.PROPOSAL,
        NotificationType.COMMUNITY,
        NotificationType.EVENT,
        NotificationType.PROPOSAL
    )

    for (i in 0 until count) {
        // Tiempo de la notificación (más reciente a más antigua)
        val timeOffset = i * (1 + Random.nextInt(5))
        val notificationTime = Date(now.time - 1000 * 60 * timeOffset)

        val typeIndex = i % notificationTypes.size

        notifications.add(
            Notification(
                id = "notification$i",
                userId = "user123",
                title = notificationTitles[i % notificationTitles.size],
                message = notificationMessages[i % notificationMessages.size],
                timestamp = Timestamp(notificationTime),
                read = i >= 2, // Primeras 2 no leídas
                type = notificationTypes[typeIndex],
                relatedId = when (notificationTypes[typeIndex]) {
                    NotificationType.EVENT -> "event${i % 4}"
                    NotificationType.COMMUNITY -> "community${i % 3}"
                    NotificationType.PROPOSAL -> "proposal${i % 2}"
                    else -> null
                }
            )
        )
    }

    return notifications
}

// Clase para representar notificaciones
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: Timestamp?,
    val read: Boolean,
    val type: NotificationType,
    val relatedId: String?
)

// Enum para tipos de notificaciones
enum class NotificationType {
    EVENT, COMMUNITY, PROPOSAL, SYSTEM
}
