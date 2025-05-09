// file: com/vecinapp/ui/screen/EventsScreenPreview.kt
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.vecinapp.domain.model.Attachment
import com.vecinapp.domain.model.Comment
import com.vecinapp.domain.model.Event
import com.vecinapp.domain.model.Rsvp
import com.vecinapp.domain.model.RsvpStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Función principal para mostrar la pantalla con datos de ejemplo
@Composable
fun EventsScreenWithMockData() {
    val mockEvents = createMockEvents()
    val currentUserId = "user123" // ID de usuario de ejemplo

    EventsScreen(
        events = mockEvents,
        currentUserId = currentUserId,
        isLoading = false,
        onEventClick = { /* No hace nada en la demo */ },
        onCreateEventClick = { /* No hace nada en la demo */ },
        onRsvpStatusChange = { _, _ -> /* No hace nada en la demo */ }
    )
}

// Función para crear eventos de ejemplo
private fun createMockEvents(): List<Event> {
    val calendar = Calendar.getInstance()

    // Evento 1: Evento futuro (mañana)
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 18)
    calendar.set(Calendar.MINUTE, 0)
    val tomorrowEvent = createEvent(
        id = "event1",
        title = "Reunión de vecinos",
        description = "Discutiremos temas importantes sobre la comunidad y planificaremos actividades para el próximo mes. ¡No faltes!",
        startDate = calendar.time,
        imageUrl = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?q=80&w=2574&auto=format&fit=crop",
        attendeeCount = 15,
        commentCount = 8
    )

    // Evento 2: Evento futuro (próxima semana)
    calendar.add(Calendar.DAY_OF_MONTH, 6)
    calendar.set(Calendar.HOUR_OF_DAY, 10)
    calendar.set(Calendar.MINUTE, 30)
    val nextWeekEvent = createEvent(
        id = "event2",
        title = "Limpieza comunitaria",
        description = "Jornada de limpieza en las áreas comunes. Trae guantes y bolsas de basura. ¡Juntos podemos mantener nuestra comunidad limpia!",
        startDate = calendar.time,
        imageUrl = "https://images.unsplash.com/photo-1617450365226-9bf28c04e130?q=80&w=2670&auto=format&fit=crop",
        attendeeCount = 8,
        commentCount = 3
    )

    // Evento 3: Evento pasado (ayer)
    calendar.setTime(Date()) // Resetear a hoy
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    calendar.set(Calendar.HOUR_OF_DAY, 19)
    calendar.set(Calendar.MINUTE, 0)
    val yesterdayEvent = createEvent(
        id = "event3",
        title = "Fiesta de vecinos",
        description = "Celebración anual de la comunidad con comida, música y actividades para todas las edades.",
        startDate = calendar.time,
        imageUrl = "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?q=80&w=2670&auto=format&fit=crop",
        attendeeCount = 25,
        commentCount = 12
    )

    // Evento 4: Evento futuro (en dos semanas)
    calendar.setTime(Date()) // Resetear a hoy
    calendar.add(Calendar.DAY_OF_MONTH, 14)
    calendar.set(Calendar.HOUR_OF_DAY, 16)
    calendar.set(Calendar.MINUTE, 0)
    val twoWeeksEvent = createEvent(
        id = "event4",
        title = "Taller de jardinería",
        description = "Aprende a cuidar tus plantas y contribuye al embellecimiento de nuestra comunidad. Traeremos expertos en jardinería urbana.",
        startDate = calendar.time,
        imageUrl = "https://images.unsplash.com/photo-1599685315640-4a9ba2613f46?q=80&w=2670&auto=format&fit=crop",
        attendeeCount = 12,
        commentCount = 5
    )

    return listOf(tomorrowEvent, nextWeekEvent, yesterdayEvent, twoWeeksEvent)
}

