package org.neidhardt.arlocation

import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import org.neidhardt.arlocation.misc.calculateCartesianCoordinates
import org.neidhardt.arlocation.misc.geodeticCurve
import org.neidhardt.arlocation.misc.getDistance
import java.util.*

private const val LOCATION_CHANGED_THRESHOLD_M = 5

class LocationArScene(private val arSceneView: ArSceneView) {

	private val tag = LocationArScene::class.java.simpleName

	private val locationMarkers = ArrayList<LocationArMarker>()

	/**
	 * [previousLocation] represents the previous location of the user.
	 */
	var previousLocation: ArLocation? = null
		private set

	/**
	 * [currentLocation] represents the current position of the user.
	 */
	var currentLocation: ArLocation? = null
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
	 * [previousTrackingState] represents the previous state of the ar scene.
	 */
	var previousTrackingState: TrackingState? = null
		private set

	/**
	 * [currentTrackingState] represents the current state of the ar scene.
	 */
	var currentTrackingState: TrackingState? = null
		private set

	/**
	 * [maxRenderDistance] a maximum distance for the rendered markers. If the distance of a marker is
	 * larger than [maxRenderDistance], it's distance is reduced to [maxRenderDistance]. This prevents
	 * render issues with far off markers.
	 */
	var maxRenderDistance = 30.0

	init {
		arSceneView.scene.addOnUpdateListener {
			arSceneView.arFrame?.let { onSceneUpdate(it) }
		}
	}

	fun addMarker(marker: LocationArMarker) {
		locationMarkers.add(marker)
		refreshSceneIfReady()
	}

	fun removeMarker(marker: LocationArMarker) {
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

	fun onLocationChanged(newLocation: ArLocation) {
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
		// first available bearing
		if (previousBearing == null) {
			refreshSceneIfReady()
		}
	}

	fun onSceneUpdate(frame: Frame) {
		previousTrackingState = currentTrackingState
		currentTrackingState = frame.camera.trackingState
		if (previousTrackingState != currentTrackingState) {
			refreshSceneIfReady()
		}
	}

	private fun refreshSceneIfReady() {
		Log.i(tag, "refreshSceneIfReady")

		val trackingState = currentTrackingState ?: return
		val location = currentLocation ?: return
		val bearing = currentBearing ?: return
		val session = arSceneView.session ?: return

		if (trackingState != TrackingState.TRACKING) {
			return
		}

		Log.i(tag, "refreshSceneIfReady $trackingState, $location, $bearing")

		for (marker in locationMarkers) {

			val curve = geodeticCurve(
					location.latitude,
					location.longitude,
					marker.latitude,
					marker.longitude
			)

			val distance = curve.ellipsoidalDistance
			val bearingToMarker = curve.azimuth

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

			val heightOffset = 0f

			detachMarker(marker)
			attachMarker(
					marker,
					session,
					positionRelativeToUser.x.toFloat(),
					positionRelativeToUser.y.toFloat(),
					heightOffset
			)
		}
	}

	private fun detachMarker(marker: LocationArMarker) {
		marker.anchorNode?.apply {
			anchor?.detach()
			isEnabled = false
		}
		marker.anchorNode = null
	}

	private fun attachMarker(
			marker: LocationArMarker,
			session: Session,
			dX: Float,
			dY: Float,
			heightOffset: Float
	) {
		val pos = floatArrayOf(
				dX,
				marker.height + heightOffset,
				-1f * dY
		)
		val rotation = floatArrayOf(0f, 0f, 0f, 1f)
		val newAnchor = session.createAnchor(Pose(pos, rotation))

		marker.anchorNode = LocationArNode(newAnchor, marker, this).apply {
			setParent(arSceneView.scene)
			addChild(marker.node)
		}
	}
}