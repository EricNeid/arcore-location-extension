package org.neidhardt.arlocation

import org.junit.Assert.assertEquals
import org.junit.Test
import org.neidhardt.arlocation.GlobalPositionUtils.geodeticCurve

class GlobalPositionUtilsTest {

	@Test
	fun bearing() {
		// action
		val result = geodeticCurve(
				GlobalPosition(52.511414, 13.507132),
				GlobalPosition(54.799974, 13.507132)
		).azimuth
		// verify
		assertEquals(0.0, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn31() {
		// action
		val result = geodeticCurve(
				GlobalPosition(52.511414, 13.507132),
				GlobalPosition(52.512414, 13.508132)
		).azimuth
		// verify
		assertEquals(31.3, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn90() {
		// action
		val result: Double = geodeticCurve(
				GlobalPosition(52.511414, 13.507132),
				GlobalPosition(52.511414, 15.507132)
		).azimuth
		// verify
		assertEquals(89.2, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn180() {
		// action
		val result = geodeticCurve(
				GlobalPosition(52.511414, 13.507132),
				GlobalPosition(48.799974, 13.507132)
		).azimuth
		// verify
		assertEquals(180.0, result, 0.1)
	}

	@Test
	fun distance() { // action
		val result = geodeticCurve(
				GlobalPosition(52.589738, 11.172685),
				GlobalPosition(52.591484, 11.238992)
		).ellipsoidalDistance
		// verify
		assertEquals(4497.859, result, 15.0)
	}

}