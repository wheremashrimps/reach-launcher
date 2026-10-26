package com.wheremashrimps.reach.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class LauncherTheme(val label: String) {
    DUNES("Night dunes"),
    WALLPAPER("Your wallpaper"),
}

/** Colors and icon treatment for a theme, read by the home page, drawer and app rows. */
@Immutable
data class LauncherStyle(
    val text: Color,
    val textSecondary: Color,
    val drawerScrim: Color,
    /** Draw every icon as a single-color glyph so the apps match the background. */
    val themedIcons: Boolean,
    val iconTint: Color,
    val iconBackground: Color,
)

private val DunesStyle = LauncherStyle(
    text = Color(0xFFEADCC4),
    textSecondary = Color(0xFFB9A68C),
    drawerScrim = Color(0xE6080A12),
    themedIcons = true,
    iconTint = Color(0xFFE8D3B0),
    iconBackground = Color(0xFF2A2129),
)

private val WallpaperStyle = LauncherStyle(
    text = Color.White,
    textSecondary = Color.White.copy(alpha = 0.7f),
    drawerScrim = Color.Black.copy(alpha = 0.72f),
    themedIcons = false,
    iconTint = Color.White,
    iconBackground = Color.Transparent,
)

val LauncherTheme.style: LauncherStyle
    get() = when (this) {
        LauncherTheme.DUNES -> DunesStyle
        LauncherTheme.WALLPAPER -> WallpaperStyle
    }

val LocalLauncherStyle = staticCompositionLocalOf { WallpaperStyle }
