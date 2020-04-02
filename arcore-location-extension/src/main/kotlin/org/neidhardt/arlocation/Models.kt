package org.neidhardt.arlocation

import com.google.ar.sceneform.math.Vector3

/**
 * [ArPosition] represents the 2d position (no height) of an ar object.
 * Reference is the current camera position (0,0,0)
 * x > 0 move to right, x < 0 move to left
 * z > 0 move behind camera, z < 0 move away from camera
 *
 * @property x the position on the x-axis (left/right)
 * @property z the position on the z-axis (near/afar)
 * @constructor creates new position
 */
data class ArPosition(val x: Float, val z: Float) {

	fun toVector3(y: Float): Vector3 = Vector3(x, y, -1f * z)
}

/**
 * [GlobalPosition] represents the 2d position (no altitude) of an real world object.
 * @property latitude
 * @property longitude
 * @constructor creates new position
 */
data class GlobalPosition(val latitude: Double, val longitude: Double)