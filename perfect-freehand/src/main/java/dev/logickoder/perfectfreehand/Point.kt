package dev.logickoder.perfectfreehand

import dev.logickoder.perfectfreehand.Point.Companion.DEFAULT_PRESSURE
import java.util.Objects

/**
 * Represents a point in 2D space, with [x] and [y] coordinates.
 *
 * @param x The x-coordinate of the point.
 * @param y The y-coordinate of the point.
 * @param p The pressure of the point. Defaults to [DEFAULT_PRESSURE].
 */
class Point(
    val x: Double,
    val y: Double,
    val p: Double = DEFAULT_PRESSURE,
) {
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