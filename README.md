# About

This library provides utilities for ARCore to place ar markers on geo locations.
It is based on the great work of <https://github.com/appoly/ARCore-Location> so be sure to check
their project out as well.

## Gradle

```gradle
implementation 'org.neidhardt:arcore-location-extension:0.2.0'
```

## Usage

```kotlin
val locationMarker = LocationMarker(lng, lat, createNode())

arsceneview.scene.addOnUpdateListener {
  // init location scene if required
  if (locationScene == null) {
    locationScene = LocationScene(arsceneview_arnavigationactivity)
  }
  
  locationScene?.addMarker(locationMarker)

  val nextFrame = arsceneview_arnavigationactivity.arFrame

  // process frame
  if (nextFrame?.camera?.trackingState == TrackingState.TRACKING) {
    locationScene?.processFrame(nextFrame)
  }
}
```

## Question or comments

Please feel free to open a new issue:
<https://github.com/EricNeid/arcore-location-extension/issues>
