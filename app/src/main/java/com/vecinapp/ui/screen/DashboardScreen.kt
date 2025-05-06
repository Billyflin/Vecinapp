package com.vecinapp.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vecinapp.ScreenAnuncios
import com.vecinapp.ScreenEventos
import com.vecinapp.ScreenSettings
import com.vecinapp.ScreenSugerencias
import com.vecinapp.ScreenTablon
import com.vecinapp.ui.Dim


@Composable
fun DashboardScreen(
    isSenior : Boolean,
    onNavigate: (Any) -> Unit
) {
    if (isSenior) {
        SeniorDashboard(onNavigate)
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bienvenido 👋", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

/* === 3a. Dashboard modo SENIOR ================================= */
@Composable
private fun SeniorDashboard(onNavigate: (Any) -> Unit) {

    val modules = listOf(
        Triple("Anuncios",    Icons.Default.Notifications, ScreenAnuncios),
        Triple("Eventos",     Icons.Default.Event,         ScreenEventos),
        Triple("Sugerencias", Icons.Default.Lightbulb,     ScreenSugerencias),
        Triple("Tablón",      Icons.Default.Chat,          ScreenTablon)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dim.gap(true)),
        verticalArrangement = Arrangement.spacedBy(Dim.gap(true))
    ) {

        /* grid 2×2 */
        item {
            Grid2x2(modules) { onNavigate(it) }
        }

        /* tarjeta extra: Ajustes */
        item {
            LargeCard("Ajustes", Icons.Default.Settings) { onNavigate(ScreenSettings) }
        }
    }
}

/* ---------- grid helper ---------- */
@Composable
private fun Grid2x2(
    items   : List<Triple<String, ImageVector, Any>>,
    onClick : (Any) -> Unit
) {
    Column {
        items.chunked(2).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dim.gap(true))
            ) {
                row.forEach { (title, icon, dest) ->
                    LargeCard(title, icon, Modifier.weight(1f)) { onClick(dest) }
                }
            }
            Spacer(Modifier.height(Dim.gap(true)))
        }
    }
}

/* ---------- tarjeta grande reutilizable ---------- */
@Composable
private fun LargeCard(
    title   : String,
    icon    : ImageVector,
    modifier: Modifier = Modifier,
    onClick : () -> Unit
) = Card(
    modifier = modifier
        .height(Dim.cardH(true))
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick),
    colors  = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.primary)
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null,
            tint     = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(Dim.icon(true))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            color    = MaterialTheme.colorScheme.onPrimary,
            fontSize = Dim.text(true),
            fontWeight = FontWeight.SemiBold
        )
    }
}