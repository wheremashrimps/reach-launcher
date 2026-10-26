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

    private companion object {
        const val KEY_THEME = "theme"
    }
}
