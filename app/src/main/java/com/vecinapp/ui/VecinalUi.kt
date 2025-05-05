package com.vecinapp.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vecinapp.ScreenAnuncios
import com.vecinapp.ScreenEventos
import com.vecinapp.ScreenSettings
import com.vecinapp.ScreenSugerencias
import com.vecinapp.ScreenTablon

/* ------------------------------------------------------------
 *  1.  Helpers de tamaño – dependen de si es modo Senior
 * ----------------------------------------------------------- */
private object Dim {
    private fun k(senior: Boolean) = if (senior) 1.25f else 1f
    fun gap  (s: Boolean) = (20 * k(s)).dp
    fun cardH(s: Boolean) = (140 * k(s)).dp
    fun icon (s: Boolean) = (48 * k(s)).dp
    fun btnH (s: Boolean) = (56 * k(s)).dp
    fun text (s: Boolean) = (18 * k(s)).sp
}

/* ------------------------------------------------------------
 *  2.  On‑boarding – elige estilo visual
 * ----------------------------------------------------------- */
@Composable
fun OnboardingModeScreen(onSelect: (Boolean) -> Unit) {
    var senior by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
    ) {

        Text("¿Cómo prefieres ver la app?", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ModeSwitchButton("Estándar", !senior, onClick = { senior = false })
            ModeSwitchButton("Senior",   senior,  onClick = { senior = true  })
        }

        Button(
            onClick  = { onSelect(senior) },
            modifier = Modifier.fillMaxWidth(.6f),
            shape    = RoundedCornerShape(24.dp)
        ) { Text("Continuar") }
    }
}

@Composable
private fun ModeSwitchButton(
    label   : String,
    selected: Boolean,
    onClick : () -> Unit
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val txtColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    )
    Button(
        onClick,
        shape   = RoundedCornerShape(16.dp),
        colors  = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor   = txtColor
        ),
        modifier = Modifier.height(Dim.btnH(selected))
    ) { Text(label) }
}

/* ------------------------------------------------------------
 *  3.  Dashboard (elige plantilla según modo)
 * ----------------------------------------------------------- */
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

/* ------------------------------------------------------------
 *  4.  Lista de Eventos (estándar + placeholder)
 * ----------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosListScreen(onEventClick: () -> Unit) {
    Scaffold(topBar = { CenterAlignedTopAppBar({ Text("Eventos") }) }) { padd ->
        LazyColumn(
            Modifier
                .padding(padd)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(4) { idx ->
                EventCard(
                    title = "Convivencia vecinal",
                    date  = "10 JUN",
                    time  = "11:00 – 14:00",
                    img   = "https://picsum.photos/400?$idx",
                    onClick = onEventClick
                )
            }
        }
    }
}

@Composable
private fun EventCard(
    title: String,
    date : String,
    time : String,
    img  : String,
    onClick: () -> Unit
) = Card(
    onClick,
    shape  = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors()
) {
    AsyncImage(
        model = img,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    )
    Column(Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("$date  •  $time", style = MaterialTheme.typography.bodyMedium)
    }
}

/* ------------------------------------------------------------
 *  5.  Módulos stub rápidos
 * ----------------------------------------------------------- */
@Composable fun AnunciosScreen()        = CenterText("Próximamente anuncios")
@Composable fun SugerenciasListScreen() = CenterText("Sugerencias (stub)")
@Composable fun TablonListScreen()      = CenterText("Tablón (stub)")
@Composable fun PanelDirectivoScreen()  = CenterText("Panel directivo (stub)")

@Composable
private fun CenterText(msg: String) = Box(
    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
) { Text(msg) }
