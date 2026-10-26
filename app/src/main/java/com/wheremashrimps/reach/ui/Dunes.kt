package com.wheremashrimps.reach.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random

private val SkyTop = Color(0xFF05070F)
private val SkyMid = Color(0xFF0E1426)
private val SkyHorizon = Color(0xFF2A2133)
private val Moon = Color(0xFFF3E7CF)

/** One dune layer: its crest as (x, y) fractions of the screen, plus lit-top and shadowed-base colors. */
private class Dune(val crest: List<Pair<Float, Float>>, val lit: Color, val shadow: Color, val ridgeAlpha: Float)

// Back to front. Later layers sit lower and darker, so the bottom of the screen
// (where the favorites list is) stays dark and readable.
private val dunes = listOf(
    Dune(
        crest = listOf(0f to 0.60f, 0.22f to 0.55f, 0.48f to 0.61f, 0.72f to 0.53f, 1f to 0.58f),
        lit = Color(0xFF3B2E3B), shadow = Color(0xFF1C1520), ridgeAlpha = 0.25f,
    ),
    Dune(
        crest = listOf(0f to 0.70f, 0.30f to 0.63f, 0.58f to 0.71f, 0.84f to 0.65f, 1f to 0.69f),
        lit = Color(0xFF4E3D40), shadow = Color(0xFF191317), ridgeAlpha = 0.35f,
    ),
    Dune(
        crest = listOf(0f to 0.79f, 0.18f to 0.75f, 0.46f to 0.81f, 0.76f to 0.72f, 1f to 0.77f),
        lit = Color(0xFF5E4B45), shadow = Color(0xFF0D0A0D), ridgeAlpha = 0.45f,
    ),
)

private class Star(val x: Float, val y: Float, val radius: Float, val alpha: Float)

/** A night desert: starry sky, a low moon, and layered sand dunes catching the moonlight. */
@Composable
fun DuneBackground(modifier: Modifier = Modifier) {
    // Fixed seed so the stars stay in the same place every time.
    val stars = remember {
        val random = Random(7)
        List(160) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat() * random.nextFloat() * 0.62f, // denser near the top
                radius = 0.4f + random.nextFloat() * 1.2f,
                alpha = 0.2f + random.nextFloat() * 0.7f,
            )
        }
    }

    Canvas(modifier) {
        drawRect(Brush.verticalGradient(0f to SkyTop, 0.35f to SkyMid, 0.62f to SkyHorizon))

        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.radius * density,
                center = Offset(star.x * size.width, star.y * size.height),
            )
        }

        drawMoon(center = Offset(size.width * 0.74f, size.height * 0.2f), radius = size.width * 0.045f)

        dunes.forEach { drawDune(it) }
    }
}

private fun DrawScope.drawMoon(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            0f to Moon.copy(alpha = 0.22f),
            1f to Color.Transparent,
            center = center,
            radius = radius * 5f,
        ),
        radius = radius * 5f,
        center = center,
    )
    drawCircle(Moon, radius, center)
}

private fun DrawScope.drawDune(dune: Dune) {
    val points = dune.crest.map { (x, y) -> Offset(x * size.width, y * size.height) }

    // Smooth curve through the crest points: each segment's control points sit at its horizontal midpoint.
    fun Path.traceRidge() {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val a = points[i - 1]
            val b = points[i]
            val midX = (a.x + b.x) / 2f
            cubicTo(midX, a.y, midX, b.y, b.x, b.y)
        }
    }
    val ridge = Path().apply { traceRidge() }
    val body = Path().apply {
        traceRidge()
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    val top = points.minOf { it.y }
    drawPath(
        path = body,
        brush = Brush.verticalGradient(0f to dune.lit, 1f to dune.shadow, startY = top, endY = top + size.height * 0.25f),
    )
    // Thin moonlit line along the crest, brightest under the moon.
    drawPath(
        path = ridge,
        brush = Brush.horizontalGradient(
            0f to Moon.copy(alpha = dune.ridgeAlpha * 0.2f),
            0.72f to Moon.copy(alpha = dune.ridgeAlpha),
            1f to Moon.copy(alpha = dune.ridgeAlpha * 0.4f),
        ),
        style = Stroke(width = 1.5f * density),
    )
}
