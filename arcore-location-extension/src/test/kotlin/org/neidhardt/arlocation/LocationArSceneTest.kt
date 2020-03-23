package org.neidhardt.arlocation

import android.location.Location
import com.google.ar.sceneform.ArSceneView
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LocationArSceneTest {

	@Mock private lateinit var arSceneView: ArSceneView
	private lateinit var unit: LocationArScene

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		unit = LocationArScene(arSceneView)
	}

	@Test
	fun onLocationChanged() {
		// arrange
		val testData1 = mock<Location> {  }
		val testData2 = mock<Location> {  }
		// action
		unit.onLocationChanged(testData1)
		unit.onLocationChanged(testData2)
		// verify
		assertEquals(testData1, unit.previousLocation)
		assertEquals(testData2, unit.currentLocation)
	}

	@Test
	fun onBearingChanged() {
		// arrange
		val testData1 = 41f
		val testData2 = 42f
		// action
		unit.onBearingChanged(testData1)
		unit.onBearingChanged(testData2)
		// verify
		assertEquals(testData1, unit.previousBearing)
		assertEquals(testData2, unit.currentBearing)
	}
}