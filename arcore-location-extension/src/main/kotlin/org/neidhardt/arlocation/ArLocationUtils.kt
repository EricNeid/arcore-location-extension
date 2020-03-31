package org.neidhardt.arlocation

object ArLocationUtils {

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
}