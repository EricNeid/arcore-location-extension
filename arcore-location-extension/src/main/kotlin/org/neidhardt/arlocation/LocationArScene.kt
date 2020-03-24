package org.neidhardt.arlocation

import android.location.Location
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Vector3
import org.neidhardt.arlocation.misc.*
import java.util.*

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

	/**
	 * [maxRenderDistance] a maximum distance for the rendered markers. If the distance of a marker is
	 * larger than [maxRenderDistance], it's distance is reduced to [maxRenderDistance]. This prevents
	 * render issues with far off markers.
	 */
	var maxRenderDistance = 30.0

	fun addMarker(marker: LocationMarker) {
		locationMarkers.add(marker)
		refreshSceneIfReady()
	}

	fun removeMarker(marker: LocationMarker) {
		if (!locationMarkers.contains(marker)) {
			Log.i(tag, "locationMarker was not found in list of rendered marker")
			return
		}
		detachMarker(marker)
		locationMarkers.remove(marker)
	}

	fun clearMarkers() {
		locationMarkers.forEach {
			detachMarker(it)
		}
		locationMarkers.clear()
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

		val location = currentLocation ?: return
		val bearing = currentBearing ?: return
		val session = arSceneView.session ?: return

		for (marker in locationMarkers) {

			val distance = getDistance(
					marker.latitude,
					marker.longitude,
					location.latitude,
					location.longitude,
					0.0, 0.0
			)

			val bearingToMarker = getBearing(
					location.latitude,
					location.longitude,
					marker.latitude,
					marker.longitude
			)

			if (distance > marker.onlyRenderWhenWithin) {
				Log.i(tag, "Not rendering. Marker distance: $distance Max render distance: ${marker.onlyRenderWhenWithin}")
				continue
			}

			var renderDistance = distance

			// limit the distance of the Anchor within the scene, to prevent rendering issues
			if (renderDistance > maxRenderDistance) {
				renderDistance = maxRenderDistance
			}

			val positionRelativeToUser = calculateCartesianCoordinates(
					r = renderDistance,
					azimuth = (bearingToMarker - bearing).toFloat()
			)

			val height = marker.height

			detachMarker(marker)
			attachMarker(marker, session, positionRelativeToUser, height)
		}
	}

	private fun detachMarker(marker: LocationMarker) {
		marker.anchorNode?.apply {
			anchor?.detach()
			isEnabled = false
		}
		marker.anchorNode = null
	}

	private fun attachMarker(
			marker: LocationMarker,
			session: Session,
			positionRelativeToUser: CartesianTuple,
			height: Float
	) {
		val pos = floatArrayOf(
				positionRelativeToUser.x.toFloat(),
				-1.5f,
				-1f * positionRelativeToUser.y.toFloat()
		)
		val rotation = floatArrayOf(0f, 0f, 0f, 1f)
		val anchor = session.createAnchor(Pose(pos, rotation))


	}
}