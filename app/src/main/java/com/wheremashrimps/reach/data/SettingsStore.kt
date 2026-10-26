package com.wheremashrimps.reach.data

import android.content.Context
import com.wheremashrimps.reach.ui.LauncherTheme

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var theme: LauncherTheme
        get() = prefs.getString(KEY_THEME, null)
            ?.let { name -> LauncherTheme.entries.firstOrNull { it.name == name } }
            ?: LauncherTheme.DUNES
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    var photoColors: PhotoColors?
        get() = prefs.getString(KEY_PHOTO_COLORS, null)
            ?.split(',')?.mapNotNull { it.toIntOrNull() }
            ?.takeIf { it.size == 5 }
            ?.let { PhotoColors(it[0], it[1], it[2], it[3], it[4]) }
        set(value) {
            val encoded = value?.run { listOf(text, textSecondary, iconTint, iconBackground, drawerScrim).joinToString(",") }
            prefs.edit().putString(KEY_PHOTO_COLORS, encoded).apply()
        }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_PHOTO_COLORS = "photo_colors"
    }
}
