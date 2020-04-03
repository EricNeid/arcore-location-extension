package org.neidhardt.arlocation

import org.junit.Test

import org.junit.Assert.*

class ArUtilsTest {

	@Test
	fun linearScaleFactor() {
		assertEquals(1.0, ArUtils.linearScaleFactor(5.0), 0.1)
		assertEquals(0.2, ArUtils.linearScaleFactor(100.0), 0.1)
		assertEquals(0.7, ArUtils.linearScaleFactor(47.5), 0.1)

		assertEquals(1.0, ArUtils.linearScaleFactor(4.0, 0.3, 200.0, 4.0), 0.1)
		assertEquals(0.3, ArUtils.linearScaleFactor(200.0, 0.3, 200.0, 4.0), 0.1)
	}

	@Test
	fun calculateArPosition_fromDistanceBearing() {
		// action
		val result = ArUtils.calculateArPosition(1.0, 0f, 90f)
		// verify
		assertEquals(1f, result.x)
		assertEquals(0f, result.z)
	}

	@Test
	fun calculateArPosition_fromGlobalPositions() {
		// arrange
		val cameraPosition = GlobalPosition(52.0, 13.0)
		val objectPosition = GlobalPositionUtils.endOfGeodeticCurve(cameraPosition, 90.0, 1.0)

		// action
		val result = ArUtils.calculateArPosition(cameraPosition, 0f, objectPosition)

		// verify
		assertEquals(1f, result.x)
		assertEquals(0f, result.z)
	}

	@Test
	fun testCalculateArPosition() {
	}
}