<!-- markdownlint-disable MD022 MD032 MD024-->
# Changelog
All notable changes to this project will be documented in this file.

## Unreleased

### Changed
* Renamed ArLocation -> GlobalPosition
* Property locationMarkers no longer public, use getMarkersInScene() and getRenderedMarkersInScene()
* Cleanup of non-ar related api -> made unrelated functions internal

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
