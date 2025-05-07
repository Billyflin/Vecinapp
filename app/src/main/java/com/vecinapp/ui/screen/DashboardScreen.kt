package com.vecinapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.vecinapp.ScreenAnuncios
import com.vecinapp.ScreenEventos
import com.vecinapp.ScreenSettings
import com.vecinapp.ScreenSugerencias
import com.vecinapp.ScreenTablon
import com.vecinapp.domain.model.Announcement
import com.vecinapp.domain.model.Community
import com.vecinapp.domain.model.Event
import com.vecinapp.domain.model.Proposal
import com.vecinapp.ui.Dim
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    isSenior: Boolean,
    onNavigate: (Any) -> Unit,
    userCommunities: List<Community>? = null,
    isLoading: Boolean = false,
    onJoinCommunity: () -> Unit = {},
    onCreateCommunity: () -> Unit = {},
    onFloatingActionClick: () -> Unit = {}
) {
    if (isSenior) {
        SeniorDashboard(onNavigate)
    } else {
        StandardDashboard(
            userCommunities = userCommunities,
            isLoading = isLoading,
            onNavigate = onNavigate,
            onJoinCommunity = onJoinCommunity,
            onCreateCommunity = onCreateCommunity,
            onFloatingActionClick = onFloatingActionClick
        )
    }
}

