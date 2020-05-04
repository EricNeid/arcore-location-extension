# About

This library provides utilities for ARCore to place ar markers on geo locations.
It is based on the great work of <https://github.com/appoly/ARCore-Location> so be sure to check
their project out as well.

## Gradle

Check for the release pages for the latest version. If using gradle you can reference the 
library like this (it's on jcenter): 

```gradle
implementation 'org.neidhardt:arcore-location-extension:x.x.x'
```

## Usage

```kotlin
// create location scene
val locationScene = LocationArScene(arsceneview)
locationScene.startLocationScene()

// create location marker
val locationMarker = LocationMarker(lng, lat, createNode())

// getting location and rotation is up to you
locationScene.onLocationChanged(newLocation)
locationScene.onBearingChanged(newBearing)
```

There are several options available for your location markers to explore.

For far off markers:

```kotlin
val marker = LocationArMarker(
  latitude = 42.0,
  longitude = 42.0,
  node = createNode()
).apply {
  placementType = LocationArMarker.PlacementType.DYNAMIC
  rotationMode = LocationArMarker.RotationMode.FACE_USER
  scalingMode = LocationArMarker.ScalingMode.GRADUAL
}
```

For simple markers in close proximity (<30m):

```kotlin
val marker = LocationArMarker(
  latitude = 42.0,
  longitude = 42.0,
  node = createNode()
).apply {
  placementType = LocationArMarker.PlacementType.STATIC
}
```

## Question or comments

Please feel free to open a new issue:
<https://github.com/EricNeid/arcore-location-extension/issues>
