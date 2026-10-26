package com.wheremashrimps.reach.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheremashrimps.reach.data.AppInfo

/** Soft shadow so white text stays readable on any wallpaper. */
val WallpaperTextShadow = Shadow(color = Color.Black.copy(alpha = 0.45f), blurRadius = 8f)

class AppActions(
    val onLaunch: (AppInfo) -> Unit,
    val onToggleFavorite: (AppInfo) -> Unit,
    val onMove: (AppInfo, Int) -> Unit,
    val onInfo: (AppInfo) -> Unit,
    val onUninstall: (AppInfo) -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppRow(
    app: AppInfo,
    isFavorite: Boolean,
    actions: AppActions,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val iconSize = if (large) 44.dp else 36.dp

    Box(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { actions.onLaunch(app) },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOpen = true
                    },
                )
                .padding(vertical = if (large) 10.dp else 7.dp),
        ) {
            Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(iconSize))
            Spacer(Modifier.width(16.dp))
            Text(
                text = app.label,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontSize = if (large) 22.sp else 17.sp, shadow = WallpaperTextShadow),
            )
        }

        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            val close = { menuOpen = false }
            MenuItem(if (isFavorite) "Remove from favorites" else "Add to favorites", close) { actions.onToggleFavorite(app) }
            if (canMoveUp) MenuItem("Move up", close) { actions.onMove(app, -1) }
            if (canMoveDown) MenuItem("Move down", close) { actions.onMove(app, 1) }
            MenuItem("App info", close) { actions.onInfo(app) }
            MenuItem("Uninstall", close) { actions.onUninstall(app) }
        }
    }
}

@Composable
private fun MenuItem(label: String, close: () -> Unit, action: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = {
            close()
            action()
        },
    )
}
