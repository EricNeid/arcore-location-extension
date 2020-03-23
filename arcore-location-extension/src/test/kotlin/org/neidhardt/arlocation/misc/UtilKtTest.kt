package org.neidhardt.arlocation.misc

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilKtTest {

	@Test
	fun bearing() {
		// action
		val result = getBearing(
				52.511414, 13.507132,
				54.799974, 13.507132
		)
		// verify
		assertEquals(0.0, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn31() {
		// action
		val result: Double = getBearing(
				52.511414, 13.507132,
				52.512414, 13.508132
		)
		// verify
		assertEquals(31.3, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn90() {
		// action
		val result: Double = getBearing(
				52.511414, 13.507132,
				52.511414, 15.507132
		)
		// verify
		assertEquals(89.2, result, 0.1)
	}

	@Test
	fun bearing_shouldReturn180() {
		// action
		val result: Double = getBearing(
				52.511414, 13.507132,
				48.799974, 13.507132
		)
		// verify
		assertEquals(180.0, result, 0.1)
	}

	@Test
	fun distance() { // action
		val result: Double = getDistance(
				52.589738, 11.172685,
				52.591484, 11.238992,
				0.0, 0.0
		)
		// verify
		assertEquals(4497.859, result, 15.0)
	}
}