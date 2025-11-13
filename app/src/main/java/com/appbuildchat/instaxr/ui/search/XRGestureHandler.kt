package com.appbuildchat.instaxr.ui.search

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs

/**
 * Gesture handler for XR spherical navigation
 * Detects swipes, drags, and taps for navigating the spherical grid
 */
class XRGestureHandler(
    private val onRotateViewport: (angleDelta: Float) -> Unit,
    private val onScrollVertical: (delta: Float) -> Unit,
    private val onTap: (Offset) -> Unit = {}
) {
    // Sensitivity multipliers
    private val rotationSensitivity = 0.3f
    private val scrollSensitivity = 1.0f

    // Threshold to differentiate horizontal vs vertical gestures
    private val directionThreshold = 20f

    /**
     * Apply gesture handling to a composable
     */
    @Composable
    fun Modifier.handleGestures(): Modifier {
        var initialOffset by remember { mutableStateOf<Offset?>(null) }
        var dragDirection by remember { mutableStateOf<DragDirection?>(null) }

        return this.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    initialOffset = offset
                    dragDirection = null
                },
                onDragEnd = {
                    initialOffset = null
                    dragDirection = null
                },
                onDragCancel = {
                    initialOffset = null
                    dragDirection = null
                },
                onDrag = { change, dragAmount ->
                    change.consume()

                    // Determine drag direction if not yet set
                    if (dragDirection == null && initialOffset != null) {
                        val totalDrag = change.position - initialOffset!!
                        if (abs(totalDrag.x) > directionThreshold || abs(totalDrag.y) > directionThreshold) {
                            dragDirection = if (abs(totalDrag.x) > abs(totalDrag.y)) {
                                DragDirection.HORIZONTAL
                            } else {
                                DragDirection.VERTICAL
                            }
                        }
                    }

                    // Handle drag based on direction
                    when (dragDirection) {
                        DragDirection.HORIZONTAL -> {
                            // Horizontal drag rotates the viewport
                            onRotateViewport(dragAmount.x * rotationSensitivity)
                        }
                        DragDirection.VERTICAL -> {
                            // Vertical drag scrolls the grid
                            onScrollVertical(dragAmount.y * scrollSensitivity)
                        }
                        null -> {
                            // Still determining direction, do nothing
                        }
                    }
                }
            )
        }.pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    onTap(offset)
                }
            )
        }
    }

    /**
     * Direction of drag gesture
     */
    private enum class DragDirection {
        HORIZONTAL,
        VERTICAL
    }
}

/**
 * Remember a gesture handler with the given callbacks
 */
@Composable
fun rememberXRGestureHandler(
    onRotateViewport: (angleDelta: Float) -> Unit,
    onScrollVertical: (delta: Float) -> Unit,
    onTap: (Offset) -> Unit = {}
): XRGestureHandler {
    return remember(onRotateViewport, onScrollVertical, onTap) {
        XRGestureHandler(
            onRotateViewport = onRotateViewport,
            onScrollVertical = onScrollVertical,
            onTap = onTap
        )
    }
}

/**
 * Extension function to apply XR gestures to any Modifier
 */
@Composable
fun Modifier.xrGestures(
    onRotateViewport: (angleDelta: Float) -> Unit,
    onScrollVertical: (delta: Float) -> Unit,
    onTap: (Offset) -> Unit = {}
): Modifier {
    val handler = rememberXRGestureHandler(onRotateViewport, onScrollVertical, onTap)
    return with(handler) {
        this@xrGestures.handleGestures()
    }
}
