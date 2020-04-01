package org.neidhardt.arlocation

import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GeodeticCurve
import org.gavaghan.geodesy.GlobalPosition as Pos

object GlobalPositionUtils {

	private val geoCalc = GeodeticCalculator()
	private val reference = Ellipsoid.WGS84

	internal fun geodeticCurve(src: GlobalPosition, dst: GlobalPosition): GeodeticCurve {
		val a = Pos(src.latitude, src.longitude, 0.0)
		val b = Pos(dst.latitude, dst.longitude, 0.0)
		return geoCalc.calculateGeodeticCurve(reference, a, b)
	}
}