package com.wheremashrimps.reach.data

import android.content.Context

/** Ordered list of favorite app keys, kept in SharedPreferences. */
class FavoritesStore(context: Context) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    val isInitialized: Boolean get() = prefs.contains(KEY)

    fun load(): List<String> =
        prefs.getString(KEY, null)?.split('\n')?.filter { it.isNotBlank() }.orEmpty()

    fun save(keys: List<String>) {
        prefs.edit().putString(KEY, keys.joinToString("\n")).apply()
    }

    companion object {
        private const val KEY = "keys"

        /** First-run favorites, picked from whichever of these are installed (Samsung and Google variants). */
        val DEFAULT_PACKAGES = listOf(
            "com.samsung.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.messaging",
            "com.google.android.apps.messaging",
            "com.sec.android.app.camera",
            "com.android.chrome",
            "com.sec.android.app.sbrowser",
            "com.google.android.gm",
            "com.spotify.music",
        )
        const val MAX_DEFAULTS = 5
    }
}
