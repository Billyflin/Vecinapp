package com.vecinapp.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vecinapp.R
import com.vecinapp.domain.model.GoogleButtonTheme

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    shape: Shape = ButtonDefaults.shape,
    theme: GoogleButtonTheme = if (isSystemInDarkTheme()) GoogleButtonTheme.Dark else GoogleButtonTheme.Light,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = when (theme) {
            GoogleButtonTheme.Light   -> Color.White
            GoogleButtonTheme.Dark    -> Color(0xFF131314)
            GoogleButtonTheme.Neutral -> Color(0xFFF2F2F2)
        },
        contentColor = when (theme) {
            GoogleButtonTheme.Dark -> Color(0xFFE3E3E3)
            else                   -> Color(0xFF1F1F1F)
        }
    ),
    border: BorderStroke? = when (theme) {
        GoogleButtonTheme.Light -> BorderStroke(1.dp, Color(0xFF747775))
        GoogleButtonTheme.Dark  -> BorderStroke(1.dp, Color(0xFF8E918F))
        GoogleButtonTheme.Neutral -> null
    },
    iconSize: Int = 38         // 24‑48 dp recomendado por Google
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .semantics { contentDescription = "Iniciar sesión con Google" },
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = PaddingValues(0.dp)       // icon centrado
    ) {
        Image(
            painter = painterResource(R.drawable.google_logo),
            contentDescription = null,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}
