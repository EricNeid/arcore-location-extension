package org.neidhardt.arlocation

import com.google.ar.sceneform.Node

class CustomLocationMarker(
	// Location in real-world terms
	var longitude: Double,
	var latitude: Double,
	// Node to render
	var node: Node
) {
	// Location in AR terms
	var anchorNode: LocationNode? = null
	/**
	 * Called on each frame
	 *
	 * @return - LocationNodeRender (event)
	 */
	/**
	 * Called on each frame.
	 */
	// Called on each frame if not null
	var renderEventListenerEvent: RenderEventListener? = null
	/**
	 * Scale multiplier
	 *
	 * @return - multiplier
	 */
	/**
	 * Scale multiplier
	 *
	 * @param scaleModifier - multiplier
	 */
	var scaleModifier = 1f
	/**
	 * Height based on camera height
	 *
	 * @return - height in metres
	 */
	/**
	 * Height based on camera height
	 *
	 * @param height - height in metres
	 */
	var height = 0f
	/**
	 * Only render this marker when within [onlyRenderWhenWithin] metres
	 *
	 * @return - metres or -1
	 */
	/**
	 * Only render this marker when within [onlyRenderWhenWithin] metres
	 *
	 * @param onlyRenderWhenWithin - metres
	 */
	var onlyRenderWhenWithin = Int.MAX_VALUE
	/**
	 * How the markers should scale
	 *
	 * @return - ScalingMode
	 */
	/**
	 * Whether the marker should scale, regardless of distance.
	 *
	 * @param scalingMode - ScalingMode.X
	 */
	var scalingMode =
			ScalingMode.FIXED_SIZE_ON_SCREEN
	var gradualScalingMinScale = 0.8f
	var gradualScalingMaxScale = 1.4f

	enum class ScalingMode {
		FIXED_SIZE_ON_SCREEN, NO_SCALING, GRADUAL_TO_MAX_RENDER_DISTANCE, GRADUAL_FIXED_SIZE
	}

}
