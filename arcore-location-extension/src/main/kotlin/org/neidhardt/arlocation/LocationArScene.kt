package org.neidhardt.arlocation

import android.location.Location
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import org.neidhardt.arlocation.misc.getDistance
import java.util.ArrayList

private const val LOCATION_CHANGED_THRESHOLD_M = 5

class LocationArScene(val arSceneView: ArSceneView) {

	private val tag = LocationArScene::class.java.simpleName

	private val locationMarkers = ArrayList<LocationMarker>()

	/**
	 * [previousLocation] represents the previous location of the user.
	 */
	var previousLocation: Location? = null
		private set

	/**
	 * [currentLocation] represents the current position of the user.
	 */
	var currentLocation: Location? = null
		private set

	/**
	 * [previousBearing] represents the previous orientation of the user.
	 * It is represented as azimuth in degree: [0,360].
	 */
	var previousBearing: Float? = null
		private set

	/**
	 * [currentBearing] represents the current orientation of the user.
	 * It is represented as azimuth in degree: [0,360].
	 */
	var currentBearing: Float? = null
		private set

	/**
	 * [trackingState] represents the current state of the ar scene.
	 */
	var trackingState: TrackingState? = null
		private set

	fun addMarker(locationMarker: LocationMarker) {
		locationMarkers.add(locationMarker)
		refreshSceneIfReady()
	}

	fun removeMarker(locationMarker: LocationMarker) {
		if (!locationMarkers.contains(locationMarker)) {
			Log.i(tag, "locationMarker was not found in list of rendered marker")
			return
		}
		locationMarker.anchorNode?.apply {
			anchor?.detach()
			isEnabled = false
		}
		locationMarker.anchorNode = null
		locationMarkers.remove(locationMarker)
		refreshSceneIfReady()
	}

	fun clearMarkers() {
		locationMarkers.forEach { marker ->
			marker.anchorNode?.apply {
				anchor?.detach()
				isEnabled = false
			}
			marker.anchorNode = null
		}
		locationMarkers.clear()
		refreshSceneIfReady()
	}

	fun onLocationChanged(newLocation: Location) {
		previousLocation = currentLocation
		currentLocation = newLocation

		val prev = previousLocation
		if (prev == null) {
			// first available location, just refresh
			refreshSceneIfReady()
		} else {
			// check if user moved far enough
			val distanceMoved = getDistance(
					prev.latitude, prev.longitude,
					newLocation.latitude, newLocation.longitude,
					0.0, 0.0
			)
			if (distanceMoved > LOCATION_CHANGED_THRESHOLD_M) {
				refreshSceneIfReady()
			}
		}
	}

	fun onBearingChanged(newBearing: Float) {
		previousBearing = currentBearing
		currentBearing = newBearing
		refreshSceneIfReady()
	}

	fun onSceneUpdate(frame: Frame) {
		trackingState = frame.camera.trackingState
		refreshSceneIfReady()
	}

	private fun refreshSceneIfReady() {
		if (trackingState != TrackingState.TRACKING) {
			return
		}
		// TODO
	}

}