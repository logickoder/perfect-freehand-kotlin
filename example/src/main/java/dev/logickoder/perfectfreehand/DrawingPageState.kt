package dev.logickoder.perfectfreehand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Options for configuring a stroke.
 * @property size The base size (diameter) of the stroke.
 * @property thinning The effect of pressure on the stroke's size.
 * @property smoothing Controls the density of points along the stroke's edges.
 * @property streamline Controls the level of variation allowed in the input points.
 * @property simulatePressure Whether to simulate pressure or use the point's provided pressures.
 * @property taperStart The distance to taper the front of the stroke.
 * @property taperEnd The distance to taper the end of the stroke.
 * @property capStart Whether to add a cap to the start of the stroke.
 * @property capEnd Whether to add a cap to the end of the stroke.
 * @property isComplete Whether the line is complete.
 */
class DrawingPageState {
    val size = mutableStateOf(16.0f)
    val thinning = mutableStateOf(0.7f)
    val smoothing = mutableStateOf(0.5f)
    val streamline = mutableStateOf(0.5f)
    val simulatePressure = mutableStateOf(true)
    val taperStart = mutableStateOf(0.0f)
    val capStart = mutableStateOf(true)
    val taperEnd = mutableStateOf(0.0f)
    val capEnd = mutableStateOf(true)
    val isComplete = mutableStateOf(false)

    val lines = mutableStateListOf<Stroke>()
    val line = mutableStateOf<Stroke?>(null)

    fun clear() {
        lines.clear()
        line.value = null
    }
}

@Composable
fun rememberDrawingPageState() = remember {
    DrawingPageState()
}