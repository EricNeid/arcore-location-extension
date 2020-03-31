package org.neidhardt.arlocation.misc

import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GeodeticCurve
import org.gavaghan.geodesy.GlobalPosition
import kotlin.math.*

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

fun Float.toRadians(): Double {
	return Math.toRadians(this.toDouble())
}
