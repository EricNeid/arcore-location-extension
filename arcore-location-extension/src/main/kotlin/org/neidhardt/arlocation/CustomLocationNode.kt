package org.neidhardt.arlocation

import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Ray
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import uk.co.appoly.arcorelocation.utils.LocationUtils

class CustomLocationNode(
		anchor: Anchor?,
		val locationMarker: CustomLocationMarker,
		private val locationScene: CustomLocationScene
) :
	AnchorNode(anchor) {
	private val TAG = "LocationNode"
	var renderEvent: CustomLocationNodeRender? = null
	var distance = 0
	var distanceInAR = 0.0
	var scaleModifier = 1f
	var height = 0f
	var gradualScalingMinScale = 0.8f
	var gradualScalingMaxScale = 1.4f
	var scalingMode = CustomLocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN

	override fun onUpdate(frameTime: FrameTime) { // Typically, getScene() will never return null because onUpdate() is only called when the node
// is in the scene.
// However, if onUpdate is called explicitly or if the node is removed from the scene on a
// different thread during onUpdate, then getScene may be null.
		for (n in children) {
			if (scene == null) {
				return
			}
			val cameraPosition = scene!!.camera.worldPosition
			val nodePosition = n.worldPosition
			// Compute the difference vector between the camera and anchor
			val dx = cameraPosition.x - nodePosition.x
			val dy = cameraPosition.y - nodePosition.y
			val dz = cameraPosition.z - nodePosition.z
			// Compute the straight-line distance.
			var distanceInAR =
				Math.sqrt(dx * dx + dy * dy + (dz * dz).toDouble())
			distanceInAR = distanceInAR
			if (locationScene.shouldOffsetOverlapping()) {
				if (locationScene.mArSceneView.scene.overlapTestAll(n).size > 0) {
					height = height + 1.2f
				}
			}
			if (locationScene.shouldRemoveOverlapping()) {
				val ray = Ray()
				ray.origin = cameraPosition
				val xDelta =
					(distanceInAR * Math.sin(Math.PI / 15)).toFloat() //12 degrees
				val cameraLeft = scene!!.camera.left.normalized()
				val left = Vector3.add(nodePosition, cameraLeft.scaled(xDelta))
				val right = Vector3.add(nodePosition, cameraLeft.scaled(-xDelta))
				val isOverlapping = (isOverlapping(n, ray, left, cameraPosition)
						|| isOverlapping(n, ray, nodePosition, cameraPosition)
						|| isOverlapping(n, ray, right, cameraPosition))
				isEnabled = if (isOverlapping) {
					false
				} else {
					true
				}
			}
		}
		if (!locationScene.minimalRefreshing()) scaleAndRotate()
		if (renderEvent != null) {
			if (this.isTracking && this.isActive && this.isEnabled) renderEvent!!.render(this)
		}
	}

	private fun isOverlapping(
		n: Node,
		ray: Ray,
		target: Vector3,
		cameraPosition: Vector3
	): Boolean {
		val nodeDirection = Vector3.subtract(target, cameraPosition)
		ray.direction = nodeDirection
		val hitTestResults =
			locationScene.mArSceneView.scene.hitTestAll(ray)
		if (hitTestResults.size > 0) {
			var closestHit: HitTestResult? = null
			for (hit in hitTestResults) { //Get the closest hit on enabled Node
				if (hit.node != null && hit.node!!.isEnabled) {
					closestHit = hit
					break
				}
			}
			// if closest hit is not the current node, it is hidden behind another node that is closer
			return closestHit != null && closestHit.node !== n
		}
		return false
	}

	fun scaleAndRotate() {
		for (n in children) {
			val markerDistance = Math.ceil(
				LocationUtils.distance(
					locationMarker.latitude,
					locationScene.deviceLocation!!.currentBestLocation!!.latitude,
					locationMarker.longitude,
					locationScene.deviceLocation!!.currentBestLocation!!.longitude, 0.0, 0.0
				)
			).toInt()
			distance = markerDistance
			// Limit the distance of the Anchor within the scene.
// Prevents uk.co.appoly.arcorelocation.rendering issues.
			var renderDistance = markerDistance
			if (renderDistance > locationScene.distanceLimit) renderDistance =
				locationScene.distanceLimit
			var scale = 1f
			val cameraPosition = scene!!.camera.worldPosition
			val direction = Vector3.subtract(cameraPosition, n.worldPosition)
			when (scalingMode) {
				CustomLocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN -> scale = Math.sqrt(
					direction.x * direction.x + direction.y * direction.y + (direction.z * direction.z).toDouble()
				).toFloat()
				CustomLocationMarker.ScalingMode.GRADUAL_TO_MAX_RENDER_DISTANCE -> {
					val scaleDifference =
						gradualScalingMaxScale - gradualScalingMinScale
					scale =
						(gradualScalingMinScale + (locationScene.distanceLimit - markerDistance) * (scaleDifference / locationScene.distanceLimit)) * renderDistance
				}
				CustomLocationMarker.ScalingMode.GRADUAL_FIXED_SIZE -> {
					scale = Math.sqrt(
						direction.x * direction.x + direction.y * direction.y + (direction.z * direction.z).toDouble()
					).toFloat()
					var gradualScale = gradualScalingMaxScale - gradualScalingMinScale
					gradualScale =
						gradualScalingMaxScale - gradualScale / renderDistance * markerDistance
					scale *= Math.max(gradualScale, gradualScalingMinScale)
				}
			}
			scale *= scaleModifier
			//Log.d("LocationScene", "scale " + scale);
			n.worldPosition = Vector3(n.worldPosition.x, height, n.worldPosition.z)
			val lookRotation =
				Quaternion.lookRotation(direction, Vector3.up())
			n.worldRotation = lookRotation
			n.worldScale = Vector3(scale, scale, scale)
		}
	}

}
