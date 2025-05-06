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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/* ───────── helpers rápidos ───────── */
private fun Float.s(enlarged: Boolean) = if (enlarged) this * 1.25f else this
private fun Int.dpS(senior: Boolean) = (this.toFloat().s(senior)).dp
private fun Int.spS(senior: Boolean) = (this.toFloat().s(senior)).sp

/* ────────── UI ───────────────────── */
@Composable
fun EventDetailScreen(
    title: String,
    dateTime: String,
    description: String,
    organizer: String,
    phone: String,
    isSenior: Boolean,
    onBack: () -> Unit = {},
    lat: Double,
    lon: Double
) {
    /* tamaños dependientes */
    val hImg = 180.dpS(isSenior)
    val hGap = 20.dpS(isSenior)
    val txtBig = 22.spS(isSenior)
    val txtNor = 16.spS(isSenior)

    Box(Modifier.fillMaxSize()) {
        /* ---------- CONTENIDO (scroll) ---------- */
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = hGap),
            verticalArrangement = Arrangement.spacedBy(hGap)
        ) {

            AsyncImage(
                model = "https://picsum.photos/800/400",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hImg)
                    .clip(RoundedCornerShape(16.dp))
            )

            Text(title, fontSize = txtBig, style = MaterialTheme.typography.titleLarge)

            InfoRow(Icons.Default.DateRange, dateTime, txtNor)

            Text(description, fontSize = txtNor)

            HorizontalDivider()

            Text("Organiza:", fontSize = txtNor, color = MaterialTheme.colorScheme.primary)
            InfoRow(Icons.Default.Person, organizer, txtNor)
            InfoRow(
                Icons.Default.Phone, phone, txtNor,
                tint = MaterialTheme.colorScheme.primary,
                onClick = { /* lanzar intent */ }
            )

            /* botones */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { /* compartir */ }, modifier = Modifier.weight(1f)) {
                    Text("Compartir", fontSize = txtNor)
                }
                Button(onClick = { /* asistir */ }, modifier = Modifier.weight(1f)) {
                    Text("Asistiré", fontSize = txtNor)
                }
            }
        }

        /* ---------- Botón volver ---------- */
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .7f),
                    shape = RoundedCornerShape(50)
                )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
        }
    }
}

/* fila icono + texto reutilizable */
@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit = {}
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.clickable(onClick = onClick)
) {
    Icon(icon, null, tint = tint)
    Spacer(Modifier.width(6.dp))
    Text(text, fontSize = fontSize, color = tint)
}
