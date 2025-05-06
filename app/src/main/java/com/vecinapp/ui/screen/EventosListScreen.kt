package com.vecinapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun EventosListScreen(onEventClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Text("Próximos eventos", style = MaterialTheme.typography.titleLarge)
        LazyColumn(
            Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(4) { idx ->
                EventCard(
                    title = "Convivencia vecinal",
                    date = "10 JUN",
                    time = "11:00 – 14:00",
                    img = "https://picsum.photos/400?$idx",
                    onClick = onEventClick
                )
            }
        }
    }
}

@Composable
private fun EventCard(
    title: String, date: String, time: String, img: String, onClick: () -> Unit
) = Card(
    onClick, shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors()
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