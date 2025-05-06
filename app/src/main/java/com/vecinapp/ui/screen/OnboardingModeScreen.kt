package com.vecinapp.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.vecinapp.ui.Dim

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


@Composable
fun OnboardingModeScreen(onSelect: (Boolean) -> Unit) {
    var senior by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        //MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer,
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
    ) {

        Text("¿Cómo prefieres ver la app?", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ModeSwitchButton("Estándar", !senior, onClick = { senior = false })
            ModeSwitchButton("Senior", senior, onClick = { senior = true })
        }

        Button(
            onClick = { onSelect(senior) },
            modifier = Modifier.fillMaxWidth(.6f),
            shape = RoundedCornerShape(24.dp)
        ) {

            Icon(
                imageVector = Icons.Filled.Diversity3,
                contentDescription = "Comunidad",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp)
            )
            Text("Continuar")
        }
    }
}