// Función auxiliar para crear un evento con datos específicos
private fun createEvent(
    id: String,
    title: String,
    description: String,
    startDate: Date,
    imageUrl: String,
    attendeeCount: Int,
    commentCount: Int
): Event {
    val calendar = Calendar.getInstance()
    calendar.time = startDate
    calendar.add(Calendar.HOUR_OF_DAY, 2) // El evento dura 2 horas

    val startTimestamp = Timestamp(startDate)
    val endTimestamp = Timestamp(calendar.time)

    // Crear RSVPs de ejemplo
    val rsvps = mutableListOf<Rsvp>()

    // Añadir RSVP para el usuario actual (user123)
    rsvps.add(
        Rsvp(
            userId = "user123",
            status = RsvpStatus.GOING,
            respondedAt = Timestamp(Date())
        )
    )

    // Añadir RSVPs para otros usuarios
    for (i in 1 until attendeeCount) {
        val status = when (i % 3) {
            0 -> RsvpStatus.GOING
            1 -> RsvpStatus.MAYBE
            else -> RsvpStatus.DECLINED
        }

        rsvps.add(
            Rsvp(
                userId = "user${100 + i}",
                status = status,
                respondedAt = Timestamp(Date())
            )
        )
    }

    // Crear comentarios de ejemplo
    val comments = mutableListOf<Comment>()
    for (i in 1..commentCount) {
        comments.add(
            Comment(
                id = "comment$i",
                authorId = "user${200 + i}",
                content = "Este es un comentario de ejemplo #$i sobre el evento.",
                createdAt = Timestamp(Date())
            )
        )
    }

    return Event(
        id = id,
        title = title,
        description = description,
        dateTime = startTimestamp,
        location = GeoPoint(37.7749, -122.4194), // Coordenadas de ejemplo
        imageUrl = imageUrl,
        organizerId = "organizer123",
        createdAt = Timestamp(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7)), // Creado hace una semana
        updatedAt = Timestamp(Date()),
        attachments = listOf(
            Attachment(url = "https://example.com/attachment1.pdf", type = "document"),
            Attachment(url = "https://example.com/attachment2.jpg", type = "image")
        ),
        comments = comments,
        rsvps = rsvps,
        voteCount = (5..20).random(),
        attendees = rsvps.filter { it.status == RsvpStatus.GOING }.map { it.userId },
        startDate = startTimestamp,
        endDate = endTimestamp
    )
}

