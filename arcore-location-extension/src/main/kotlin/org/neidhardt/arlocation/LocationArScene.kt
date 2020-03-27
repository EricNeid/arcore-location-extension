package org.neidhardt.arlocation

import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Vector3
import org.neidhardt.arlocation.misc.calculateCartesianCoordinates
import org.neidhardt.arlocation.misc.geodeticCurve
import org.neidhardt.arlocation.misc.toRadians
import kotlin.math.cos
import kotlin.math.sin

private const val LOCATION_CHANGED_THRESHOLD_M = 5

@Suppress("unused", "MemberVisibilityCanBePrivate")
class LocationArScene(private val arSceneView: ArSceneView) {

	private val tag = LocationArScene::class.java.simpleName

	val locationMarkers = ArrayList<LocationArMarker>()
	private val renderedLocationMarkers = ArrayList<LocationArMarker>()

	/**
	 * [previousLocation] represents the position of the user, the last time the scene was refreshed.
	 */
	private var previousLocation: ArLocation? = null

	/**
	 * [currentLocation] represents the current position of the user.
	 */
	var currentLocation: ArLocation? = null
		private set

	/**
	 * [previousBearing] represents the bearing of the user, the last time the scene was refreshed.
	 * It is represented as azimuth in degree: [0,360].
	 */
	private var previousBearing: Float? = null

	/**
	 * [currentBearing] represents the current orientation of the user.
	 * It is represented as azimuth in degree: [0,360].
	 */
	var currentBearing: Float? = null
		private set

	/**
	 * [previousTrackingState] represents the previous state of the ar scene.
	 */
	private var previousTrackingState: TrackingState? = null

	/**
	 * [currentTrackingState] represents the current state of the ar scene.
	 */
	var currentTrackingState: TrackingState? = null
		private set

	/**
	 * [maxRenderDistance] a maximum distance for the rendered markers. If the distance of a marker is
	 * larger than [maxRenderDistance], it's distance is reduced to [maxRenderDistance]. This prevents
	 * render issues with far off markers. ARCore seems to fail if marker is further away than 30 meter.
	 */
	var maxRenderDistance = 25.0

	fun startLocationScene() {
		arSceneView.scene.addOnUpdateListener {
			arSceneView.arFrame?.let { onSceneUpdate(it) }
		}
	}

	fun addMarker(marker: LocationArMarker) {
		if (locationMarkers.contains(marker)) {
			Log.w(tag, "locationMarker already added to location scene")
			return
		}
		locationMarkers.add(marker)
		refreshSceneIfReady()
	}

	fun removeMarker(marker: LocationArMarker) {
		if (!locationMarkers.contains(marker)) {
			Log.w(tag, "locationMarker was not found in list markers")
			return
		}
		detachMarker(marker)
		locationMarkers.remove(marker)
		renderedLocationMarkers.remove(marker)
	}

	fun clearMarkers() {
		locationMarkers.forEach {
			detachMarker(it)
		}
		locationMarkers.clear()
		renderedLocationMarkers.clear()
	}

	fun onLocationChanged(newLocation: ArLocation) {
		val firstAvailableLocation = currentLocation == null
		currentLocation = newLocation

		if (firstAvailableLocation) {
			rememberState()
			refreshSceneIfReady()
		} else {
			// if previous location is available
			previousLocation?.let { prev ->
				// check if user moved far enough
				val distanceMoved = geodeticCurve(
						prev.latitude, prev.longitude,
						newLocation.latitude, newLocation.longitude
				).ellipsoidalDistance
				if (distanceMoved > LOCATION_CHANGED_THRESHOLD_M) {
					refreshSceneIfReady()
				}
			}
		}
	}

	fun onBearingChanged(newBearing: Float) {
		val firstAvailableBearing = currentBearing == null
		currentBearing = newBearing
		if (firstAvailableBearing) {
			refreshSceneIfReady()
		}
	}

	private fun onSceneUpdate(frame: Frame) {
		currentTrackingState = frame.camera.trackingState
		if (previousTrackingState != currentTrackingState) {
			refreshSceneIfReady()
		}
	}

	private fun rememberState() {
		previousLocation = currentLocation
		previousBearing = currentBearing
		previousTrackingState = currentTrackingState
	}

