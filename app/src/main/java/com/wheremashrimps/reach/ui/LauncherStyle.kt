package com.wheremashrimps.reach.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.wheremashrimps.reach.data.PhotoColors

enum class LauncherTheme(val label: String) {
    DUNES("Night dunes"),
    PHOTO("Your photo"),
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

/** The style for this theme. [photoColors] are the colors taken from the background photo, if one is set. */
fun LauncherTheme.style(photoColors: PhotoColors?): LauncherStyle = when (this) {
    LauncherTheme.DUNES -> DunesStyle
    LauncherTheme.WALLPAPER -> WallpaperStyle
    LauncherTheme.PHOTO -> photoColors?.let {
        LauncherStyle(
            text = Color(it.text),
            textSecondary = Color(it.textSecondary),
            drawerScrim = Color(it.drawerScrim),
            themedIcons = true,
            iconTint = Color(it.iconTint),
            iconBackground = Color(it.iconBackground),
        )
    } ?: DunesStyle
}

val LocalLauncherStyle = staticCompositionLocalOf { WallpaperStyle }
