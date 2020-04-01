package org.neidhardt.arlocation

import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import kotlin.math.cos
import kotlin.math.sin

private const val LOCATION_UPDATE_THRESHOLD_M = 5

@Suppress("unused", "MemberVisibilityCanBePrivate")
class LocationArScene(private val arSceneView: ArSceneView) {

	private val tag = LocationArScene::class.java.simpleName

	private val locationMarkers = ArrayList<LocationArMarker>()
	private val renderedLocationMarkers = ArrayList<LocationArMarker>()

	/**
	 * [locationUpdateThreshold] represents the minimum distance between 2 calls of [onLocationChanged]
	 * for scene refresh to be triggered. If the distance between the location of the last update and
	 * the current update is smaller than the threshold, the scene is not refreshed.
	 */
	var locationUpdateThreshold = LOCATION_UPDATE_THRESHOLD_M
	set(value) {
		if (value < 0) throw IllegalArgumentException("Threshold must be <= 0")
		field = value
	}

	/**
	 * [previousLocation] represents the position of the user, the last time the scene was refreshed.
	 */
	private var previousLocation: GlobalPosition? = null

	/**
	 * [currentLocation] represents the current position of the user.
	 */
	var currentLocation: GlobalPosition? = null
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

	/**
	 * [getMarkersInScene] returns a list containing all Markers currently added to the scene.
	 * Changes to this list have no impact on the current visualization.
	 *
	 * @return list of location markers
	 */
	fun getMarkersInScene(): List<LocationArMarker> {
		return ArrayList<LocationArMarker>().apply {
			addAll(locationMarkers)
		}
	}

	/**
	 * [getRenderedMarkersInScene] returns a list containing all Markers currently added
	 * and rendered to the scene. Changes to this list have no impact on the current visualization.
	 *
	 * @return list of location markers
	 */
	fun getRenderedMarkersInScene(): List<LocationArMarker> {
		return ArrayList<LocationArMarker>().apply {
			addAll(renderedLocationMarkers)
		}
	}

	fun addMarker(marker: LocationArMarker) {
		if (locationMarkers.contains(marker)) {
			Log.w(tag, "locationMarker already added to location scene")
			return
		}
		locationMarkers.add(marker)
		refreshMarker(marker)
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

	/**
	 * [onLocationChanged] updates the current user location in the location scene.
	 * If no location was set so far, this triggers [refreshSceneIfReady].
	 * If the distance between the new location and the location of the last update is
	 * larger than [locationUpdateThreshold], this triggers [refreshSceneIfReady].
	 */
	fun onLocationChanged(newLocation: GlobalPosition) {
		val firstAvailableLocation = currentLocation == null
		currentLocation = newLocation

		if (firstAvailableLocation) {
			rememberState()
			refreshSceneIfReady()
		} else {
			// if previous location is available
			previousLocation?.let { prev ->
				// check if user moved far enough
				val distanceMoved = GlobalPositionUtils.geodeticCurve(
						prev,
						newLocation
				).ellipsoidalDistance
				if (distanceMoved > locationUpdateThreshold) {
					refreshSceneIfReady()
				}
			}
		}
	}

	/**
	 * [onBearingChanged] updates the current user bearing in the location scene.
	 * If no bearing was set so far, this triggers [refreshSceneIfReady].
	 */
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

	/**
	 * [refreshSceneIfReady] refreshes the current scene and redraws all marker currently added.
	 */
	fun refreshSceneIfReady() {
		Log.i(tag, "refreshSceneIfReady")

		rememberState()

		val trackingState = currentTrackingState ?: return
		if (trackingState != TrackingState.TRACKING) {
			return
		}

		for (marker in locationMarkers) {
			refreshMarker(marker)
		}
	}

	/**
	 * [refreshMarker] refreshes the given location marker.
	 *
	 * @param marker to update
	 */
	fun refreshMarker(marker: LocationArMarker) {
		if (!locationMarkers.contains(marker)) {
			throw IllegalArgumentException("Given marker is not part of this scene, call addMarker instead")
		}

		val location = currentLocation ?: return
		val bearing = currentBearing ?: return
		val session = arSceneView.session ?: return
		val frame = arSceneView.arFrame ?: return
		val trackingState = frame.camera.trackingState ?: return

		if (trackingState != TrackingState.TRACKING) {
			return
		}

		val curve = GlobalPositionUtils.geodeticCurve(
				location,
				marker.globalPosition
		)

		val distance = curve.ellipsoidalDistance
		val bearingToMarker = curve.azimuth.toFloat()

		if (marker.placementType == LocationArMarker.PlacementType.STATIC) {
			refreshStaticMarker(marker, session, distance, bearing, bearingToMarker)
		}
		if (marker.placementType == LocationArMarker.PlacementType.DYNAMIC) {
			refreshDynamicMarker(marker, session, frame, distance, bearing, bearingToMarker)
		}
	}

	private fun refreshStaticMarker(
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
		attachStaticMarker(session, marker, distance, bearing, bearingToMarker)
		renderedLocationMarkers.add(marker)
	}

	private fun refreshDynamicMarker(
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
				session,
				frame,
				marker,
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
			session: Session,
			marker: LocationArMarker,
			distance: Double,
			userBearing: Float,
			bearingToMarker: Float
	) {
		val arPosition = ArUtils.calculateArPosition(
				distance = distance,
				cameraBearing =  userBearing,
				bearingToObject = bearingToMarker
		)

		val pos = floatArrayOf(
				arPosition.x,
				marker.height,
				-1f * arPosition.z
		)
		val rotation = floatArrayOf(0f, 0f, 0f, 0f)
		val newAnchor = session.createAnchor(Pose(pos, rotation))

		marker.anchorNode = LocationArNode(newAnchor, marker, this).apply {
			setParent(arSceneView.scene)
			addChild(marker.node)
		}
	}

	private fun attachDynamicMarker(
			session: Session,
			frame: Frame,
			marker: LocationArMarker,
			markerDistance: Double,
			renderDistance: Double,
			userBearing: Float,
			bearingToMarker: Float
	) {
		val rotation = bearingToMarker - userBearing

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
	}
}