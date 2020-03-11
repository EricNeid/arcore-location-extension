package org.neidhardt.arlocation

import android.app.Activity
import android.os.Handler
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Vector3
import org.neidhardt.arlocation.CustomDeviceLocation
import org.neidhardt.arlocation.CustomLocationMarker
import org.neidhardt.arlocation.CustomLocationNode
import uk.co.appoly.arcorelocation.sensor.DeviceLocationChanged
import uk.co.appoly.arcorelocation.sensor.DeviceOrientation
import uk.co.appoly.arcorelocation.utils.LocationUtils
import java.util.*

/**
 * Created by John on 02/03/2018.
 */
open class CustomLocationScene(var context: Activity, mArSceneView: ArSceneView) {

	private val RENDER_DISTANCE = 25f

	var mArSceneView: ArSceneView
	var deviceLocation: CustomDeviceLocation?
	var deviceOrientation: DeviceOrientation
	var mLocationMarkers = ArrayList<CustomLocationMarker>()
	// Anchors are currently re-drawn on an interval. There are likely better
// ways of doing this, however it's sufficient for now.
	private var anchorRefreshInterval = 1000 * 5 // 5 seconds
	/**
	 * The distance cap for distant markers.
	 * ARCore doesn't like markers that are 2000km away :/
	 *
	 * @return
	 */
	/**
	 * The distance cap for distant markers.
	 * Render distance limit is 30 meters, impossible to change that for now
	 * https://github.com/google-ar/sceneform-android-sdk/issues/498
	 */
	// Limit of where to draw markers within AR scene.
// They will auto scale, but this helps prevents rendering issues
	var distanceLimit = 30
	private var offsetOverlapping = false
	private var removeOverlapping = false
	// Bearing adjustment. Can be set to calibrate with true north
	private var bearingAdjustment = 0
	private val TAG = "LocationScene"
	private var anchorsNeedRefresh = true
	private var minimalRefreshing = false
	private var refreshAnchorsAsLocationChanges = false
	private val mHandler = Handler()
	var anchorRefreshTask: Runnable = object : Runnable {
		override fun run() {
			anchorsNeedRefresh = true
			mHandler.postDelayed(this, anchorRefreshInterval.toLong())
		}
	}
	var isDebugEnabled = false
	private val mSession: Session?
	/**
	 * Get additional event to run as device location changes.
	 * Save creating extra sensor classes
	 *
	 * @return
	 */
	/**
	 * Set additional event to run as device location changes.
	 * Save creating extra sensor classes
	 */
	var locationChangedEvent: DeviceLocationChanged? = null

	private fun test() {
		val bearing = LocationUtils.bearing(
			48.31244200607186,
			2.1290194140624408,
			48.33577350525661,
			2.073057805175722
		).toFloat()
		Log.d("brako", "OKKKKKK $bearing")
	}

	fun minimalRefreshing(): Boolean {
		return minimalRefreshing
	}

	fun setMinimalRefreshing(minimalRefreshing: Boolean) {
		this.minimalRefreshing = minimalRefreshing
	}

	fun refreshAnchorsAsLocationChanges(): Boolean {
		return refreshAnchorsAsLocationChanges
	}

	fun setRefreshAnchorsAsLocationChanges(refreshAnchorsAsLocationChanges: Boolean) {
		if (refreshAnchorsAsLocationChanges) {
			stopCalculationTask()
		} else {
			startCalculationTask()
		}
		refreshAnchors()
		this.refreshAnchorsAsLocationChanges = refreshAnchorsAsLocationChanges
	}

	fun getAnchorRefreshInterval(): Int {
		return anchorRefreshInterval
	}

	/**
	 * Set the interval at which anchors should be automatically re-calculated.
	 *
	 * @param anchorRefreshInterval
	 */
	fun setAnchorRefreshInterval(anchorRefreshInterval: Int) {
		this.anchorRefreshInterval = anchorRefreshInterval
		stopCalculationTask()
		startCalculationTask()
	}

	fun clearMarkers() {
		for (lm in mLocationMarkers) {
			if (lm.anchorNode != null) {
				lm.anchorNode!!.anchor!!.detach()
				lm.anchorNode!!.isEnabled = false
				lm.anchorNode = null
			}
		}
		mLocationMarkers = ArrayList()
	}

	fun shouldOffsetOverlapping(): Boolean {
		return offsetOverlapping
	}

	fun shouldRemoveOverlapping(): Boolean {
		return removeOverlapping
	}

	/**
	 * Attempts to raise markers vertically when they overlap.
	 * Needs work!
	 *
	 * @param offsetOverlapping
	 */
	fun setOffsetOverlapping(offsetOverlapping: Boolean) {
		this.offsetOverlapping = offsetOverlapping
	}

	/**
	 * Remove farthest markers when they overlap
	 *
	 * @param removeOverlapping
	 */
	fun setRemoveOverlapping(removeOverlapping: Boolean) {
		this.removeOverlapping = removeOverlapping
		//        for (LocationMarker mLocationMarker : mLocationMarkers) {
//            LocationNode anchorNode = mLocationMarker.anchorNode;
//            if (anchorNode != null) {
//                anchorNode.setEnabled(true);
//            }
//        }
	}

	fun processFrame(frame: Frame) {
		refreshAnchorsIfRequired(frame)
	}

	/**
	 * Force anchors to be re-calculated
	 */
	fun refreshAnchors() {
		anchorsNeedRefresh = true
	}

