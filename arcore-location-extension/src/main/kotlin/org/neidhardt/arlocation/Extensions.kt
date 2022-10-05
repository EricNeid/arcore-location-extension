package org.neidhardt.arlocation

/**
 * Converts an angle measured in degrees to an approximately equivalent angle measured in radians.
 */
internal fun Float.toRadians(): Double {
	return Math.toRadians(this.toDouble())
}