[![](https://jitpack.io/v/logickoder/perfect-freehand-kotlin.svg)](https://jitpack.io/#logickoder/perfect-freehand-kotlin)

# ![Screenshot](https://github.com/steveruizok/perfect-freehand-dart/blob/main/doc/assets/perfect-freehand-logo.svg "Perfect Freehand")

Draw perfect pressure-sensitive freehand lines.

🔗 A port of the [perfect-freehand](https://github.com/steveruizok/perfect-freehand) JavaScript
library. [Try out that demo](https://perfect-freehand-example.vercel.app/).

💕 Love this library?
Consider [becoming a sponsor](https://github.com/sponsors/steveruizok?frequency=recurring&sponsor=steveruizok).

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Community](#community)
- [Author](#author)

## Introduction

This package exports a function named `getStroke` that will generate the points for a polygon based
on a list of points.

![Screenshot](https://github.com/steveruizok/perfect-freehand-dart/blob/main/doc/assets/process.gif "A GIF showing a stroke with input points, outline points, and a curved path connecting these points")

To do this work, `getStroke` first creates a set of spline points (red) based on the input points (
grey) and then creates outline points (blue). You can render the result any way you like, using
whichever technology you prefer.

## Installation

This package is available on [jitpack.io](https://jitpack.io/). It can be used with or without
Android.

Add the jitpack repository to your `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
    }
}
```

```kotlin
dependencies {
    implementation("com.github.logickoder:perfect-freehand:1.0.0")
}
```

## Usage

This package exports a function named `getStroke` from the `PerfectFreehand` class that:

- accepts a list of points and several options
- returns a stroke outline as a list of points

```kotlin
import dev.logickoder.perfectfreehand.PerfectFreehand.getStroke

val myPoints = listOf(
    Point(0, 0),
    Point(1, 2),
    // etc...
)

val stroke = getStroke(myPoints)
```

You may also provide options as named parameters:

```kotlin
val stroke = getStroke(
    myPoints,
    size = 16f,
    thinning = 0.7f,
    smoothing = 0.5f,
    streamline = 0.5f,
    taperStart = 0f,
    taperEnd = 0f,
    capStart = true,
    capEnd = true,
    simulatePressure = true,
    isComplete = false,
)
```

To use real pressure, provide each point's pressure as a third parameter.

```kotlin
val myPoints = listOf(
    Point(0, 0, 0.2f),
    Point(1, 2, 0.3f),
    Point(2, 4, 0.4f),
    // etc...
)

val stroke = getStroke(myPoints, simulatePressure = false)
```

### Options

The optional parameters are:

| Property           | Type    | Default | Description                                                                                                |
|--------------------|---------|---------|------------------------------------------------------------------------------------------------------------|
| `size`             | Float   | 16f     | The base size (diameter) of the stroke.                                                                    |
| `thinning`         | Float   | .5f     | The effect of pressure on the stroke's size.                                                               |
| `smoothing`        | Float   | .5f     | How much to soften the stroke's edges.                                                                     |
| `streamline`       | Float   | .5f     | How much to remove variation from the input points.                                                        |
| `startTaper`       | Float   | 0f      | How far to taper the start of the line.                                                                    |
| `endTaper`         | Float   | 0f      | How far to taper the end of the line.                                                                      |
| `isComplete`       | Boolean | true    | Whether the stroke is complete.                                                                            |
| `simulatePressure` | Boolean | true    | Whether to simulate pressure based on distance between points, or else use the provided Points' pressures. |

**Note:** When the `last` property is `true`, the line's end will be drawn at the last input point,
rather than slightly behind it.

**Note:** The `cap` property has no effect when `taper` is more than zero.

**Tip:** To create a stroke with a steady line, set the `thinning` option to `0`.

**Tip:** To create a stroke that gets thinner with pressure instead of thicker, use a negative
number for the `thinning` option.

### Rendering

While `getStroke` returns a list of points representing the outline of a stroke, it's up to you to
decide how you will render these points. Check the **example project** to see how you might draw
these points in Compose using a `Canvas`.

```kotlin
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
```

### Advanced Usage

For advanced usage, the library also exports smaller functions that `getStroke` uses to generate its
outline points.

#### `getStrokePoints`

A function that accepts a list of Points and returns a set of `StrokePoints`. The path's total
length will be the `runningLength` of the last point in the array. Like `getStroke`, this function
also accepts any of the [optional named parameters](#options) listed above.

```kotlin
val myPoints = listOf(
    Point(0, 0),
    Point(1, 2),
    // etc...
)

val strokePoints = getStrokePoints(myPoints, size = 16f)
```

#### `getOutlinePoints`

A function that accepts a list of StrokePoints (i.e. the output of `getStrokePoint`) and returns a
list of Points defining the outline of a stroke. Like `getStroke`, this function also accepts any of
the [optional named parameters](#options) listed above.

```kotlin
val myPoints = listOf(
    Point(0, 0),
    Point(1, 2),
    // etc...
)

val myStrokePoints = getStrokePoints(myPoints, size = 16f)

val myOutlinePoints = getStrokeOutlinePoints(myStrokePoints, size = 16f)
```

**Note:** Internally, the `getStroke` function passes the result of `getStrokePoints`
to `getStrokeOutlinePoints`, just as shown in this example. This means that, in this example, the
result of `myOutlinePoints` will be the same as if the `myPoints` List had been passed
to `getStroke`.

## Community

### Support

Need help? Please [open an issue](https://github.com/logickoder/perfect-freehand-kotlin/issues/new)
for
support.

### Discussion

Have an idea or casual question? Visit
the [discussion page](https://github.com/logickoder/perfect-freehand-kotlin/discussions).

### License

- MIT
- ...but if you're using `perfect-freehand` in a commercial product,
  consider [becoming a sponsor](https://github.com/sponsors/steveruizok?frequency=recurring&sponsor=steveruizok).
  💰

## Author

- [@logickoder](https://twitter.com/logickoder)