package org.neidhardt.arlocation

import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import org.neidhardt.arlocation.misc.calculateCartesianCoordinates
import org.neidhardt.arlocation.misc.geodeticCurve
import org.neidhardt.arlocation.misc.getDistance
import java.util.*
import kotlin.math.*

private const val LOCATION_CHANGED_THRESHOLD_M = 5
private const val MAX_RENDER_DISTANCE = 25f

@Suppress("unused", "MemberVisibilityCanBePrivate")
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
	 * render issues with far off markers. ARCore seems to fail if marker is further away than 30 meter.
	 */
	var maxRenderDistance = 25.0

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

	private fun onSceneUpdate(frame: Frame) {
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
		val frame = arSceneView.arFrame ?: return

		if (trackingState != TrackingState.TRACKING) {
			return
		}

		Log.i(tag, "refreshSceneIfReady $trackingState, $location, $bearing")

		for (marker in locationMarkers) {
			// detach old marker
			detachMarker(marker)

			val curve = geodeticCurve(
					location.latitude,
					location.longitude,
					marker.latitude,
					marker.longitude
			)

			val distance = curve.ellipsoidalDistance
			val bearingToMarker = curve.azimuth.toFloat()

			if (distance > marker.onlyRenderWhenWithin) {
				Log.i(tag, "Not rendering. Marker distance: $distance Max render distance: ${marker.onlyRenderWhenWithin}")
				continue
			}

			var renderDistance = distance

			// limit the distance of the Anchor within the scene, to prevent rendering issues
			if (renderDistance > maxRenderDistance) {
				renderDistance = maxRenderDistance
			}

			if (marker.placementType == LocationArMarker.PlacementType.STATIC) {
				attachStaticMarker(
						marker,
						session,
						renderDistance,
						bearing,
						bearingToMarker
				)
			}
			if (marker.placementType == LocationArMarker.PlacementType.DYNAMIC) {
				attachDynamicMarker(
						marker,
						frame,
						session,
						distance,
						renderDistance,
						bearing,
						bearingToMarker)
			}
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
		val positionRelativeToUser = calculateCartesianCoordinates(
				r = distance,
				azimuth = (bearingToMarker - bearing)
		)

		val pos = floatArrayOf(
				positionRelativeToUser.x.toFloat(),
				marker.height,
				-1f * positionRelativeToUser.y.toFloat()
		)
		val rotation = floatArrayOf(0f, 0f, 0f, 0f)
		val newAnchor = session.createAnchor(Pose(pos, rotation))

		marker.anchorNode = LocationArNode(newAnchor, marker).apply {
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
		var markerBearing = bearingToMarker - bearing
		markerBearing += 360
		markerBearing %= 360

		val rotation = floor(markerBearing)

		// adjustment to add markers on horizon, instead of just directly in front of camera
		var heightAdjustment = 0.0
		// raise distant markers for better illusion of distance
		val cappedRealDistance = if (markerDistance > 500) {
			500.0
		} else {
			markerDistance
		}
		if (renderDistance != markerDistance) {
			heightAdjustment += 0.005f * (cappedRealDistance - renderDistance)
		}
		val z = -renderDistance.toFloat().coerceAtMost(MAX_RENDER_DISTANCE)
		val rotationRadian = Math.toRadians(rotation.toDouble())
		val zRotated = (z * cos(rotationRadian)).toFloat()
		val xRotated = (-(z * sin(rotationRadian))).toFloat()
		val y = frame.camera.displayOrientedPose.ty() + heightAdjustment.toFloat()

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

		marker.anchorNode = LocationArNode(newAnchor, marker).apply {
			setParent(arSceneView.scene)
			addChild(marker.node)
		}
		marker.node.localPosition = Vector3.zero()
	}
}