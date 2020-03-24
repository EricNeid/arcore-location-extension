package org.neidhardt.arlocation.misc

import kotlin.math.*

fun getBearing(srcLat: Double, srcLon: Double, dstLat: Double, dstLon: Double): Double {
	val latitude1 = Math.toRadians(srcLat)
	val latitude2 = Math.toRadians(dstLat)
	val longDiff = Math.toRadians(dstLon - srcLon)
	val y = sin(longDiff) * cos(latitude2)
	val x = cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff)
	return (Math.toDegrees(atan2(y, x)) + 360) % 360
}

private const val R = 6371 // Radius of the earth

fun getDistance(
		lat1: Double, lon1: Double,
		lat2: Double, lon2: Double,
		el1: Double, el2: Double
): Double {
	val latDistance = Math.toRadians(lat2 - lat1)
	val lonDistance = Math.toRadians(lon2 - lon1)
	val a = (sin(latDistance / 2) * sin(latDistance / 2)
			+ (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
			* sin(lonDistance / 2) * sin(lonDistance / 2)))
	val c = 2 * atan2(sqrt(a), sqrt(1 - a))
	var distance = R * c * 1000 // convert to meters
	val height = el1 - el2
	distance = distance.pow(2.0) + height.pow(2.0)
	return sqrt(distance)
}

fun calculateCartesianCoordinates(r: Double, azimuth: Float): CartesianTuple {
	val x = cos((90 - azimuth).toRadians())
	val y = sin((90 - azimuth).toRadians())

	return CartesianTuple(r * x, r * y)
}

data class CartesianTuple(val x: Double, val y: Double)

private fun Float.toRadians(): Double {
	return Math.toRadians(this.toDouble())
}
