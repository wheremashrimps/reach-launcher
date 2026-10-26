package com.wheremashrimps.reach.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import java.io.File

/** Colors pulled from the background photo, as ARGB ints. */
data class PhotoColors(
    val text: Int,
    val textSecondary: Int,
    val iconTint: Int,
    val iconBackground: Int,
    val drawerScrim: Int,
)

/** Keeps a downscaled copy of the user's chosen background photo in app storage. */
class BackgroundStore(private val context: Context) {

    private val file get() = File(context.filesDir, "background.jpg")

    fun load(): Bitmap? = if (file.exists()) BitmapFactory.decodeFile(file.path) else null

    /** Copies the picked photo into app storage, scaled to about screen size. Returns it, or null if it can't be read. */
    fun save(uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // Halve the size until the long edge is near the screen's, so a 50 MP photo doesn't fill memory.
        val target = context.resources.displayMetrics.run { maxOf(widthPixels, heightPixels) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= target) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        return bitmap
    }

    companion object {
        /**
         * Picks theme colors from the photo: a light accent for icons and text, and a deep shade
         * of the photo's darks for icon discs and the drawer, adjusted so text stays readable.
         */
        fun extractColors(bitmap: Bitmap): PhotoColors {
            val palette = Palette.from(bitmap).maximumColorCount(16).generate()
            val accentSwatch = palette.lightVibrantSwatch ?: palette.lightMutedSwatch
                ?: palette.vibrantSwatch ?: palette.dominantSwatch
            val darkSwatch = palette.darkMutedSwatch ?: palette.darkVibrantSwatch ?: palette.dominantSwatch

            val accent = atLeastLuminance(accentSwatch?.rgb ?: Color.WHITE, 0.55)
            val dark = atMostLuminance(darkSwatch?.rgb ?: Color.BLACK, 0.06)

            return PhotoColors(
                text = ColorUtils.blendARGB(accent, Color.WHITE, 0.55f),
                textSecondary = ColorUtils.setAlphaComponent(accent, 0xD9),
                iconTint = accent,
                iconBackground = ColorUtils.setAlphaComponent(dark, 0xE6),
                drawerScrim = ColorUtils.setAlphaComponent(dark, 0xE0),
            )
        }

        private fun atLeastLuminance(color: Int, min: Double): Int {
            var c = color
            while (ColorUtils.calculateLuminance(c) < min) c = ColorUtils.blendARGB(c, Color.WHITE, 0.15f)
            return c
        }

        private fun atMostLuminance(color: Int, max: Double): Int {
            var c = color
            while (ColorUtils.calculateLuminance(c) > max) c = ColorUtils.blendARGB(c, Color.BLACK, 0.15f)
            return c
        }
    }
}
