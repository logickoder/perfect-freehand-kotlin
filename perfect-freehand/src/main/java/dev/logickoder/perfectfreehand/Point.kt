package dev.logickoder.perfectfreehand

import dev.logickoder.perfectfreehand.Point.Companion.DEFAULT_PRESSURE
import java.util.Objects

/**
 * Represents a point in 2D space, with [x] and [y] coordinates.
 *
 * @property x The x-coordinate of the point.
 * @property y The y-coordinate of the point.
 * @property pressure The pressure of the point. Defaults to [DEFAULT_PRESSURE].
 */
class Point(
    x: Number,
    y: Number,
    val pressure: Double = DEFAULT_PRESSURE,
) {
    val x = x.toDouble()

    val y = y.toDouble()

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other !is Point -> false
        else -> x == other.x && y == other.y
    }

    override fun hashCode() = Objects.hash(x, y)

    companion object {
        const val DEFAULT_PRESSURE = 0.5
    }
}