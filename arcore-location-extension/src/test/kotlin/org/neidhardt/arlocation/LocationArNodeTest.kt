package org.neidhardt.arlocation

import com.google.ar.sceneform.math.Vector3
import org.junit.Assert.assertEquals
import org.junit.Test
import org.neidhardt.arlocation.misc.geodeticCurve
import org.neidhardt.arlocation.misc.scaleFactorForDistance

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
		val testDirection = Vector3(1f, 1f, 1f)
		// action
		val result = LocationArNode.getScaleGradual(
				testDirection,
				ArLocation(52.001, 13.001),
				ArLocation(52.002, 13.002)
		)
		// verify
		assertEquals(
				LocationArNode.getScaleFixedSize(testDirection) * scaleFactorForDistance(geodeticCurve(
						52.001, 13.001,
						52.002, 13.002
				).ellipsoidalDistance),
				result,
				0.1
		)
	}
}