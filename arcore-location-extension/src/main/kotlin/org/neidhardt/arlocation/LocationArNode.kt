package org.neidhardt.arlocation

import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import org.neidhardt.arlocation.misc.geodeticCurve
import org.neidhardt.arlocation.misc.scaleFactorForDistance
import kotlin.math.sqrt

class LocationArNode(
		anchor: Anchor,
		private val locationMarker: LocationArMarker,
		private val locationScene: LocationArScene
) : AnchorNode(anchor) {

	override fun onUpdate(frameTime: FrameTime?) {
		super.onUpdate(frameTime)

		if (locationMarker.scalingMode != LocationArMarker.ScalingMode.DEFAULT) {
			scale()
		}

		if (locationMarker.rotationMode != LocationArMarker.RotationMode.DEFAULT) {
			rotate()
		}

		locationMarker.onRender?.onRender(this)
	}

	private fun scale() {
		val node = locationMarker.node
		val cameraPosition = scene?.camera?.worldPosition ?: return
		val direction = Vector3.subtract(cameraPosition, node.worldPosition)

		val scale = when(locationMarker.scalingMode){
			LocationArMarker.ScalingMode.FIXED_SIZE -> {
				getScaleFixedSize(direction).toFloat()
			}
			LocationArMarker.ScalingMode.GRADUAL -> {
				getScaleGradual(direction).toFloat()
			}

			else -> 1f // should not occur
		}

		node.worldPosition = Vector3(node.worldPosition.x, locationMarker.height, node.worldPosition.z)
		node.worldScale = Vector3(scale, scale, scale)
	}

	private fun rotate() {
		val node = locationMarker.node
		val cameraPosition = scene?.camera?.worldPosition ?: return
		val direction = Vector3.subtract(cameraPosition, node.worldPosition)

		val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
		node.worldRotation = lookRotation
	}

	private fun getScaleFixedSize(direction: Vector3): Double {
		return sqrt(direction.x * direction.x + direction.y * direction.y + (direction.z * direction.z)
				.toDouble())
	}

	private fun getScaleGradual(direction: Vector3): Double {
		val scaleFixedSize = getScaleFixedSize(direction)
		val userLocation = locationScene.currentLocation ?: return scaleFixedSize

		val distance = geodeticCurve(
				userLocation.latitude, userLocation.longitude,
				locationMarker.latitude, locationMarker.longitude
		).ellipsoidalDistance

		val distanceScaleFactor = scaleFactorForDistance(distance)
		return scaleFixedSize * distanceScaleFactor
	}

}