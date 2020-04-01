package org.neidhardt.arlocation

import kotlin.math.cos
import kotlin.math.sin

object ArUtils {

	/**
	 * [scaleFactorForDistance] returns the scale factor of ar object, depending on the distance.
	 * @param distance distance to marker
	 * @return scale factor
	 */
	fun scaleFactorForDistance(distance: Double): Double {
		return scaleFactorForDistance(distance, 0.2, 100.0, 5.0)
	}

	/**
	 * [scaleFactorForDistance] returns the scale factor of ar object, depending on the distance.
	 * @param distance distance to marker
	 * @param minScale minimum scale at maximum distance
	 * @param maxDistance maximum distance to scale down (size is minScale beyond this distance)
	 * @param minDistance minimum distance to apply scaling (size is 1 before this distance)
	 * @return scale factor
	 */
	fun scaleFactorForDistance(
			distance: Double,
			minScale: Double,
			maxDistance: Double,
			minDistance: Double
	): Double {
		if (distance <= 5) {
			return 1.0
		}
		if (distance >= maxDistance) {
			return minScale
		}

		val c = 1 / minDistance
		val d = minScale / minDistance
		val e = maxDistance / minDistance

		val m = (c - d) / (1.0 - e)
		val n = minScale - m * maxDistance

		return distance * m + n
	}

	fun calculateArPosition(
			cameraPosition: GlobalPosition,
			cameraBearing: Float,
			objectPosition: GlobalPosition
	): ArPosition {
		val curve = GlobalPositionUtils.geodeticCurve(
				cameraPosition,
				objectPosition
		)
		val r = curve.ellipsoidalDistance
		val bearing = curve.azimuth
		val alpha = (bearing - cameraBearing).toFloat()

		// r and alpha are polar coordinates, convert to cartesian for ar position
		val result = calculateCartesianCoordinates(r, alpha)
		return ArPosition(result.first.toFloat(), result.second.toFloat())
	}

	fun calculateArPosition(
			distance: Double,
			cameraBearing: Float,
			bearingToObject: Float
	): ArPosition {
		val rotation = (bearingToObject - cameraBearing)

		// r and alpha are polar coordinates, convert to cartesian for ar position
		val result = calculateCartesianCoordinates(
				r = distance,
				azimuth = rotation
		)
		return ArPosition(result.first.toFloat(), result.second.toFloat())
	}

	private fun calculateCartesianCoordinates(r: Double, azimuth: Float): Pair<Double,Double> {
		val x = cos((90 - azimuth).toRadians())
		val y = sin((90 - azimuth).toRadians())

		return Pair(r * x, r * y)
	}

}