	fun refreshSceneIfReady() {
		Log.i(tag, "refreshSceneIfReady")

		rememberState()

		val trackingState = currentTrackingState ?: return
		val location = currentLocation ?: return
		val bearing = currentBearing ?: return
		val session = arSceneView.session ?: return
		val frame = arSceneView.arFrame ?: return

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
			val bearingToMarker = curve.azimuth.toFloat()

			if (marker.placementType == LocationArMarker.PlacementType.STATIC) {
				updateStaticMarker(marker, session, distance, bearing, bearingToMarker)
			}
			if (marker.placementType == LocationArMarker.PlacementType.DYNAMIC) {
				updateDynamicMarker(marker, session, frame, distance, bearing, bearingToMarker)
			}
		}
	}

	private fun updateStaticMarker(
			marker: LocationArMarker,
			session: Session,
			distance: Double,
			bearing: Float,
			bearingToMarker: Float
	) {
		if (renderedLocationMarkers.contains(marker)) {
			Log.d(tag, "Marker already rendered, skipping")
			return
		}
		if (distance > marker.onlyRenderWhenWithin) {
			Log.d(tag, "Marker not within range, distance: $distance, range: ${marker.onlyRenderWhenWithin}")
			detachMarker(marker)
			renderedLocationMarkers.remove(marker)
			return
		}
		detachMarker(marker)
		attachStaticMarker(marker, session, distance, bearing, bearingToMarker)
		renderedLocationMarkers.add(marker)
	}

	private fun updateDynamicMarker(
			marker: LocationArMarker,
			session: Session,
			frame: Frame,
			distance: Double,
			bearing: Float,
			bearingToMarker: Float
	) {
		// detach old marker
		detachMarker(marker)

		if (distance > marker.onlyRenderWhenWithin) {
			Log.d(tag, "Marker not within range, distance: $distance, range: ${marker.onlyRenderWhenWithin}")
			return
		}

		// limit the distance of the Anchor within the scene, to prevent rendering issues
		val renderDistance = if (distance > maxRenderDistance) {
			maxRenderDistance
		} else {
			distance
		}

		attachDynamicMarker(
				marker,
				frame,
				session,
				distance,
				renderDistance,
				bearing,
				bearingToMarker)

		if (!renderedLocationMarkers.contains(marker)) {
			renderedLocationMarkers.add(marker)
		}
	}

	private fun detachMarker(marker: LocationArMarker) {
		marker.anchorNode?.apply {
			anchor?.detach()
			isEnabled = false
		}
		marker.anchorNode = null
	}

	private fun attachStaticMarker(
			marker: LocationArMarker,
			session: Session,
			distance: Double,
			bearing: Float,
			bearingToMarker: Float
	) {
		// from distance (r) and bearing (azimuth) we can calculate the position in cartesian coordinates (x,y)
		// x (left/right) is equivalent to x in scene coordinates
		// y (distance) is equivalent to -1 * z in scene coordinates (values behind camera are positive)
		// height is equivalent to y in scene coordinates and is unaffected
		val positionRelativeToUser = calculateCartesianCoordinates(
				r = distance,
				azimuth = bearingToMarker - bearing
		)
		val pos = floatArrayOf(
				positionRelativeToUser.x.toFloat(),
				marker.height,
				-1f * positionRelativeToUser.y.toFloat()
		)
		val rotation = floatArrayOf(0f, 0f, 0f, 0f)
		val newAnchor = session.createAnchor(Pose(pos, rotation))

		marker.anchorNode = LocationArNode(newAnchor, marker, this).apply {
			setParent(arSceneView.scene)
			addChild(marker.node)
		}
	}

	private fun attachDynamicMarker(
			marker: LocationArMarker,
			frame: Frame,
			session: Session,
			markerDistance: Double,
			renderDistance: Double,
			bearing: Float,
			bearingToMarker: Float
	) {
		val rotation = bearingToMarker - bearing

		// if marker outside of render distance
		// raise it for better illusion of distance
		val heightAdjustment = if (renderDistance != markerDistance) {
			val cappedRealDistance = if (markerDistance > 500) {
				500.0
			} else {
				markerDistance
			}
			0.005 * (cappedRealDistance - renderDistance)
		} else {
			0.0
		}

		val z = -renderDistance.toFloat().coerceAtMost(maxRenderDistance.toFloat())
		val rotationRadian = rotation.toRadians()
		val zRotated = (z * cos(rotationRadian)).toFloat()
		val xRotated = (-(z * sin(rotationRadian))).toFloat()
		val y = (frame.camera.displayOrientedPose.ty() + marker.height + heightAdjustment).toFloat()

		marker.anchorNode?.apply {
			anchor?.detach()
			anchor = null
			isEnabled = false
		}
		marker.anchorNode = null

		// don't immediately assign newly created anchor in-case of exceptions
		val translation = Pose.makeTranslation(xRotated, y, zRotated)
		val newAnchor = session.createAnchor(
				frame.camera
						.displayOrientedPose
						.compose(translation)
						.extractTranslation()
		)

		marker.anchorNode = LocationArNode(newAnchor, marker, this).apply {
			setParent(arSceneView.scene)
			addChild(marker.node)
		}
		marker.node.localPosition = Vector3.zero()
	}
}