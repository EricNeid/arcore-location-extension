package org.neidhardt.arlocation

import com.google.ar.sceneform.Node


class LocationMarker(
	var latitude: Double,
	var longitude: Double,
	var node: Node
) {

	var anchorNode: LocationNode? = null

	/**
	 * [renderEventListener] is called on each frame an can be used to update marker
	 * properties before it is being rendered.
	 */
	var renderEventListener: RenderEventListener? = null

	/**
	 * [scaleModifier] is applied to the scale of the rendered object.
	 */
	var scaleModifier = 1f

	/**
	 * height is the altitude of the object, based on camera height.
	 */
	var height = 0f

	/**
	 * [onlyRenderWhenWithin] determines the maximum range in which markers are still rendered.
	 */
	var onlyRenderWhenWithin = Int.MAX_VALUE

	/**
	 * [scalingMode] determines whether the marker should scale, regardless of distance.
	 */
	var scalingMode = ScalingMode.FIXED_SIZE_ON_SCREEN

	var gradualScalingMinScale = 0.8f
	var gradualScalingMaxScale = 1.4f

	enum class ScalingMode {
		FIXED_SIZE_ON_SCREEN, NO_SCALING, GRADUAL_TO_MAX_RENDER_DISTANCE, GRADUAL_FIXED_SIZE
	}

}
