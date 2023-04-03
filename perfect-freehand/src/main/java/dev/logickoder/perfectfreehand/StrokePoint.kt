package dev.logickoder.perfectfreehand

/**
 * A processed point returned by `getStrokePoints`. Used as an input to `getStrokeOutlinePoints`.
 *
 * @property point The point's x and y coordinates and pressure.
 * @property vector The vector between this point and the previous point.
 * @property distance The distance from this point and the previous point.
 * @property runningLength The running length of the line at this point.
 */
data class StrokePoint(
    val point: Point,
    var vector: Point,
    val distance: Double,
    val runningLength: Double
)
