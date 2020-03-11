package org.neidhardt.arlocation

import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Ray
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.math.sqrt


@Suppress("MemberVisibilityCanBePrivate")
class LocationNode(
		anchor: Anchor,
		val locationMarker: LocationMarker,
		val locationScene: LocationScene
) : AnchorNode(anchor) {

	var renderEventListener: RenderEventListener? = null

	var distance = 0
	var distanceInAR = 0.0

	var height = 0f

	var scaleModifier = 1f
	var gradualScalingMinScale = 0.8f
	var gradualScalingMaxScale = 1.4f
	var scalingMode = LocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN

	override fun onUpdate(frameTime: FrameTime) {

		scene?.let { scene ->
			children.forEach { n ->

				val cameraPosition = scene.camera.worldPosition
				val nodePosition = n.worldPosition

				// Compute the difference vector between the camera and anchor
				val dx = cameraPosition.x - nodePosition.x
				val dy = cameraPosition.y - nodePosition.y
				val dz = cameraPosition.z - nodePosition.z

				// Compute the straight-line distance.
				distanceInAR = sqrt(dx * dx + dy * dy + (dz * dz).toDouble())
				if (locationScene.offsetOverlapping) {
					if (locationScene.arSceneView.scene.overlapTestAll(n).size > 0) {
						height += 1.2f
					}
				}

				if (locationScene.removeOverlapping) {
					val ray = Ray()
					ray.origin = cameraPosition
					val xDelta = (distanceInAR * sin(Math.PI / 15)).toFloat() //12 degrees
					val cameraLeft = scene.camera.left.normalized()
					val left = Vector3.add(nodePosition, cameraLeft.scaled(xDelta))
					val right = Vector3.add(nodePosition, cameraLeft.scaled(-xDelta))
					val isOverlapping = (isOverlapping(n, ray, left, cameraPosition)
							|| isOverlapping(n, ray, nodePosition, cameraPosition)
							|| isOverlapping(n, ray, right, cameraPosition))
					isEnabled = !isOverlapping
				}
			}
		}

		if (!locationScene.minimalRefreshing) {
			scaleAndRotate()
		}

		renderEventListener?.let {
			if (isTracking && isActive && isEnabled) {
				it.onRender(this)
			}
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
		val hitTestResults = locationScene.arSceneView.scene.hitTestAll(ray)
		if (hitTestResults.size > 0) {

			var closestHit: HitTestResult? = null
			for (hit in hitTestResults) {
				//Get the closest hit on enabled Node
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

		val location = locationScene.location ?: return

		scene?.let { scene ->
			children.forEach { n ->
				val markerDistance = ceil(
						distance(
								locationMarker.latitude,
								locationMarker.longitude,
								location.latitude,
								location.longitude,
								0.0, 0.0
						)
				).toInt()
				distance = markerDistance

				// limit the distance of the Anchor within the scene.
				var renderDistance = markerDistance
				if (renderDistance > locationScene.distanceLimit) renderDistance = locationScene.distanceLimit
				val cameraPosition = scene.camera.worldPosition
				val direction = Vector3.subtract(cameraPosition, n.worldPosition)

				var scale = 1f
				when (scalingMode) {
					LocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN -> {
						scale =
								sqrt(direction.x * direction.x + direction.y * direction.y + (direction.z * direction.z).toDouble()).toFloat()
					}
					LocationMarker.ScalingMode.GRADUAL_TO_MAX_RENDER_DISTANCE -> {
						val scaleDifference =
								gradualScalingMaxScale - gradualScalingMinScale
						scale =
								(gradualScalingMinScale + (locationScene.distanceLimit - markerDistance) * (scaleDifference / locationScene.distanceLimit)) * renderDistance
					}
					LocationMarker.ScalingMode.GRADUAL_FIXED_SIZE -> {
						scale =
								sqrt(direction.x * direction.x + direction.y * direction.y + (direction.z * direction.z).toDouble()).toFloat()
						var gradualScale = gradualScalingMaxScale - gradualScalingMinScale
						gradualScale = gradualScalingMaxScale - gradualScale / renderDistance * markerDistance
						scale *= gradualScale.coerceAtLeast(gradualScalingMinScale)
					}
					LocationMarker.ScalingMode.NO_SCALING -> {
						scale = 1f
					}
				}

				scale *= scaleModifier
				n.worldPosition = Vector3(n.worldPosition.x, height, n.worldPosition.z)
				val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
				n.worldRotation = lookRotation
				n.worldScale = Vector3(scale, scale, scale)
			}
		}
	}
}