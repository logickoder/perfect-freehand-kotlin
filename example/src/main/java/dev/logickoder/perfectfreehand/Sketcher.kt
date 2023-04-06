package dev.logickoder.perfectfreehand

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.dp
import dev.logickoder.perfectfreehand.PerfectFreehand.getStroke


// Draw the strokes onto the canvas.
@Composable
fun Sketcher(
    lines: List<Stroke>,
    options: DrawingPageState,
    modifier: Modifier = Modifier,
) = Canvas(modifier = modifier, onDraw = {

    // Draw each stroke.
    lines.forEach { stroke ->
        // 1. Get the outline points from the input points
        val points = getStroke(
            points = stroke.points,
            size = options.size.value,
            thinning = options.thinning.value,
            smoothing = options.smoothing.value,
            streamline = options.streamline.value,
            taperStart = options.taperStart.value,
            capStart = options.capStart.value,
            taperEnd = options.taperEnd.value,
            capEnd = options.capEnd.value,
            simulatePressure = options.simulatePressure.value,
            isComplete = options.isComplete.value,
        )

        // 2. Render the points as a path
        val path = when {
            // If the outline points list is empty, we have nothing to draw.
            points.isEmpty() -> return@forEach
            // If the list only has one point, draw a dot.
            points.size == 1 -> Path().apply {
                val x = points.first().x
                val y = points.first().y
                addOval(
                    Rect(
                        x - 0.5f,
                        y - 0.5f,
                        x + 0.5f,
                        y + 0.5f,
                    )
                )
            }
            // Otherwise, draw a line that connects each point with a bezier curve segment.
            else -> Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.lastIndex) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    quadraticBezierTo(
                        p0.x,
                        p0.y,
                        (p0.x + p1.x) / 2,
                        (p0.y + p1.y) / 2
                    )
                }
            }
        }

        // Draw the path on the canvas.
        drawPath(
            path, style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = options.size.value.dp.toPx(),
                cap = if (options.capStart.value) StrokeCap.Round else StrokeCap.Butt,
                join = StrokeJoin.Round,
            ), color = Color.Black
        )
    }
})

