package org.neidhardt.arlocation

import android.location.Location
import android.os.Handler
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Vector3
import uk.co.appoly.arcorelocation.utils.LocationUtils
import java.util.*
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

private const val RENDER_DISTANCE = 25f

@Suppress("MemberVisibilityCanBePrivate")
class LocationScene(val arSceneView: ArSceneView) {

	private  val tag = LocationScene::class.java.simpleName

	/**
	 * [location] represents the current position of the user, interacting with the ar scene.
	 */
	var location: Location? = null
		private set

	/**
	 * [bearing] represents the current position of the user, interacting with the ar scene.
	 * It is represented as azimuth in degree: [0,360].
	 */
	var bearing: Float? = null
		private set

	/**
	 * [bearingAdjustment] can be used to calibrate the bearing sensor.
	 */
	var bearingAdjustment = 0
		set(value) {
			field = value
			anchorsNeedRefresh = true
		}

	/**
	 * [locationMarkers] contains all markers currently added to the scene.
	 */
	var locationMarkers = ArrayList<LocationMarker>()

	var distanceLimit = 30

	var minimalRefreshing = false

	var refreshAnchorsAsLocationChanges = false
		set(value) {
			if (value) {
				stopCalculationTask()
			} else {
				startCalculationTask()
			}
			refreshAnchors()
			field = value
		}

	var anchorRefreshInterval = 1000 * 5 // 5 seconds
		set(value) {
			field = value
			stopCalculationTask()
			startCalculationTask()
		}

	var offsetOverlapping = false
	var removeOverlapping = false

	private var anchorsNeedRefresh = true

	private val handler = Handler()
	private val anchorRefreshTask: Runnable = object : Runnable {
		override fun run() {
			anchorsNeedRefresh = true
			handler.postDelayed(this, anchorRefreshInterval.toLong())
		}
	}

	fun startCalculationTask() {
		anchorRefreshTask.run()
	}

	fun stopCalculationTask() {
		handler.removeCallbacks(anchorRefreshTask)
	}

	init {
		startCalculationTask()
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
	}

	fun processFrame(frame: Frame) {
		refreshAnchorsIfRequired(frame)
	}

	private fun refreshAnchorsIfRequired(frame: Frame) {
		if (!anchorsNeedRefresh) {
			return
		}
		anchorsNeedRefresh = false

		val location = location ?: return
		val currentBearing = bearing ?: return
		val session = arSceneView.session ?: return

		for (marker in locationMarkers) {

			val markerDistance = LocationUtils.distance(
					marker.latitude,
					location.latitude,
					marker.longitude,
					location.longitude, 0.0, 0.0
			).roundToInt()

			if (markerDistance > marker.onlyRenderWhenWithin) {
				Log.i(tag, "Not rendering. Marker distance: $markerDistance Max render distance: ${marker.onlyRenderWhenWithin}")
				continue
			}

			val bearing = LocationUtils.bearing(
					location.latitude,
					location.longitude,
					marker.latitude,
					marker.longitude
			).toFloat()

			var markerBearing = bearing - currentBearing
			markerBearing += bearingAdjustment + 360
			markerBearing %= 360

			val rotation = floor(markerBearing)
			Log.d(
					tag, "currentDegree " + currentBearing
					+ " bearing " + bearing + " markerBearing " + markerBearing
					+ " rotation " + rotation + " distance " + markerDistance
			)

			var renderDistance = markerDistance
			// limit the distance of the Anchor within the scene
			// prevents rendering issues
			if (renderDistance > distanceLimit) {
				renderDistance = distanceLimit
			}
			// adjustment to add markers on horizon, instead of just directly in front of camera
			var heightAdjustment = 0.0
			// raise distant markers for better illusion of distance
			val cappedRealDistance = if (markerDistance > 500) {
				500
			} else {
				markerDistance
			}
			if (renderDistance != markerDistance) {
				heightAdjustment += 0.005f * (cappedRealDistance - renderDistance).toDouble()
			}
			val z = -renderDistance.toFloat().coerceAtMost(RENDER_DISTANCE)
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

			marker.anchorNode = LocationNode(newAnchor, marker, this).apply {
				setParent(arSceneView.scene)
				addChild(marker.node)

				scalingMode = LocationMarker.ScalingMode.NO_SCALING
				scaleModifier = marker.scaleModifier
				scalingMode = marker.scalingMode
				gradualScalingMaxScale = marker.gradualScalingMaxScale
				gradualScalingMinScale = marker.gradualScalingMinScale
				height = marker.height
			}
			marker.node.localPosition = Vector3.zero()
			marker.renderEventListener?.let {
				marker.anchorNode?.renderEventListener = it
			}

			if (minimalRefreshing) {
				marker.anchorNode?.scaleAndRotate()
			}
		}

		// is this necessary?
		System.gc()
	}

	fun refreshAnchors() {
		anchorsNeedRefresh = true
	}

	fun onLocationChanged(location: Location) {
		this.location = location
		if (refreshAnchorsAsLocationChanges) {
			refreshAnchors()
		}
	}

	fun onBearingChanged(bearing: Float) {
		this.bearing = bearing
		// TODO investigate
	}

}