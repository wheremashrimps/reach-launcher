package com.wheremashrimps.reach.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private const val WAVE_RADIUS = 3f
private val WAVE_SHIFT = 56.dp

/**
 * Vertical strip of index letters. Touching or dragging along it selects a letter, and
 * the letters near the finger bulge out to the left in a wave.
 */
@Composable
fun AlphabetScroller(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (letters.isEmpty()) return

    val haptics = LocalHapticFeedback.current
    val shiftPx = with(LocalDensity.current) { WAVE_SHIFT.toPx() }
    val currentLetters by rememberUpdatedState(letters)
    val currentOnSelected by rememberUpdatedState(onLetterSelected)
    val currentOnActive by rememberUpdatedState(onActiveChange)

    var heightPx by remember { mutableIntStateOf(1) }
    // Finger position in "letter units" (0 = first letter); NaN when not touching.
    var touchPos by remember { mutableFloatStateOf(Float.NaN) }
    var selected by remember { mutableIntStateOf(-1) }

    fun update(y: Float) {
        val count = currentLetters.size
        val pos = (y / heightPx * count).coerceIn(0f, count - 0.001f)
        touchPos = pos
        val index = pos.toInt()
        if (index != selected) {
            selected = index
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentOnSelected(currentLetters[index])
        }
    }

    Box(
        modifier = modifier
            .width(40.dp)
            .fillMaxHeight()
            .onSizeChanged { heightPx = it.height.coerceAtLeast(1) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    currentOnActive(true)
                    update(down.position.y)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        if (change.positionChange() != Offset.Zero) {
                            change.consume()
                            update(change.position.y)
                        }
                    }
                    touchPos = Float.NaN
                    selected = -1
                    currentOnActive(false)
                }
            },
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            letters.forEachIndexed { i, letter ->
                // 1 at the finger, falling to 0 at WAVE_RADIUS letters away.
                val strength = if (touchPos.isNaN()) 0f else {
                    val d = abs(i + 0.5f - touchPos) / WAVE_RADIUS
                    (1f - d).coerceAtLeast(0f).let { it * it }
                }
                val animated by animateFloatAsState(
                    targetValue = strength,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
                    label = "wave",
                )
                Text(
                    text = letter.toString(),
                    color = if (i == selected) Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationX = -shiftPx * animated
                            scaleX = 1f + animated
                            scaleY = 1f + animated
                        },
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
