package org.neidhardt.arlocation

import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime

class LocationArNode(
		anchor: Anchor,
		val locationMarker: LocationArMarker
) : AnchorNode(anchor) {

	override fun onUpdate(frameTime: FrameTime?) {
		super.onUpdate(frameTime)

		locationMarker.onRender?.let {
			it.onRender(this)
		}
	}
}