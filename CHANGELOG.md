<!-- markdownlint-disable MD022 MD032 MD024-->
# Changelog
All notable changes to this project will be documented in this file.

## Unreleased
## Changed (Breaking)
* Moved LocationArNode to LocationArScene

## v0.8.0 - 2021-03-30
* First release to maven central
* groupId has changed from org.neidhardt to com.github.ericneid

## v0.7.2 - 2020-05-04
### Fixed
* Dynamically placing markers did not take the current camera position into account
(they were always placed relative to world origin and not to the camera)
* Dynamically placed marker was detached twice

## v0.7.0 - 2020-04-03
### Added
* Ability to set custom scale factor
### Changed
* Renamed scaleFactorForDistance -> linearScaleFactor
* Made some helper functions internal

## v0.6.0 - 2020-04-01
### Added
* Utils to calculate ar position from GlobalPosition
### Changed
* Renamed ArLocation -> GlobalPosition
* Property locationMarkers no longer public, use getMarkersInScene() and getRenderedMarkersInScene()
* Cleanup of non-ar related api -> made unrelated functions internal
* NodeUpdateEventListener transformed to kotlin lambda
* Changed constructor of LocationArNode to accept GlobalPosition instead of lat/lng

## v0.5.0 - 2020-03-30
### Added
* Property to determine the distance threshold between location updates
### Changed
* Adding a marker not longer updates all other markers in this location scene
* Renamed onRender listener -> onUpdate
* Added FrameTime ton onUpdate callback
* Made some properties of LocationArNode public

## v0.4.0 - 2020-03-26
### Changed
* Updated android build tools and dependencies

## v0.3.1 - 2020-03-26
### Fixed
* height of dynamic placed marker was ignored

## v0.3.0 - 2020-03-25
### Changed
* Renamed anchorRefreshInterval -> anchorRefreshIntervalSec
* LocationMarker: changed order of arguments lng,lat -> lat,lng
* Complete restructure of location scene api 

## v0.2.0 - 2020-03-11
### Added
* Initial commit
