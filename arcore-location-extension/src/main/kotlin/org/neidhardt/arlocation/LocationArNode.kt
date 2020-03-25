package org.neidhardt.arlocation

import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import kotlin.math.sqrt

class LocationArNode(
		anchor: Anchor,
		private val locationMarker: LocationArMarker
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

	fun scale() {
		val node = locationMarker.node
		val cameraPosition = scene?.camera?.worldPosition ?: return
		val direction = Vector3.subtract(cameraPosition, node.worldPosition)

		val scale =
				sqrt(direction.x * direction.x + direction.y * direction.y + (direction.z * direction.z).toDouble()).toFloat()

		node.worldPosition = Vector3(node.worldPosition.x, locationMarker.height, node.worldPosition.z)
		node.worldScale = Vector3(scale, scale, scale)
	}

	fun rotate() {
		val node = locationMarker.node
		val cameraPosition = scene?.camera?.worldPosition ?: return
		val direction = Vector3.subtract(cameraPosition, node.worldPosition)

		val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
		node.worldRotation = lookRotation
	}
}