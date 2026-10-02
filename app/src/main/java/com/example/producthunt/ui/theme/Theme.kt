package com.example.producthunt.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = PhOrange,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = PhOrangeDark,
    background = PhBackground,
    surface = androidx.compose.ui.graphics.Color.White
)

private val DarkColors = darkColorScheme(
    primary = PhOrange,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = PhOrangeDark,
    background = PhBackgroundDark,
    surface = PhSurfaceDark
)

@Composable
fun ProductHuntTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desligado por padrão: a cor dinâmica (Material You) do One UI da Samsung
    // gera paletas com contraste ruim/instável (texto secundário quase invisível),
    // diferente da implementação padrão do Android. Usamos sempre as cores fixas
    // do app, que têm contraste garantido em qualquer aparelho.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