// Implementación de la pantalla de eventos (igual que antes)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    events: List<Event>,
    currentUserId: String,
    isLoading: Boolean = false,
    onEventClick: (Event) -> Unit,
    onCreateEventClick: () -> Unit,
    onRsvpStatusChange: (Event, RsvpStatus) -> Unit
) {
    // Estados para filtros y visualización
    var selectedFilter by remember { mutableStateOf(EventFilter.ALL) }
    var showPastEvents by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cabecera con título y filtros
                EventsHeader(
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    showPastEvents = showPastEvents,
                    onShowPastEventsChange = { showPastEvents = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de eventos
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (events.isEmpty()) {
                    EmptyEventsState(onCreateEventClick)
                } else {
                    val filteredEvents = events.filter { event ->
                        when (selectedFilter) {
                            EventFilter.ALL -> true
                            EventFilter.GOING -> event.rsvps.any { it.userId == currentUserId && it.status == RsvpStatus.GOING }
                            EventFilter.MAYBE -> event.rsvps.any { it.userId == currentUserId && it.status == RsvpStatus.MAYBE }
                        }
                    }.filter { event ->
                        if (showPastEvents) {
                            true
                        } else {
                            event.startDate?.toDate()?.after(Date()) ?: true
                        }
                    }.sortedBy { it.startDate?.seconds }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredEvents) { event ->
                            EventCard(
                                event = event,
                                currentUserId = currentUserId,
                                onClick = { onEventClick(event) },
                                onRsvpStatusChange = { status -> onRsvpStatusChange(event, status) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Espacio para el FAB
                        }
                    }
                }
            }

            // Botón flotante para crear evento
            FloatingActionButton(
                onClick = onCreateEventClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear evento",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventsHeader(
    selectedFilter: EventFilter,
    onFilterChange: (EventFilter) -> Unit,
    showPastEvents: Boolean,
    onShowPastEventsChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Eventos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            FilterChip(
                selected = showPastEvents,
                onClick = { onShowPastEventsChange(!showPastEvents) },
                label = { Text("Eventos pasados") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedFilter == EventFilter.ALL,
                onClick = { onFilterChange(EventFilter.ALL) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            ) {
                Text("Todos")
            }
            SegmentedButton(
                selected = selectedFilter == EventFilter.GOING,
                onClick = { onFilterChange(EventFilter.GOING) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            ) {
                Text("Asistiré")
            }
            SegmentedButton(
                selected = selectedFilter == EventFilter.MAYBE,
                onClick = { onFilterChange(EventFilter.MAYBE) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            ) {
                Text("Tal vez")
            }
        }
    }
}

@Composable
private fun EmptyEventsState(onCreateEventClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No hay eventos programados",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Crea el primer evento para tu comunidad",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateEventClick,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Crear evento")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(
    event: Event,
    currentUserId: String,
    onClick: () -> Unit,
    onRsvpStatusChange: (RsvpStatus) -> Unit
) {
    val currentUserRsvp = event.rsvps.find { it.userId == currentUserId }
    val currentStatus = currentUserRsvp?.status ?: RsvpStatus.UNDECIDED

    var showRsvpOptions by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Imagen del evento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
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

                // Overlay para mejorar legibilidad del texto
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

                // Menú de opciones
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Compartir evento") },
                            onClick = {
                                // Implementar compartir
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reportar") },
                            onClick = {
                                // Implementar reporte
                                showMenu = false
                            }
                        )
                    }
                }

                // Título del evento
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Contenido del evento
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Fecha y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatDateTime(event.startDate?.toDate()),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (event.endDate != null) {
                        Text(
                            text = " - ${formatTime(event.endDate.toDate())}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Ubicación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Ubicación del evento", // Aquí deberías mostrar la dirección real
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Descripción
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider()

                // Estadísticas y acciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Asistentes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = event.rsvps.count { it.status == RsvpStatus.GOING }
                                            .toString()
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Comentarios
                        BadgedBox(
                            badge = {
                                if (event.comments.isNotEmpty()) {
                                    Badge {
                                        Text(
                                            text = event.comments.size.toString()
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Botón de RSVP y opciones
                    Box {
                        Button(
                            onClick = { showRsvpOptions = !showRsvpOptions },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (currentStatus) {
                                    RsvpStatus.GOING -> MaterialTheme.colorScheme.primaryContainer
                                    RsvpStatus.MAYBE -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                contentColor = when (currentStatus) {
                                    RsvpStatus.GOING -> MaterialTheme.colorScheme.onPrimaryContainer
                                    RsvpStatus.MAYBE -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            when (currentStatus) {
                                RsvpStatus.GOING -> Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                RsvpStatus.MAYBE -> Icon(
                                    Icons.Default.QuestionMark,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )

                                else -> Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = when (currentStatus) {
                                    RsvpStatus.GOING -> "Asistiré"
                                    RsvpStatus.MAYBE -> "Tal vez"
                                    RsvpStatus.DECLINED -> "No asistiré"
                                    RsvpStatus.UNDECIDED -> "¿Asistirás?"
                                }
                            )
                        }

                        // Menú desplegable de opciones RSVP
                        if (showRsvpOptions) {
                            Card(
                                modifier = Modifier
                                    .padding(top = 48.dp)
                                    .width(200.dp)
                                    .align(Alignment.TopEnd),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    RsvpOption(
                                        text = "Asistiré",
                                        icon = Icons.Default.CheckCircle,
                                        selected = currentStatus == RsvpStatus.GOING,
                                        onClick = {
                                            onRsvpStatusChange(RsvpStatus.GOING)
                                            showRsvpOptions = false
                                        }
                                    )

                                    RsvpOption(
                                        text = "Tal vez",
                                        icon = Icons.Default.QuestionMark,
                                        selected = currentStatus == RsvpStatus.MAYBE,
                                        onClick = {
                                            onRsvpStatusChange(RsvpStatus.MAYBE)
                                            showRsvpOptions = false
                                        }
                                    )

                                    RsvpOption(
                                        text = "No asistiré",
                                        icon = Icons.Outlined.Close,
                                        selected = currentStatus == RsvpStatus.DECLINED,
                                        onClick = {
                                            onRsvpStatusChange(RsvpStatus.DECLINED)
                                            showRsvpOptions = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RsvpOption(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// Funciones de utilidad para formatear fechas
private fun formatDate(date: Date?): String {
    if (date == null) return "Fecha por definir"
    val formatter = SimpleDateFormat("dd MMM", Locale("es", "ES"))
    return formatter.format(date)
}

private fun formatDateTime(date: Date?): String {
    if (date == null) return "Fecha por definir"
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "ES"))
    return formatter.format(date)
}

private fun formatTime(date: Date?): String {
    if (date == null) return ""
    val formatter = SimpleDateFormat("HH:mm", Locale("es", "ES"))
    return formatter.format(date)
}

// Enum para filtros de eventos
enum class EventFilter {
    ALL, GOING, MAYBE
}

// Función de vista previa para ver la pantalla en Android Studio
@Preview(showBackground = true)
@Composable
fun EventsScreenPreview() {
    MaterialTheme {
        EventsScreenWithMockData()
    }
}