package org.neidhardt.arlocation.misc

import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GeodeticCurve
import org.gavaghan.geodesy.GlobalPosition
import kotlin.math.*

fun scaleFactorForDistance(distance: Double): Double {
	if (distance <= 5) {
		return 1.0
	}
	if (distance >= 100) {
		return 0.2
	}

	val a = (1.0 / 5.0) + (0.2 / 95.5) - (20.0 / 95.0)
	val b = (20.0 / 19.0) - (0.2 / 19.0)

	return a * distance + b
}

fun geodeticCurve(srcLat: Double, srcLon: Double, dstLat: Double, dstLon: Double): GeodeticCurve {
	val geoCalc = GeodeticCalculator()
	val reference = Ellipsoid.WGS84
	val a = GlobalPosition(srcLat, srcLon, 0.0)
	val b = GlobalPosition(dstLat, dstLon, 0.0)
	return geoCalc.calculateGeodeticCurve(reference, a, b)
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
