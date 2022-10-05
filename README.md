# About

This library provides utilities for ARCore to place ar markers on geo locations.
It is based on the great work of <https://github.com/appoly/ARCore-Location> so be sure to check
their project out as well.

## Gradle

Check for the release pages for the latest version.

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.github.ericneid:arcore-location-extension:0.8.0'
}
```

## Quickstart

A simple example on the important functions. A more complete example is provided [below](#Example)

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

## Example

This sections provides a more complete example on how to use this library.
However, getting the current user location and bearing is omitted in this example.
I'm using on of my other android libs for that: <https://github.com/EricNeid/rx-location-services>

### Loading the renderable

```kotlin
fun loadViewRenderable(
    context: Context,
    viewId: Int,
    onSuccess: (ViewRenderable)->Unit,
    onFailure: (Throwable)->Unit
) {
  ViewRenderable.builder()
    .setView(context, viewId)
    .build()
    .thenAccept(onSuccess)
    .exceptionally { error ->
      onFailure(error)
      null
    }
}
```

### Add renderable to LocationArScene

```kotlin
fun LocationArScene.renderArObject(
  lat: Double,
  lng: Double,
  height: Float,
  renderable: Renderable
): LocationArMarker {
  val node = Node().apply {
    this.renderable = renderable
  }

  val marker = LocationArMarker(GlobalPosition(lat, lng), node).apply {
    this.height = height
    placementType = LocationArMarker.PlacementType.DYNAMIC
    rotationMode = LocationArMarker.RotationMode.FACE_USER
    scalingMode = LocationArMarker.ScalingMode.GRADUAL
  }

  addMarker(marker)

  return marker
}
```

### Util for handling ar session lifecycle

```kotlin
class ArSceneLifecycleUtil(
  private val activityContext: Context,
  private val arSceneView: ArSceneView
) {

  @Throws(UnavailableException::class, CameraNotAvailableException::class)
  fun onActivityResume() {
    try {
      if (arSceneView.session == null) {
        // no session so far, create new pme
        val session = Session(activityContext)

        // configure session
        Config(session).apply {
          updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        }.let {
          session.configure(it)
        }

        arSceneView.setupSession(session)
      }
    } catch (e: UnavailableException) {
      throw UnavailableException("Cannot resume ArSceneView, because ar core is not available: $e")
    }

    try {
      arSceneView.resume()
    } catch (e: CameraNotAvailableException) {
      throw CameraNotAvailableException("Cannot resume ArSceneView, because camera is not available: $e")
    }
  }

  fun onActivityPause() {
    arSceneView.pause()
  }

  fun onActivityDestroy() {
    arSceneView.destroy()
  }

}
```

### SimpleDemoActivity

```kotlin
class SimpleDemoActivity : AppCompatActivity() {

  private lateinit var locationArScene: LocationArScene
  private lateinit var arSceneLifecycleUtil: ArSceneLifecycleUtil

  private val arSceneView get() = arsceneview_simpledemoactivity

  // implement your own way of getting updates for location and bearing
  private val magicLocationRepository = MagicLocationRepository()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_simpledemo)

    locationArScene = LocationArScene(arSceneView).apply {
      startLocationScene()
    }

    arSceneLifecycleUtil = ArSceneLifecycleUtil(this, arSceneView)

    loadViewRenderable(
      this,
      R.layout.node_sample,
      { renderable ->
        // estimate height camera (-1.5m)
        // i haven't found a good solution to get the height from the ar framework
        locationArScene.renderArObject(
          42.0, // lat
          13.0, // lng
          -1.5, // height of object, compensate with camera height
          renderable
        )
      },
      {
        // handle error
      }
    )

    magicLocationRepository.getLocationUpdates { location ->
      locationArScene.onLocationChanged(GlobalPosition(location.lat, location.lng))
    }

    magicLocationRepository.getBearingUpdates { bearing ->
      locationArScene.onBearingChanged(bearing)
    }
  }

  override fun onResume() {
    super.onResume()
    try {
      arSceneLifecycleUtil.onActivityResume()
    } catch (e: CameraNotAvailableException) {
      // handle error
    } catch (e: UnavailableException) {
      // handle error
    }
  }

  override fun onPause() {
    super.onPause()
    arSceneLifecycleUtil.onActivityPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    arSceneLifecycleUtil.onActivityDestroy()
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) {
      window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }
```

### activity_simpledemo

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="match_parent"
  android:layout_height="match_parent">


  <com.google.ar.sceneform.ArSceneView
    android:id="@+id/arsceneview_simpledemoactivity"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### Relevant dependencies in build.gradle

```groovy
dependencies {
  // arcore
  implementation 'com.google.ar:core:1.16.0'
  implementation 'com.google.ar.sceneform.ux:sceneform-ux:1.15.0'
  implementation 'com.github.ericneid:arcore-location-extension:0.8.0'
}
```
