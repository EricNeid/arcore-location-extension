package org.neidhardt.arlocation

import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node

class LocationArMarker(
		var globalPosition: GlobalPosition,
		var node: Node
) {
	var anchorNode: LocationArNode? = null

	/**
	 * [onUpdate] is called on each frame an can be used to update marker
	 * properties before it is being rendered.
	 */
	var onUpdate: ((frameTime: FrameTime, locationNode: LocationArNode) -> Unit)? = null

	/**
	 * [height] is the altitude of the object, based on camera height.
	 */
	var height = 0f

	/**
	 * [onlyRenderWhenWithin] determines the maximum range in which markers are still rendered.
	 */
	var onlyRenderWhenWithin = Int.MAX_VALUE

	/**
	 * [placementType] indicates how this maker should be placed.
	 * Use [PlacementType.STATIC] for maker in close proximity (< 20m).
	 * User [PlacementType.DYNAMIC] for makers far away. The systems tries to emulate the actual distance.
	 */
	var placementType = PlacementType.STATIC

	var scalingMode = ScalingMode.DEFAULT

	var rotationMode = RotationMode.DEFAULT

	/**
	 * [customScale] should be set, when [ScalingMode.CUSTOM] is used to define the scaling behaviour.
	 */
	var customScale: ((distance: Double, locationNode: LocationArNode) -> Double)? = null

	/**
	 * [PlacementType] indicates how this maker should be placed.
	 * Marker cannot be render after 30 meters, so special handling is required
	 * Use [PlacementType.STATIC] for maker in close proximity (< 30m).
	 * User [PlacementType.DYNAMIC] for makers far away. The systems tries to emulate the actual distance.
	 */
	enum class PlacementType {
		STATIC,
		DYNAMIC
	}

	/**
	 * [ScalingMode] defines how the node should scale. If [PlacementType.STATIC] is used, the scaling
	 * mode should remain [DEFAULT], otherwise the behaviour is undefined.
	 *
	 * [DEFAULT] means no special handling. Let ar scene handle everything. Intended for [PlacementType.STATIC].
	 * [FIXED_SIZE] means the node does not scale.
	 * [GRADUAL] means scaling the node between 5m and 100m from 1.0 to 0.2.
	 * [CUSTOM] allows you to provide your own scaling. It requires setting scaling function [customScale].
	 */
	enum class ScalingMode {
		DEFAULT,
		FIXED_SIZE,
		GRADUAL,
		CUSTOM
	}

	/**
	 * [RotationMode] defines how the node should rotate.
	 * [DEFAULT] means no special handling. Let ar scene handle everything.
	 * [FACE_USER] means the node always rotates it's front to the user.
	 */
	enum class RotationMode {
		DEFAULT,
		FACE_USER
	}
}
