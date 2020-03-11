package org.neidhardt.arlocation

import android.location.Location

class CustomDeviceLocation(private val locationScene: CustomLocationScene) {

	var currentBestLocation: Location? = null

	fun onLocationUpdateAvailable(location: Location) {
		currentBestLocation = location

		locationScene.locationChangedEvent?.onChange(currentBestLocation)

		if (locationScene.refreshAnchorsAsLocationChanges()) {
			locationScene.refreshAnchors()
		}
	}
}