package com.vecinapp.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

/* ------------------ COLORS ------------------ */
val PrimaryColor = Color(0xFF2E7D32)
val AccentColor  = Color(0xFFFFC107)
val DangerColor  = Color(0xFFD32F2F)
val Gray100      = Color(0xFFFAFAFA)
val Gray700      = Color(0xFF616161)

@Composable
fun VecinalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary   = PrimaryColor,
            secondary = AccentColor,
            error     = DangerColor,
            background = Gray100,
            surface    = Color.White,
            onPrimary  = Color.White,
            onSecondary = Color.Black
        ),
        typography = Typography(
            bodyLarge  = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        ),
        content = content
    )
}

/* ------------------ DIMENSIONS ------------------ */
object Dimens {
    var scale by mutableFloatStateOf(1f)
    val btnHeight : Dp @Composable get() = (56  * scale).dp
    val iconSize  : Dp @Composable get() = (48  * scale).dp
    val cardH     : Dp @Composable get() = (140 * scale).dp
    val gap       : Dp @Composable get() = (24  * scale).dp
}
fun setSeniorMode(on: Boolean) { Dimens.scale = if (on) 1.2f else 1f }

/* ------------------ COMMON BUTTONS ------------------ */
@Composable
fun FilledBtn(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(Dimens.btnHeight),
        shape = RoundedCornerShape(12.dp)
    ) { Text(label, fontSize = 18.sp * Dimens.scale) }
}

@Composable
fun OutBtn(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(Dimens.btnHeight),
        shape = RoundedCornerShape(12.dp)
    ) { Text(label, fontSize = 18.sp * Dimens.scale) }
}

/* ------------------ ONBOARDING ------------------ */
@Composable
fun OnboardingModeScreen(onSelect: (Boolean) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¿Cómo prefieres ver la app?", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(32.dp))
        FilledBtn(
            label = "Modo Adulto Mayor",
            onClick = { onSelect(true) }
        )
        Spacer(Modifier.height(12.dp))
        OutBtn(
            label = "Modo Estándar",
            onClick = { onSelect(false) }
        )
        Spacer(Modifier.height(24.dp))
        Text("Puedes cambiarlo en Ajustes", color = Gray700)
    }
}

/* ------------------ DASHBOARD SENIOR ------------------ */
@Composable
fun DashboardSeniorScreen(onNav: (Any) -> Unit) {
    val items = listOf(
        Triple("Anuncios",    Icons.Default.Notifications,   ScreenAnuncios),
        Triple("Eventos",     Icons.Default.Event,           ScreenEventos),
        Triple("Sugerencias", Icons.Default.Lightbulb,       ScreenSugerencias),
        Triple("Tablón",      Icons.Default.Chat,            ScreenTablon)
    )
    Column(Modifier.fillMaxSize().padding(Dimens.gap)) {
        // grid
        LazyColumn { item { GridButtons(items, onNav) } }
        // FAB para panel directivo (demo: siempre visible)
        FloatingActionButton(onClick = { onNav(ScreenPanel) }, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Default.Settings, contentDescription = null)
        }
    }
    Column(Modifier.fillMaxSize().padding(Dimens.gap)) {
        // grid
        LazyColumn { item { GridButtons(items, onNav) } }
        // FAB para panel directivo (demo: siempre visible)
        FloatingActionButton(onClick = { onNav(ScreenPanel) }, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Default.Delete, contentDescription = null)
        }
    }
}

@Composable
fun GridButtons(items: List<Triple<String, ImageVector, Any>>, onClick: (Any) -> Unit) {
    Column {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                row.forEach { (txt, ic, route) ->
                    DashCard(txt, ic, Modifier.weight(1f)) { onClick(route) }
                }
            }
            Spacer(Modifier.height(Dimens.gap))
        }
    }
}

@Composable
fun DashCard(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(Dimens.cardH).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryColor)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(Dimens.iconSize))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.White, fontSize = 18.sp * Dimens.scale)
        }
    }
}

/* ------------------ ANUNCIOS ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnunciosScreen() {
    Scaffold(topBar = { CenterAlignedTopAppBar({ Text("Anuncios") }) }) {
        Box(Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) {
            Text("Próximamente anuncios de la comunidad")
        }
    }
}

/* ------------------ STUB OTRAS PANTALLAS ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosListScreen(onEvent: () -> Unit) {
    Scaffold(topBar = { CenterAlignedTopAppBar({ Text("Eventos") }) }) { p ->
        LazyColumn(Modifier.padding(p)) {
            items(3) {
                EventCard("Completada", "24 MAY", "13‑18 h", "https://picsum.photos/400", onEvent)
            }
        }
    }
}

@Composable
fun EventCard(title: String, dateLabel: String, timeLabel: String, thumbnail: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Image(painter = rememberAsyncImagePainter(thumbnail), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.height(120.dp).fillMaxWidth())
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Box(Modifier.background(AccentColor, RoundedCornerShape(12.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(dateLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(timeLabel, fontSize = 14.sp, color = Gray700)
        }
    }
}

@Composable fun SugerenciasListScreen() { Text("Sugerencias (stub)") }
@Composable fun TablonListScreen() { Text("Tablón (stub)") }
@Composable fun PanelDirectivoScreen() { Text("Panel Directivo (stub)") }