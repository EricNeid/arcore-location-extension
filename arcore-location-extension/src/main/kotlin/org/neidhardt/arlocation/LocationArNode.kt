package org.neidhardt.arlocation

import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import org.neidhardt.arlocation.misc.geodeticCurve
import kotlin.math.sqrt

@Suppress("MemberVisibilityCanBePrivate")
class LocationArNode(
		anchor: Anchor,
		val locationMarker: LocationArMarker,
		val locationScene: LocationArScene
) : AnchorNode(anchor) {

	override fun onUpdate(frameTime: FrameTime) {
		super.onUpdate(frameTime)

		if (locationMarker.scalingMode != LocationArMarker.ScalingMode.DEFAULT) {
			scale()
		}

		if (locationMarker.rotationMode != LocationArMarker.RotationMode.DEFAULT) {
			rotate()
		}

		locationMarker.onUpdate?.onUpdate(frameTime, this)
	}

	private fun scale() {
		val node = locationMarker.node
		val cameraPosition = scene?.camera?.worldPosition ?: return
		val direction = Vector3.subtract(cameraPosition, node.worldPosition)

		val scale = when(locationMarker.scalingMode){
			LocationArMarker.ScalingMode.FIXED_SIZE -> {
				getScaleFixedSize(
						direction
				).toFloat()
			}
			LocationArMarker.ScalingMode.GRADUAL -> {
				getScaleGradual(
						direction,
						locationScene.currentLocation,
						GlobalPosition(locationMarker.latitude, locationMarker.longitude)
				).toFloat()
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

	companion object {

		fun getScaleFixedSize(direction: Vector3): Double {
			return sqrt(
					direction.x.square() +
					direction.y.square() +
					direction.z.square()
			)
		}

		fun getScaleGradual(direction: Vector3, src: GlobalPosition?, dst: GlobalPosition?): Double {
			val scaleFixedSize = getScaleFixedSize(direction)

			val userLocation = src ?: return scaleFixedSize
			val markerLocation = dst ?: return scaleFixedSize

			val distance = geodeticCurve(
					userLocation.latitude, userLocation.longitude,
					markerLocation.latitude, markerLocation.longitude
			).ellipsoidalDistance

			val distanceScaleFactor = ArLocationUtils.scaleFactorForDistance(distance)
			return scaleFixedSize * distanceScaleFactor
		}
	}
}

private fun Float.square(): Double {
	return this * this.toDouble()
}
