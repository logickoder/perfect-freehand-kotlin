package dev.logickoder.perfectfreehand

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * @return a new [Point] object with the negated x and y coordinates of this point.
 */
operator fun Point.unaryMinus() = Point(-x, -y, pressure)

/**
 * Returns a new [Point] whose x and y coordinates are the sum of the corresponding
 * coordinates of this [Point] and [other].
 *
 * @param other the [Point] to add to this [Point].
 * @return a new [Point] with the sum of the two points' coordinates.
 */
operator fun Point.plus(other: Point) = Point(x + other.x, y + other.y, other.pressure)

/**
 * Subtracts the given [other] point from this point and returns a new point as the result.
 *
 * @param other The point to subtract from this point.
 * @return A new [Point] instance representing the vector from [other] to this point.
 */
operator fun Point.minus(other: Point) = Point(x - other.x, y - other.y, other.pressure)

/**
 * Multiplies the components of this point by the corresponding components of the [other] point,
 * producing a new point.
 *
 * @param other The point to multiply this point by.
 * @return A new [Point] with x equal to the product of this point's x and the [other] point's x,
 * y equal to the product of this point's y and the [other] point's y, and p equal to the [other] point's p.
 */
operator fun Point.times(other: Point) = Point(x * other.x, y * other.y, other.pressure)

/**
 * Divides this [Point] by another [Point] element-wise and returns a new [Point].
 *
 * The resulting [Point] has the same pressure as the divisor [Point].
 *
 * @param other The [Point] to divide by.
 * @return A new [Point] representing the element-wise division of this [Point] by [other].
 */
operator fun Point.div(other: Point) = Point(x / other.x, y / other.y, other.pressure)


/**
 * Multiplies this point by a scalar value [factor].
 * Returns a new [Point] with the resulting coordinates.
 *
 * @param factor The scalar value to multiply this point by.
 * @return A new [Point] with the resulting coordinates.
 */
operator fun Point.times(factor: Double) = Point(x * factor, y * factor, pressure)

/**
 * Divides this point by the given scaling factor.
 * Returns a new point representing the result of the division.
 *
 * @param factor the scaling factor to divide by
 * @return a new point representing the result of the division
 */
operator fun Point.div(factor: Double) = Point(x / factor, y / factor, pressure)


/**
 * Returns the perpendicular point of this point.
 *
 * The perpendicular point is a new point with the same pressure value as this point, but with the horizontal and
 * vertical components negated and swapped.
 *
 * @return The perpendicular point of this point.
 */
val Point.perpendicular get() = Point(y, -x, pressure)


/**
 * Returns a new point with the same direction as the given point, but with a magnitude of 1.
 *
 * @return the normalized point.
 */
val Point.unit get() = this / this.length

/**
 * Linearly interpolate between this [Point] and [b] with a given factor [t].
 *
 * @param b the end point
 * @param t the interpolation factor, must be between 0 and 1
 * @return the linearly interpolated point
 */
fun Point.lerp(b: Point, t: Double) = this + ((b - this) * t)

/**
 * Calculates the midpoint between this [Point] and [b].
 *
 * @param b the second point
 * @return the midpoint between this [Point] and [b]
 */
fun Point.midpoint(b: Point) = lerp(b, 0.5)


/**
 * Projects this [Point] onto the line defined by this [Point] and [b] at distance [d] from point [b].
 * Returns the resulting point.
 *
 * @param b The first point on the line.
 * @param d The distance from point [b] to the projected point.
 * @return The projected point.
 */
fun Point.project(b: Point, d: Double) = this + ((b - this) * (d / (b - this).length))

/**
 * Rotates a point around another point by the given angle in radians.
 *
 * @param c the center point to rotate around
 * @param r the angle to rotate by in radians
 * @return the rotated point
 */
fun Point.rotateAround(c: Point, r: Double): Point {
    val sin = sin(r)
    val cos = cos(r)
    val px = x - c.x
    val py = y - c.y
    val nx = px * cos - py * sin
    val ny = px * sin + py * cos
    return Point(nx + c.x, ny + c.y, pressure)
}

/**
 * @return The length of the point.
 */
val Point.length: Double get() = sqrt(lengthSqr())

/**
 * Returns the square of the length of the point from the origin (0, 0).
 *
 * @return the square of the length of the point
 */
fun Point.lengthSqr() = x * x + y * y

/**
 * Returns the distance between this [Point] and [b].
 *
 * @param b the second point
 * @return the distance between this [Point] and [b]
 */
fun Point.distance(b: Point): Double = (this - b).length

/**
 * Returns the square of the Euclidean distance between this [Point] and [b].
 *
 * @param b The second point
 * @return The square of the Euclidean distance between this [Point] and [b]
 */
fun Point.distanceSqr(b: Point) = (this - b).lengthSqr()

/**
 * Returns the dot product of two points, i.e this [Point] and point [b].
 *
 * @param b the second point
 * @return the dot product of A and B
 */
fun Point.dotProduct(b: Point) = x * b.x + y * b.y