package org.neidhardt.arlocation

import com.google.ar.sceneform.math.Vector3
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationArNodeTest {

	@Test
	fun getScaleFixedSize() {
		// arrange
		val testDirection = Vector3(1f, 1f, 1f)
		// action
		val result = LocationArNode.getScaleFixedSize(testDirection)
		// verify
		assertEquals(1.73, result, 0.1)
	}

	@Test
	fun getScaleGradual() {
		// arrange
		val src = GlobalPosition(52.001, 13.001)
		val dst = GlobalPosition(52.002, 13.002)
		val testDirection = Vector3(1f, 1f, 1f)
		val distance = GlobalPositionUtils.geodeticCurve(
				src,
				dst
		).ellipsoidalDistance
		// action
		val result = LocationArNode.getScaleGradual(
				testDirection,
				src,
				dst
		)
		// verify
		assertEquals(
				LocationArNode.getScaleFixedSize(testDirection) * ArUtils.scaleFactorForDistance(distance),
				result,
				0.1
		)
	}
}