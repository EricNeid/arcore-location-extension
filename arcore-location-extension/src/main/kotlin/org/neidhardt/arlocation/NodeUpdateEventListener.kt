package org.neidhardt.arlocation

import com.google.ar.sceneform.FrameTime

/**
 * [NodeUpdateEventListener] is called each frame before the node is rendered (onUpdate callback).
 */
interface NodeUpdateEventListener {
	fun onUpdate(frameTime: FrameTime, locationNode: LocationArNode)
}
