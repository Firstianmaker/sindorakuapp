package com.example.sindoraku.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
private val SindorakuColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = CardTintLavender,
    onPrimaryContainer = AccentVioletDeep,
    secondary = AccentVioletMid,
    onSecondary = OnPrimary,
    tertiary = AccentLime,
    onTertiary = InkDeep,
    background = Canvas,
    onBackground = Ink,
    surface = SurfaceCard,
    onSurface = Ink,
    surfaceVariant = Surface,
    onSurfaceVariant = Charcoal,
    outline = Hairline,
    outlineVariant = HairlineSoft,
    error = SemanticError,
    onError = OnPrimary,
    inverseSurface = CanvasDark,
    inverseOnSurface = OnDark
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppTokens.Radius.xs),
    small = RoundedCornerShape(AppTokens.Radius.sm),
    medium = RoundedCornerShape(AppTokens.Radius.md),
    large = RoundedCornerShape(AppTokens.Radius.lg),
    extraLarge = RoundedCornerShape(AppTokens.Radius.xl)
)

@Composable
fun SindorakuTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    @Suppress("UNUSED_VARIABLE")
    val isDark = darkTheme || isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = SindorakuColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
