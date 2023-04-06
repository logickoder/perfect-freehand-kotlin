package dev.logickoder.perfectfreehand

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object PerfectFreehand {
    /**
     * This is the rate of change for simulated pressure. It could be an option.
     */
    private const val RATE_OF_PRESSURE_CHANGE = 0.275f

    /**
     * Compute a radius based on the pressure.
     *
     * @return the stroke's radius, given its size, thinning and pressure.
     */
    fun getStrokeRadius(size: Float, thinning: Float, pressure: Float): Float {
        return size * (0.5f - thinning * (0.5f - pressure))
    }

    /**
     * Get an array of points describing a polygon that surrounds the input [points].
     *
     * @param points The input points.
     * @param size Sets the base diameter for the shape. Defaults to 16.
     * @param thinning Sets the effect of pressure on the stroke's size. Defaults to 0.7.
     * @param smoothing Sets the density of points along the stroke's edges. Defaults to 0.5.
     * @param streamline Sets the level of variation allowed in the input points. Defaults to 0.5.
     * @param taperStart Sets the distance to taper the front of the stroke. Defaults to 0.0.
     * @param taperEnd Sets the distance to taper the end of the stroke. Defaults to 0.0.
     * @param capStart Whether to add a cap to the start of the stroke. Defaults to true.
     * @param capEnd Whether to add a cap to the end of the stroke. Defaults to true.
     * @param simulatePressure Whether to simulate pressure or use the point's provided pressures. Defaults to true.
     * @param isComplete Whether the line is complete. Defaults to false.
     *
     * @return An array of points describing a polygon that surrounds the input [points].
     */
    fun getStroke(
        points: List<Point>,
        size: Float = 16f,
        thinning: Float = 0.7f,
        smoothing: Float = 0.5f,
        streamline: Float = 0.5f,
        taperStart: Float = 0.0f,
        taperEnd: Float = 0.0f,
        capStart: Boolean = true,
        capEnd: Boolean = true,
        simulatePressure: Boolean = true,
        isComplete: Boolean = false,
    ): List<Point> {
        return getStrokeOutlinePoints(
            getStrokePoints(
                points,
                size = size,
                streamline = streamline,
                simulatePressure = simulatePressure,
                isComplete = isComplete,
            ),
            size = size,
            thinning = thinning,
            smoothing = smoothing,
            taperStart = taperStart,
            taperEnd = taperEnd,
            capStart = capStart,
            capEnd = capEnd,
            simulatePressure = simulatePressure,
            isComplete = isComplete,
        )
    }

    /**
     * Get an array of points representing the outline of a stroke, based on the provided [points].
     * Used internally by [getStroke] but possibly of separate interest. Accepts the result of [getStrokePoints].
     *
     * @param size the base diameter for the shape.
     *
     * @param thinning the effect of pressure on the stroke's size.
     *
     * @param smoothing the density of points along the stroke's edges.
     *
     * @param taperStart the distance to taper the front of the stroke.
     *
     * @param capStart whether to add a cap to the start of the stroke.
     *
     * @param taperEnd the distance to taper the end of the stroke.
     *
     * @param capEnd whether to add a cap to the end of the stroke.
     *
     * @param simulatePressure whether to simulate pressure or use the point's provided pressures.
     *
     * @param isComplete whether the line is complete.
     */
    fun getStrokeOutlinePoints(
        points: List<StrokePoint>,
        size: Float = 16.0f,
        thinning: Float = 0.7f,
        smoothing: Float = 0.5f,
        taperStart: Float = 0.0f,
        taperEnd: Float = 0.0f,
        capStart: Boolean = true,
        capEnd: Boolean = true,
        simulatePressure: Boolean = true,
        isComplete: Boolean = false,
    ): List<Point> {
        // We can't do anything with an empty array or a stroke with negative size.
        if (points.isEmpty() || size < 0) return emptyList()

        // The total length of the line
        val totalLength = points.last().runningLength

        // The minimum allowed distance between points (squared)
        val minDistance = (size * smoothing).pow(2)

        // Our collected left and right points
        val leftPoints = mutableListOf<Point>()
        val rightPoints = mutableListOf<Point>()

        // Previous pressure (start with average of first five pressures,
        // in order to prevent fat starts for every line. Drawn lines
        // almost always start slow!
        var previousPressure = points
            .take(10)
            .map { it.point.pressure }
            .reduceIndexed { index, acc, curr ->
                var pressure = curr

                if (simulatePressure) {
                    // Speed of change - how fast should the the pressure changing?
                    val sp = min(1f, points[index].distance / size)
                    // Rate of change - how much of a change is there?
                    val rp = min(1f, 1f - sp)
                    // Accelerate the pressure
                    pressure = min(
                        1f,
                        acc + (rp - acc) * (sp * RATE_OF_PRESSURE_CHANGE)
                    )
                }

                (acc + pressure) / 2
            }


        // The current radius
        var radius = getStrokeRadius(
            size,
            thinning,
            points.last().point.pressure,
        )
        var firstRadius: Float? = null

        var previousVector = points[0].vector

        // previous left and right points
        var previousLeft = points[0].point
        var previousRight = previousLeft

        // temporary left and right points
        var temporaryLeft = previousLeft
        var temporaryRight = previousRight

        // Keep track of whether the previous point is a sharp corner
        // so that we don't detect the same corner twice
        var isPrevPointSharpCorner = false

        // Find the outline's left and right points
        // Iterating through the points and populate the right and left points lists,
        // skipping the first and last points, which will get caps later on.
        for (i in points.indices) {
            val (point, vector, distance, runningLength) = points[i]
            var pressure = point.pressure

            // Removes noise from the end of the line
            if (i < points.lastIndex && totalLength - runningLength < 3) {
                continue
            }


            // Calculate the radius
            // If not thinning, the current point's radius will be half the size; or
            // otherwise, the size will be based on the current (real or simulated)
            // pressure.
            radius = if (thinning != 0f) {
                if (simulatePressure) {
                    // If we're simulating pressure, then do so based on the distance
                    // between the current point and the previous point, and the size
                    // of the stroke. Otherwise, use the input pressure.
                    val sp = min(1f, distance / size)
                    val rp = min(1f, 1 - sp)
                    pressure = min(
                        1f,
                        previousPressure + (rp - previousPressure) * (sp * RATE_OF_PRESSURE_CHANGE)
                    )
                }
                getStrokeRadius(size, thinning, pressure)
            } else size / 2f

            if (firstRadius == null) {
                firstRadius = radius
            }

            // Apply tapering
            // If the current length is within the taper distance at either the
            // start or the end, calculate the taper strengths. Apply the smaller
            // of the two taper strengths to the radius.

            val taperingStart = if (runningLength < taperStart) {
                runningLength / taperStart
            } else 1f

            val taperingEnd = if (totalLength - runningLength < taperEnd) {
                (totalLength - runningLength) / taperEnd
            } else 1f

            radius = max(0.01f, radius * taperingStart.coerceAtMost(taperingEnd))

            // Add points to left and right and Handle sharp corners
            // Find the difference (dot product) between the current and next vector.
            // If the next vector is at more than a right angle to the current vector,
            // draw a cap at the current point.
            val nextVector = if (i < points.lastIndex) points[i + 1].vector else points[i].vector
            val nextDotProduct = if (i < points.lastIndex) vector.dotProduct(nextVector) else 1f
            val previousDotProduct = vector.dotProduct(previousVector)

            val isPointSharpCorner = previousDotProduct < 0 && !isPrevPointSharpCorner
            val isNextPointSharpCorner = nextDotProduct < 0

            if (isPointSharpCorner || isNextPointSharpCorner) {
                // It's a sharp corner. Draw a rounded cap and move on to the next point
                // Considering saving these and drawing them later? So that we can avoid
                // crossing future points.

                val offset = previousVector.perpendicular * radius

                val step = 1f / 13f
                var angle = 0f
                while (angle <= 1f) {
                    temporaryLeft = (point - offset).rotateAround(point, Math.PI * angle)
                    leftPoints += temporaryLeft

                    temporaryRight = (point + offset).rotateAround(point, Math.PI * angle)
                    rightPoints += temporaryRight

                    angle += step
                }

                previousLeft = temporaryLeft
                previousRight = temporaryRight

                if (isNextPointSharpCorner) {
                    isPrevPointSharpCorner = true
                }
                continue
            }

            isPrevPointSharpCorner = false

            // Handle the last point
            if (i == points.lastIndex) {
                val offset = vector.perpendicular * radius
                leftPoints += point - offset
                rightPoints += point + offset
                continue
            }

            // Add regular points
            // Project points to either side of the current point, using the
            // calculated size as a distance. If a point's distance to the
            // previous point on that side greater than the minimum distance
            // (or if the corner is kinda sharp), add the points to the side's
            // points list.

            val offset = nextVector.lerp(vector, nextDotProduct).perpendicular * radius

            temporaryLeft = point - offset
            if (i <= 1 || previousLeft.distanceSqr(temporaryLeft) > minDistance) {
                leftPoints += temporaryLeft
                previousLeft = temporaryLeft
            }

            temporaryRight = point + offset
            if (i <= 1 || previousRight.distanceSqr(temporaryRight) > minDistance) {
                rightPoints += temporaryRight
                previousRight = temporaryRight
            }

            // Set variables for next iteration
            previousPressure = pressure
            previousVector = vector
        }

        // Drawing caps
        // Now that we have our points on either side of the line, we need to
        // draw caps at the start and end. Tapered lines don't have caps, but
        // may have dots for very short lines.

        val firstPoint = points.first().point
        val lastPoint = if (points.size > 1) {
            points.last().point
        } else firstPoint + Point(1, 1)

        // Draw a dot for very short or completed strokes
        // If the line is too short to gather left or right points and if the line is
        // not tapered on either side, draw a dot. If the line is tapered, then only
        // draw a dot if the line is both very short and complete. If we draw a dot,
        // we can just return those points.

        val isVeryShort = leftPoints.size <= 1 || rightPoints.size <= 1

        val startCap = mutableListOf<Point>()
        val endCap = mutableListOf<Point>()

        when {
            isVeryShort && (!(taperStart > 0 || taperEnd > 0) || isComplete) -> return buildList {
                val start = firstPoint.project(
                    (firstPoint - lastPoint).perpendicular.unit,
                    -(firstRadius ?: radius),
                )

                val step = 1f / 13f
                var angle = step

                while (angle <= 1f) {
                    add(start.rotateAround(firstPoint, Math.PI * 2 * angle))
                    angle += step
                }
            }
            // Draw a start cap
            // Unless the line has a tapered start, or unless the line has a tapered end
            // and the line is very short, draw a start cap around the first point. Use
            // the distance between the second left and right point for the cap's radius.
            // Finally remove the first left and right points. :psyduck:
            else -> {
                when {
                    taperStart > 0 || (taperEnd > 0 && isVeryShort) -> {
                        // The start point is tapered, noop
                    }

                    capStart -> {
                        // Draw the round cap - add thirteen points rotating the
                        // right point around the start point to the left point
                        val step = 1f / 13f
                        var angle = step

                        while (angle <= 1f) {
                            startCap += rightPoints.first()
                                .rotateAround(firstPoint, Math.PI * angle)
                            angle += step
                        }
                    }

                    else -> {
                        // Draw the flat cap - add a point to the left and right of the start point
                        val cornersVector = leftPoints.first() - rightPoints.first()

                        val offsetA = cornersVector * 0.5f
                        val offsetB = cornersVector * 0.51f

                        startCap.addAll(
                            listOf(
                                firstPoint - offsetA,
                                firstPoint - offsetB,
                                firstPoint + offsetB,
                                firstPoint + offsetA,
                            ),
                        )
                    }
                }

                // Draw an end cap
                // If the line does not have a tapered end, and unless the line has a tapered
                // start and the line is very short, draw a cap around the last point.
                // Finally, remove the last left and right points. Otherwise, add the last point.
                // Note that This cap is a full-turn-and-a-half: this prevents incorrect caps on
                // sharp end turns.

                val direction = (-points.last().vector).perpendicular
                when {
                    taperEnd > 0 || (taperStart > 0 && isVeryShort) -> {
                        // Tapered end - push the last point to the line
                        endCap += lastPoint
                    }

                    capEnd -> {
                        // Draw the round end cap
                        val start = lastPoint.project(direction, radius)
                        val step = 1f / 29f
                        var angle = step

                        while (angle < 1f) {
                            endCap += start.rotateAround(lastPoint, Math.PI * 3 * angle)
                            angle += step
                        }
                    }
                    // Draw the flat end cap
                    else -> endCap.addAll(
                        listOf(
                            lastPoint + direction * radius,
                            lastPoint + direction * (radius * 0.99f),
                            lastPoint - direction * (radius * 0.99f),
                            lastPoint - direction * radius
                        )
                    )
                }
            }
        }
        // Return the points in the correct winding order: begin on the left side,
        // then continue around the end cap, then come back along the right side,
        // and finally complete the start cap.
        return leftPoints + endCap + rightPoints.reversed() + startCap
    }

    /**
     * Get an array of [StrokePoint] objects with an adjusted point, pressure, vector, distance, and runningLength
     * for the provided [points]. Used internally by `getStroke` but possibly of separate interest.
     * Can be passed to `getStrokeOutlinePoints`.
     *
     * @param points The list of points to process.
     * @param size The base diameter for the shape. Defaults to 16.
     * @param streamline The level of variation allowed in the input points. Defaults to 0.5.
     * @param simulatePressure Whether to simulate pressure or use the point's provided pressures. Defaults to true.
     * @param isComplete Whether the line is complete. Defaults to false.
     *
     * @return A list of [StrokePoint]s.
     */
    fun getStrokePoints(
        points: List<Point>,
        size: Float = 16f,
        streamline: Float = 0.5f,
        simulatePressure: Boolean = true,
        isComplete: Boolean = false
    ): List<StrokePoint> = buildList {
        // if we don't have any points, return an empty list
        if (points.isEmpty()) return@buildList

        // find an interpolation level between the points
        val interpolationFactor = 0.15f + (1 - streamline) * 0.85f

        // If there's only one point, add another point at a 1pt offset
        val pts = kotlin.run {
            if (points.size == 1) {
                points + Point(points[0].x + 1, points[0].y + 1, points[0].pressure)
            } else points
        }

        // We're set this to the latest point, so we can use it to calculate
        // the distance and vector of the next point.
        var previous = StrokePoint(pts.first(), Point(1, 1), 0f, 0f)

        // add the first point, which needs no adjustment.
        add(previous)

        // We use the runningLength to keep track of the total distance
        var runningLength = 0f

        // A flag to see whether we've already reached out minimum length
        var hasReachedMinimumLength = false

        // Iterate through all of the points, creating StrokePoints.
        for (i in 1 until pts.size) {
            // If we're at the last point, and isComplete is true,
            // then add the actual input point.
            val point = if (isComplete && i == pts.size - 1) {
                pts[i]
            } else {
                // Otherwise, using the interpolationFactor calculated from the streamline
                // interpolate a new point between the previous point the current point.
                previous.point.lerp(pts[i], interpolationFactor).let {
                    if (!simulatePressure) {
                        // Use real pressure.
                        Point(it.x, it.y, pts[i].pressure)
                    } else {
                        it
                    }
                }
            }

            // If the new point is the same as the previous point, skip ahead.
            if (point == previous.point) {
                continue
            }

            // How far is the new point from the previous point?
            val distance = point.distance(previous.point)

            // Add this distance to the total "running length" of the line.
            runningLength += distance

            // At the start of the line, we wait until the new point is a
            // certain distance away from the original point, to avoid noise
            if (i < pts.lastIndex && !hasReachedMinimumLength) {
                if (runningLength < size) {
                    continue
                }
                hasReachedMinimumLength = true
            }

            // Create a new stroke point (it will be the new "previous" one).
            previous = StrokePoint(
                // The adjusted point
                point,
                // The vector from the current point to the previous point
                (previous.point - point).unit,
                // The distance between the current point and the previous point
                distance,
                // The total distance so far
                runningLength
            )

            // add the point to the final array
            add(previous)
        }

        // Set the vector of the first point to be the same as the second point.
        if (isNotEmpty()) {
            first().vector = this[1].vector
        }
    }
}