	private fun refreshAnchorsIfRequired(frame: Frame) {
		if (!anchorsNeedRefresh) {
			return
		}
		anchorsNeedRefresh = false
		Log.i(TAG, "Refreshing anchors...")
		if (deviceLocation == null || deviceLocation!!.currentBestLocation == null) {
			Log.i(TAG, "Location not yet established.")
			return
		}
		for (i in mLocationMarkers.indices) {
			try {
				val marker = mLocationMarkers[i]
				val markerDistance = Math.round(
					LocationUtils.distance(
						marker.latitude,
						deviceLocation!!.currentBestLocation!!.latitude,
						marker.longitude,
						deviceLocation!!.currentBestLocation!!.longitude, 0.0, 0.0
					)
				).toInt()
				if (markerDistance > marker.onlyRenderWhenWithin) { // Don't render if this has been set and we are too far away.
					Log.i(
						TAG, "Not rendering. Marker distance: " + markerDistance
								+ " Max render distance: " + marker.onlyRenderWhenWithin
					)
					continue
				}
				val bearing = LocationUtils.bearing(
					deviceLocation!!.currentBestLocation!!.latitude,
					deviceLocation!!.currentBestLocation!!.longitude,
					marker.latitude,
					marker.longitude
				).toFloat()
				var markerBearing = bearing - deviceOrientation.orientation
				// Bearing adjustment can be set if you are trying to
// correct the heading of north - setBearingAdjustment(10)
				markerBearing = markerBearing + bearingAdjustment + 360
				markerBearing = markerBearing % 360
				val rotation = Math.floor(markerBearing.toDouble())
				Log.d(
					TAG, "currentDegree " + deviceOrientation.orientation
							+ " bearing " + bearing + " markerBearing " + markerBearing
							+ " rotation " + rotation + " distance " + markerDistance
				)
				// When pointing device upwards (camera towards sky)
// the compass bearing can flip.
// In experiments this seems to happen at pitch~=-25
//if (deviceOrientation.pitch > -25)
//rotation = rotation * Math.PI / 180;
				var renderDistance = markerDistance
				// Limit the distance of the Anchor within the scene.
// Prevents rendering issues.
				if (renderDistance > distanceLimit) renderDistance = distanceLimit
				// Adjustment to add markers on horizon, instead of just directly in front of camera
				var heightAdjustment = 0.0
				// Math.round(renderDistance * (Math.tan(Math.toRadians(deviceOrientation.pitch)))) - 1.5F;
// Raise distant markers for better illusion of distance
// Hacky - but it works as a temporary measure
				val cappedRealDistance = if (markerDistance > 500) 500 else markerDistance
				if (renderDistance != markerDistance) heightAdjustment += 0.005f * (cappedRealDistance - renderDistance).toDouble()
				val z = -Math.min(renderDistance.toFloat(), RENDER_DISTANCE)
				val rotationRadian = Math.toRadians(rotation)
				val zRotated =
					(z * Math.cos(rotationRadian)).toFloat()
				val xRotated =
					(-(z * Math.sin(rotationRadian))).toFloat()
				val y =
					frame.camera.displayOrientedPose.ty() + heightAdjustment.toFloat()

				if (marker.anchorNode != null && marker.anchorNode!!.anchor != null) {
					marker.anchorNode!!.anchor!!.detach()
					marker.anchorNode!!.anchor = null
					marker.anchorNode!!.isEnabled = false
					marker.anchorNode = null
				}
				// Don't immediately assign newly created anchor in-case of exceptions
				val translation = Pose.makeTranslation(xRotated, y, zRotated)
				val newAnchor = mSession!!.createAnchor(
					frame.camera
						.displayOrientedPose
						.compose(translation)
						.extractTranslation()
				)
				marker.anchorNode = CustomLocationNode(newAnchor, marker, this)
				marker.anchorNode!!.scalingMode = CustomLocationMarker.ScalingMode.NO_SCALING
				marker.anchorNode!!.setParent(mArSceneView.scene)
				marker.anchorNode!!.addChild(mLocationMarkers[i].node)
				marker.node.localPosition = Vector3.zero()
				if (marker.renderEvent != null) {
					marker.anchorNode!!.renderEvent = marker.renderEvent
				}
				marker.anchorNode!!.scaleModifier = marker.scaleModifier
				marker.anchorNode!!.scalingMode = marker.scalingMode
				marker.anchorNode!!.gradualScalingMaxScale = marker.gradualScalingMaxScale
				marker.anchorNode!!.gradualScalingMinScale = marker.gradualScalingMinScale
				marker.anchorNode!!.height = marker.height
				if (minimalRefreshing) marker.anchorNode!!.scaleAndRotate()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
		//this is bad, you should feel bad
		System.gc()
	}

	/**
	 * Adjustment for compass bearing.
	 *
	 * @return
	 */
	fun getBearingAdjustment(): Int {
		return bearingAdjustment
	}

	/**
	 * Adjustment for compass bearing.
	 * You may use this for a custom method of improving precision.
	 *
	 * @param i
	 */
	fun setBearingAdjustment(i: Int) {
		bearingAdjustment = i
		anchorsNeedRefresh = true
	}

	/**
	 * Resume sensor services. Important!
	 */
	fun resume() {
		deviceOrientation.resume()
	}

	/**
	 * Pause sensor services. Important!
	 */
	fun pause() {
		deviceOrientation.pause()
	}

	fun startCalculationTask() {
		anchorRefreshTask.run()
	}

	fun stopCalculationTask() {
		mHandler.removeCallbacks(anchorRefreshTask)
	}

	init {
		mSession = mArSceneView.session
		this.mArSceneView = mArSceneView
		startCalculationTask()
		deviceLocation = CustomDeviceLocation(this)
		deviceOrientation = DeviceOrientation(context)
		deviceOrientation.resume()
		//test();
	}
}