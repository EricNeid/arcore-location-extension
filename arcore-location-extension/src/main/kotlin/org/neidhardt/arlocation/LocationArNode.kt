package org.neidhardt.arlocation

import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode

class LocationArNode(
		anchor: Anchor,
		val locationMarker: LocationArMarker,
		val locationScene: LocationArScene
) : AnchorNode(anchor) {

}