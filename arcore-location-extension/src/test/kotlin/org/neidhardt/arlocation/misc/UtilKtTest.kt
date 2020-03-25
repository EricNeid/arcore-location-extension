package org.neidhardt.arlocation.misc

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilKtTest {

	@Test
	fun bearing() {
		// action
		val result = geodeticCurve(
				52.511414, 13.507132,
				54.799974, 13.507132
		).azimuth
		// verify
		assertEquals(0.0, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn31() {
		// action
		val result: Double = geodeticCurve(
				52.511414, 13.507132,
				52.512414, 13.508132
		).azimuth
		// verify
		assertEquals(31.3, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn90() {
		// action
		val result: Double = geodeticCurve(
				52.511414, 13.507132,
				52.511414, 15.507132
		).azimuth
		// verify
		assertEquals(89.2, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn180() {
		// action
		val result: Double = geodeticCurve(
				52.511414, 13.507132,
				48.799974, 13.507132
		).azimuth
		// verify
		assertEquals(180.0, result, 0.1)
	}

	@Test
	fun distance() { // action
		val result = geodeticCurve(
				52.589738, 11.172685,
				52.591484, 11.238992
		).ellipsoidalDistance
		// verify
		assertEquals(4497.859, result, 15.0)
	}

	@Test
	fun calculateCartesianCoordinates() {
		// action
		val result1 = calculateCartesianCoordinates(2.0, 0f)
		// verify
		// that result is directly in front of user
		assertEquals(0.0, result1.x, 0.1)
		assertEquals(2.0, result1.y, 0.1)

		// action
		val result2 = calculateCartesianCoordinates(2.0, 90f)
		// verify
		// that result is directly to right of user
		assertEquals(2.0, result2.x, 0.1)
		assertEquals(0.0, result2.y, 0.1)
	}

	@Test
	fun scaleFactorForDistance() {
		assertEquals(1.0,
				scaleFactorForDistance(5.0), 0.1)
		assertEquals(1.0,
				scaleFactorForDistance(4.0), 0.1)
		assertEquals(0.2,
				scaleFactorForDistance(100.0), 0.1)
		assertEquals(0.2,
				scaleFactorForDistance(101.0), 0.1)
		assertEquals(0.7,
				scaleFactorForDistance(47.5), 0.1)
	}
}