/* === Dashboard modo SENIOR (Mejorado) ================================= */
@Composable
private fun SeniorDashboard(onNavigate: (Any) -> Unit) {
    val modules = listOf(
        Triple("Anuncios", Icons.Default.Notifications, ScreenAnuncios),
        Triple("Eventos", Icons.Default.Event, ScreenEventos),
        Triple("Sugerencias", Icons.Default.Lightbulb, ScreenSugerencias),
        Triple("Tablón", Icons.AutoMirrored.Filled.Chat, ScreenTablon)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dim.gap(true)),
        verticalArrangement = Arrangement.spacedBy(Dim.gap(true))
    ) {
        // Mensaje de bienvenida
        item {
            WelcomeCard(isSenior = true)
        }

        // Grid 2x2
        item {
            Grid2x2(modules) { onNavigate(it) }
        }

        // Tarjeta extra: Ajustes
        item {
            LargeCard(
                title = "Ajustes",
                icon = Icons.Default.Settings,
                description = "Configura tu perfil y preferencias",
                modifier = Modifier.fillMaxWidth()
            ) {
                onNavigate(ScreenSettings)
            }
        }

        // Ayuda y soporte
        item {
            HelpSupportCard()
        }

        // Espacio al final para evitar que el contenido quede oculto
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/* === Dashboard modo ESTÁNDAR (Mi Comunidad) ================================= */
@Composable
private fun StandardDashboard(
    userCommunities: List<Community>?,
    isLoading: Boolean,
    onNavigate: (Any) -> Unit,
    onJoinCommunity: () -> Unit,
    onCreateCommunity: () -> Unit,
    onFloatingActionClick: () -> Unit
) {
    var selectedCommunity by remember { mutableStateOf<Community?>(null) }

    // Seleccionar la primera comunidad por defecto si hay comunidades disponibles
    LaunchedEffect(userCommunities) {
        if (!userCommunities.isNullOrEmpty() && selectedCommunity == null) {
            selectedCommunity = userCommunities.first()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            // Mostrar indicador de carga
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (userCommunities.isNullOrEmpty()) {
            // Mostrar pantalla de "sin comunidades"
            NoCommunityView(onJoinCommunity, onCreateCommunity)
        } else {
            // Mostrar vista de comunidad
            CommunityView(
                communities = userCommunities,
                selectedCommunity = selectedCommunity,
                onCommunitySelected = { selectedCommunity = it },
                onNavigate = onNavigate
            )
        }

        // FAB para acciones rápidas
        if (!userCommunities.isNullOrEmpty()) {
            FloatingActionButton(
                onClick = onFloatingActionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun NoCommunityView(
    onJoinCommunity: () -> Unit,
    onCreateCommunity: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Apartment,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aún no perteneces a ninguna comunidad",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Únete a una comunidad existente o crea una nueva para conectar con tus vecinos",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onJoinCommunity() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Unirme a una comunidad")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onCreateCommunity() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Crear nueva comunidad")
        }
    }
}

@Composable
private fun CommunityView(
    communities: List<Community>,
    selectedCommunity: Community?,
    onCommunitySelected: (Community) -> Unit,
    onNavigate: (Any) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Selector de comunidad
        if (communities.size > 1) {
            CommunitySelectorRow(
                communities = communities,
                selectedCommunity = selectedCommunity,
                onCommunitySelected = onCommunitySelected
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Contenido de la comunidad seleccionada
        selectedCommunity?.let { community ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información de la comunidad
                item {
                    CommunityInfoCard(community)
                }

                // Módulos principales
                item {
                    ModulesSection(onNavigate)
                }

                // Anuncios recientes
                item {
                    RecentAnnouncementsSection(
                        announcements = community.announcements.take(3),
                        onSeeAll = { onNavigate(ScreenAnuncios) }
                    )
                }

                // Próximos eventos
                item {
                    UpcomingEventsSection(
                        events = community.events.take(3),
                        onSeeAll = { onNavigate(ScreenEventos) }
                    )
                }

                // Propuestas activas
                item {
                    ActiveProposalsSection(
                        proposals = community.proposals.take(3),
                        onSeeAll = { /* Navegar a propuestas */ }
                    )
                }

                // Espacio al final para evitar que el contenido quede oculto por el FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun CommunitySelectorRow(
    communities: List<Community>,
    selectedCommunity: Community?,
    onCommunitySelected: (Community) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(communities) { community ->
            val isSelected = community.id == selectedCommunity?.id

            Card(
                modifier = Modifier
                    .height(40.dp)
                    .clickable { onCommunitySelected(community) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityInfoCard(community: Community) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Apartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    community.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    icon = Icons.Default.People,
                    value = "${community.members.size}",
                    label = "Miembros"
                )

                InfoItem(
                    icon = Icons.Default.Notifications,
                    value = "${community.announcements.size}",
                    label = "Anuncios"
                )

                InfoItem(
                    icon = Icons.Default.Event,
                    value = "${community.events.size}",
                    label = "Eventos"
                )

                InfoItem(
                    icon = Icons.Default.HowToVote,
                    value = "${community.proposals.size}",
                    label = "Propuestas"
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModulesSection(onNavigate: (Any) -> Unit) {
    val modules = listOf(
        Triple("Anuncios", Icons.Default.Notifications, ScreenAnuncios),
        Triple("Eventos", Icons.Default.CalendarMonth, ScreenEventos),
        Triple("Tablón", Icons.AutoMirrored.Filled.Message, ScreenTablon),
        Triple("Propuestas", Icons.Default.HowToVote, ScreenSugerencias)
    )

    Column {
        Text(
            text = "Módulos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            modules.forEach { (title, icon, destination) ->
                ModuleItem(
                    title = title,
                    icon = icon,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigate(destination)
                }
            }
        }
    }
}

@Composable
private fun ModuleItem(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecentAnnouncementsSection(
    announcements: List<Announcement>,
    onSeeAll: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Anuncios recientes",
            onSeeAll = onSeeAll
        )

        if (announcements.isEmpty()) {
            EmptyStateMessage(message = "No hay anuncios recientes")
        } else {
            announcements.forEach { announcement ->
                AnnouncementItem(announcement)
                if (announcement != announcements.last()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun AnnouncementItem(announcement: Announcement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* Navegar al detalle */ },
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = announcement.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Por: Admin",  // Aquí deberías obtener el nombre real del autor
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = announcement.createdAt?.toDate()?.formatToString() ?: "Fecha desconocida",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UpcomingEventsSection(
    events: List<Event>,
    onSeeAll: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Próximos eventos",
            onSeeAll = onSeeAll
        )

        if (events.isEmpty()) {
            EmptyStateMessage(message = "No hay eventos próximos")
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    EventCard(event)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(event: Event) {
    ElevatedCard(
        modifier = Modifier
            .width(240.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { /* Navegar al detalle */ }
    ) {
        Column {
            // Imagen del evento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (event.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(event.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Fecha del evento
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.dateTime?.toDate()?.formatToString() ?: "Fecha por confirmar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Detalles del evento
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Asistentes
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${event.rsvps.size} asistentes",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Botón de asistencia
                    Text(
                        text = "Asistiré",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { /* Marcar asistencia */ }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveProposalsSection(
    proposals: List<Proposal>,
    onSeeAll: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Propuestas activas",
            onSeeAll = onSeeAll
        )

        if (proposals.isEmpty()) {
            EmptyStateMessage(message = "No hay propuestas activas")
        } else {
            proposals.forEach { proposal ->
                ProposalItem(proposal)
                if (proposal != proposals.last()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProposalItem(proposal: Proposal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* Navegar al detalle */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = proposal.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fecha
                Text(
                    text = proposal.createdAt?.toDate()?.formatToString() ?: "Fecha desconocida",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Votos
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HowToVote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${proposal.voteCount} votos",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Ver todos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onSeeAll)
        )
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* === Componentes mejorados para el Dashboard Senior ================================= */
@Composable
private fun WelcomeCard(isSenior: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "¡Bienvenido!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = if (isSenior)
                        "Accede fácilmente a todas las funciones de tu comunidad"
                    else
                        "¿Qué te gustaría hacer hoy?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun Grid2x2(
    items: List<Triple<String, ImageVector, Any>>,
    onClick: (Any) -> Unit
) {
    Column {
        items.chunked(2).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dim.gap(true))
            ) {
                row.forEach { (title, icon, dest) ->
                    LargeCard(
                        title = title,
                        icon = icon,
                        description = getDescriptionForModule(title),
                        modifier = Modifier.weight(1f)
                    ) {
                        onClick(dest)
                    }
                }

                // Si solo hay un elemento en la fila, añadir un espacio vacío
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(Dim.gap(true)))
        }
    }
}

@Composable
private fun LargeCard(
    title: String,
    icon: ImageVector,
    description: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = Card(
    modifier = modifier
        .height(if (description != null) 140.dp else Dim.cardH(true))
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick),
    colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.primary)
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(Dim.icon(true))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = Dim.text(true),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        description?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HelpSupportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Necesitas ayuda?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estamos aquí para asistirte con cualquier duda o problema que tengas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Abrir pantalla de ayuda */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Contactar soporte")
            }
        }
    }
}

// Funciones de utilidad
private fun getDescriptionForModule(moduleName: String): String {
    return when (moduleName) {
        "Anuncios" -> "Información importante de tu comunidad"
        "Eventos" -> "Actividades y reuniones programadas"
        "Sugerencias" -> "Comparte tus ideas para mejorar"
        "Tablón" -> "Mensajes y comunicaciones entre vecinos"
        "Ajustes" -> "Configura tu perfil y preferencias"
        else -> ""
    }
}

private fun Date.formatToString(): String {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(this)
}

private fun Timestamp.toDate(): Date {
    return Date(this.seconds * 1000 + this.nanoseconds / 1000000)
}
