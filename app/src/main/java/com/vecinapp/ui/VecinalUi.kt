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

internal object Dim {
    private fun k(senior: Boolean) = if (senior) 1.25f else 1f
    fun gap(s: Boolean) = (20 * k(s)).dp
    fun cardH(s: Boolean) = (140 * k(s)).dp
    fun icon(s: Boolean) = (48 * k(s)).dp
    fun btnH(s: Boolean) = (56 * k(s)).dp
    fun text(s: Boolean) = (18 * k(s)).sp
}

@Composable
internal fun ModeSwitchButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val txtColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    )
    Button(
        onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = txtColor
        ),
        modifier = Modifier.height(Dim.btnH(selected))
    ) { Text(label) }
}


@Composable
internal fun CenterText(msg: String) = Box(
    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
) { Text(msg) }
