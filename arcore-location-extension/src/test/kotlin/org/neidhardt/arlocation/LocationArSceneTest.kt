package org.neidhardt.arlocation

import com.google.ar.sceneform.ArSceneView
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class LocationArSceneTest {

	@Mock private lateinit var arSceneView: ArSceneView
	private lateinit var unit: LocationArScene

	@Before
	fun setUp() {
		arSceneView = mock {
			on { session } doReturn mock {  }
			on { scene } doReturn mock {  }
		}
		unit = LocationArScene(arSceneView)
	}

	@Test
	fun onLocationChanged() {
		// arrange
		val testData1 = GlobalPosition(0.0, 1.0)
		// action
		unit.onLocationChanged(testData1)
		// verify
		assertEquals(testData1, unit.currentLocation)
	}

	@Test
	fun onBearingChanged() {
		// arrange
		val testData1 = 41f
		// action
		unit.onBearingChanged(testData1)
		// verify
		assertEquals(testData1, unit.currentBearing)
	}
}