package org.neidhardt.arlocation

import org.junit.Test

import org.junit.Assert.*

class ArLocationUtilsTest {

	@Test
	fun scaleFactorForDistance() {
		assertEquals(1.0,
				ArLocationUtils.scaleFactorForDistance(5.0),
				0.1)
		assertEquals(1.0,
				ArLocationUtils.scaleFactorForDistance(4.0),
				0.1)
		assertEquals(0.2,
				ArLocationUtils.scaleFactorForDistance(100.0),
				0.1)
		assertEquals(0.2,
				ArLocationUtils.scaleFactorForDistance(101.0),
				0.1)
		assertEquals(0.7,
				ArLocationUtils.scaleFactorForDistance(47.5),
				0.1)
	}
}