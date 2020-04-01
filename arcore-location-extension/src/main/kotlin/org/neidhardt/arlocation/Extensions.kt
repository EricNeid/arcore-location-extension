package org.neidhardt.arlocation

internal fun Float.toRadians(): Double {
	return Math.toRadians(this.toDouble())
}