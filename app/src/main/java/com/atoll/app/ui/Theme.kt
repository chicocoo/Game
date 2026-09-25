package com.atoll.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Palette « lagon » (identique au prototype web). */
@Immutable
data class Palette(
    val bg: Color, val ink: Color, val inkSoft: Color, val panel: Color, val line: Color,
    val accent: Color, val accentInk: Color, val board: Color, val sea: Color, val sea2: Color, val hole: Color,
    val nacre1: Color, val nacre2: Color, val nacre3: Color, val blocks: List<Color>, val gold: Color, val rock: Color,
    val danger: Color,
)

val LightPalette = Palette(
    bg = Color(0xFFD9F2EE), ink = Color(0xFF0E3F4E), inkSoft = Color(0xFF3E6B76), panel = Color(0xFFF3FBF9),
    line = Color(0xFFAFDAD3), accent = Color(0xFF129C98), accentInk = Color.White, board = Color(0xFF0F5566),
    sea = Color(0xFF3FC4C0), sea2 = Color(0xFF7ADAD4), hole = Color(0xFFE6D5AE),
    nacre1 = Color(0xFFFFF8EF), nacre2 = Color(0xFFE4DCFF), nacre3 = Color(0xFFC6F1EA),
    blocks = listOf(Color(0xFFFF6F59), Color(0xFFFFB23F), Color(0xFF3FB57A), Color(0xFF8B6CEF), Color(0xFF2F7BDD)),
    gold = Color(0xFFE9A20E), rock = Color(0xFF6E6A62), danger = Color(0xFFCF4F3A),
)

val DarkPalette = Palette(
    bg = Color(0xFF06252D), ink = Color(0xFFDDF4F0), inkSoft = Color(0xFF93BFC0), panel = Color(0xFF0B333D),
    line = Color(0xFF1A4E59), accent = Color(0xFF37CBC5), accentInk = Color(0xFF04262D), board = Color(0xFF03181E),
    sea = Color(0xFF0F4F5B), sea2 = Color(0xFF1B7383), hole = Color(0xFF4B4432),
    nacre1 = Color(0xFFFFF8EF), nacre2 = Color(0xFFD8CEFF), nacre3 = Color(0xFFB9EFE7),
    blocks = listOf(Color(0xFFFF7C67), Color(0xFFFFC152), Color(0xFF4DC68B), Color(0xFF9C80F7), Color(0xFF4A8FEB)),
    gold = Color(0xFFFFC940), rock = Color(0xFF8A857B), danger = Color(0xFFDD604A),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }

object Type {
    val display = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
}

@Composable
fun AtollTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val p = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(primary = p.accent, onPrimary = p.accentInk, background = p.bg, onBackground = p.ink,
            surface = p.panel, onSurface = p.ink, surfaceVariant = p.panel, onSurfaceVariant = p.inkSoft, outline = p.line)
    } else {
        lightColorScheme(primary = p.accent, onPrimary = p.accentInk, background = p.bg, onBackground = p.ink,
            surface = p.panel, onSurface = p.ink, surfaceVariant = p.panel, onSurfaceVariant = p.inkSoft, outline = p.line)
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
