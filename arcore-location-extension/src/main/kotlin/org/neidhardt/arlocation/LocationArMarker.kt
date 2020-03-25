package org.neidhardt.arlocation

import com.google.ar.sceneform.Node

class LocationArMarker(
		var latitude: Double,
		var longitude: Double,
		var node: Node
) {
	var anchorNode: LocationArNode? = null

	/**
	 * [onRender] is called on each frame an can be used to update marker
	 * properties before it is being rendered.
	 */
	var onRender: NodeRenderEventListener? = null

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
	 * [PlacementType] indicates how this maker should be placed.
	 * Use [PlacementType.STATIC] for maker in close proximity (< 20m).
	 * User [PlacementType.DYNAMIC] for makers far away. The systems tries to emulate the actual distance.
	 */
	enum class PlacementType {
		STATIC,
		DYNAMIC
	}

	/**
	 * [ScalingMode] defines how the node should scale.
	 * [DEFAULT] means no special handling. Let ar scene handle everything.
	 * [FIXED_SIZE] means the node does not scale.
	 */
	enum class ScalingMode {
		DEFAULT,
		FIXED_SIZE
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
