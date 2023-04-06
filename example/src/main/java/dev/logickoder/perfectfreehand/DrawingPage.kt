package dev.logickoder.perfectfreehand

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun DrawingPage(
    modifier: Modifier = Modifier,
    state: DrawingPageState = rememberDrawingPageState(),
) {
    val onTouchEvent: (MotionEvent) -> Boolean = remember {
        { state.handlePointerEvent(it) }
    }
    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        content = { scaffoldPadding ->
            Box(
                modifier = Modifier.padding(scaffoldPadding),
                content = {
                    Sketcher(
                        modifier = Modifier
                            .fillMaxSize(),
                        lines = state.lines,
                        options = state,
                    )
                    Sketcher(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInteropFilter(onTouchEvent = onTouchEvent),
                        lines = state.line.value?.let { listOf(it) } ?: emptyList(),
                        options = state,
                    )
                    Toolbar(
                        modifier = Modifier
                            .offset(x = (-24).dp, y = 24.dp)
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(.4f),
                        state = state,
                    )
                }
            )
        }
    )
}

private fun DrawingPageState.handlePointerEvent(event: MotionEvent): Boolean {
    val isStylus = event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS
    val offset = Offset(event.x, event.y)
    val point = Point(
        offset.x,
        offset.y,
        if (isStylus) event.pressure else Point.DEFAULT_PRESSURE
    )

    return when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            line.value = Stroke(listOf(point))
            true
        }

        MotionEvent.ACTION_MOVE -> {
            val points = line.value?.points ?: emptyList()
            line.value = Stroke(points + point)
            true
        }

        MotionEvent.ACTION_UP -> {
            if (line.value != null) {
                lines.add(line.value!!)
            }
            true
        }

        else -> false
    }
}

@Composable
private fun Toolbar(
    state: DrawingPageState,
    modifier: Modifier = Modifier,
) = with(state) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            ValueSlider(
                title = "Size",
                position = size,
                range = 1f..50f,
            )
            ValueSlider(
                title = "Thinning",
                position = thinning,
                range = -1f..1f,
            )
            ValueSlider(
                title = "Streamline",
                position = streamline,
                range = 0f..1f,
            )
            ValueSlider(
                title = "Smoothing",
                position = smoothing,
                range = 0f..1f,
            )
            ValueSlider(
                title = "Taper Start",
                position = taperStart,
                range = 0f..100f,
            )
            ValueSlider(
                title = "Taper End",
                position = taperEnd,
                range = 0f..100f,
            )
            ClearButton(::clear)
        }
    )
}

@Composable
private fun ClearButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            Text(
                text = "Clear",
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                )
            )
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onClick)
                    .padding(16.dp),
                painter = rememberVectorPainter(Icons.Default.Replay), // Set the icon to be displayed in the circle avatar
                contentDescription = "Clear",
                tint = Color.White, // Set the color of the icon
            )
        }
    )
}

@Composable
private fun ValueSlider(
    title: String,
    position: MutableState<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 100,
    range: ClosedFloatingPointRange<Float> = 0f..100f,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            Text(
                text = title,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                )
            )
            Slider(
                value = position.value,
                valueRange = range,
                steps = steps,
                onValueChange = { newPosition ->
                    position.value = newPosition
                },
            )
        }
    )
}

@Preview
@Composable
private fun DrawingPagePreview() = with(rememberDrawingPageState()) {
    DrawingPage(modifier = Modifier.fillMaxSize())
}