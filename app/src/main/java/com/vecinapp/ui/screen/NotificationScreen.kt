// file: com/vecinapp/ui/screen/NotificationScreen.kt
package com.vecinapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun NotificationScreen(
    onBack: () -> Unit = {},
    onNavigateToEventDetails: (String) -> Unit = {},
    onNavigateToCommunityDetails: (String) -> Unit = {},
    onNavigateToProposalDetails: (String) -> Unit = {}
) {
    // Estado para las notificaciones
    val notifications = remember { mutableStateListOf<Notification>() }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar notificaciones de ejemplo
    LaunchedEffect(Unit) {
        // Simular carga de datos
        delay(800)
        notifications.addAll(createMockNotifications(15))
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Título de la pantalla
        Text(
            text = "Notificaciones",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )

        // Contenido principal
        if (isLoading) {
            // Pantalla de carga
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (notifications.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No tienes notificaciones",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Las notificaciones aparecerán aquí cuando haya actividad en tus comunidades",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {


            // Lista de notificaciones
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            // Marcar como leída
                            val index = notifications.indexOf(notification)
                            if (index != -1) {
                                notifications[index] = notification.copy(read = true)
                            }

                            // Navegar según el tipo
                            when (notification.type) {
                                NotificationType.EVENT -> notification.relatedId?.let {
                                    onNavigateToEventDetails(
                                        it
                                    )
                                }

                                NotificationType.COMMUNITY -> notification.relatedId?.let {
                                    onNavigateToCommunityDetails(
                                        it
                                    )
                                }

                                NotificationType.PROPOSAL -> notification.relatedId?.let {
                                    onNavigateToProposalDetails(
                                        it
                                    )
                                }

                                else -> {}
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
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
            .padding(16.dp),
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

        Spacer(modifier = Modifier.width(16.dp))

        // Contenido de la notificación
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatTimeAgo(notification.timestamp?.toDate()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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

// Funciones de utilidad

private fun formatTimeAgo(date: Date?): String {
    if (date == null) return ""

    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInMinutes = diffInMillis / (60 * 1000)
    val diffInHours = diffInMillis / (60 * 60 * 1000)
    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

    return when {
        diffInMinutes < 1 -> "Ahora"
        diffInMinutes < 60 -> "Hace ${diffInMinutes} min"
        diffInHours < 24 -> "Hace ${diffInHours} h"
        diffInDays < 7 -> "Hace ${diffInDays} días"
        else -> {
            val formatter = SimpleDateFormat("dd MMM", Locale("es", "ES"))
            formatter.format(date)
        }
    }
}

// Función para crear notificaciones de ejemplo
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

        val titleIndex = i % notificationTitles.size
        val messageIndex = i % notificationMessages.size
        val typeIndex = i % notificationTypes.size

        notifications.add(
            Notification(
                id = "notification$i",
                userId = "user123",
                title = notificationTitles[titleIndex],
                message = notificationMessages[messageIndex],
                timestamp = Timestamp(notificationTime),
                read = i >= count / 3, // Primeras no leídas
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


@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    MaterialTheme {
        NotificationScreen()
    }
}