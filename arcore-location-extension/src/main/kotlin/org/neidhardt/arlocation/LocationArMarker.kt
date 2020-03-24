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

}
