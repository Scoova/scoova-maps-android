# scoova-maps-android (Kotlin / JVM)

Scoova map SDK for Android. Two things in one library:

1. **MapLibre helpers** — `buildInlineStyle`, `routeFeature`, `markerFeature`,
   `bboxOf`, and the canonical Scoova defaults — to feed into MapLibre Native
   Android (`Style.Builder`, `ShapeSource`, `LineLayer`, `CircleLayer`).
2. **Standalone static-map + style URL builders** that work without MapLibre
   Native — perfect for `ImageView` loaders, OG share images on the server,
   PDF receipts, etc.

Maven artifact: `info.scoo-va:scoova-maps-android:1.1.3`

## Install

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // Or, until Maven Central propagation finishes:
        maven("https://maven.pkg.github.com/Scoova/scoova-maps-android")
    }
}

// build.gradle.kts
dependencies {
    implementation("info.scoo-va:scoova-maps-android:1.1.3")
}
```

## Interactive map (with MapLibre Native Android)

```kotlin
import com.scoova.maps.*

mapView.getMapAsync { map ->
    map.setStyle(
        Style.Builder().fromUri(
            ScoovaMapDefaults.styleUrlForLocale("fr")  // ?locale=fr appended
        )
    )
    map.cameraPosition = CameraPosition.Builder()
        .target(LatLng(
            ScoovaMapDefaults.defaultCenter.lat,
            ScoovaMapDefaults.defaultCenter.lon))
        .zoom(ScoovaMapDefaults.defaultZoom)
        .build()
}

val route = routeFeature(listOf(31.24 to 30.04, 31.25 to 30.05))
val marker = markerFeature(ScoovaLatLng(30.04, 31.24))
```

## Static map URL (no MapLibre needed)

```kotlin
import com.scoova.maps.*

val url = staticMapUrl(StaticMapOptions(
    apiKey = "sk_live_…",
    style = "scoova-light",
    width = 600, height = 400,
    center = ScoovaLatLng(30.0444, 31.2357), zoom = 13.0,
    markers = listOf(
        StaticMapMarker(lat = 30.0444, lon = 31.2357, color = "#FF6A00"),
    ),
    paths = listOf(
        StaticMapPath(
            coordinates = listOf(
                ScoovaLatLng(30.04, 31.24),
                ScoovaLatLng(30.05, 31.25),
                ScoovaLatLng(30.06, 31.26),
            ),
            stroke = "#0EA5E9", width = 4,
        ),
    ),
    locale = "fr",
))
// Picasso/Coil/Glide.load(url).into(imageView)
```

Or fetch the bytes directly (blocking — call from a worker dispatcher):

```kotlin
val bytes = staticMapBytes(StaticMapOptions(...))
val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
```

## Style URL (without `ScoovaMapDefaults`)

```kotlin
val styleUri = styleUrl("scoova-dark", StyleUrlOptions(
    apiKey = "sk_live_…",
    locale = "es",
))
mapView.getMapAsync { map ->
    map.setStyle(Style.Builder().fromUri(styleUri))
}
```

## API

### MapLibre helpers
- `ScoovaMapDefaults` — `styleUrl`, `tilesUrl`, `defaultCenter`, `colors`,
  `styleUrlForLocale(locale)`
- `buildInlineStyle(options)` — full v8 style as `JSONObject`
- `routeFeature(coords, ...)` — `ScoovaRouteFeature { shape, casingPaint, linePaint }`
- `markerFeature(position, ...)` — `ScoovaMarkerFeature { shape, circlePaint }`
- `bboxOf(points)` — `[[minLon, minLat], [maxLon, maxLat]]`

### Static + style helpers
- `staticMapUrl(opts)` — pure URL builder
- `staticMapBytes(opts)` — blocking `ByteArray` fetcher
- `styleUrl(styleName, opts)` — Scoova-compatible style URL
- `SCOOVA_DEFAULT_API_BASE`, `SCOOVA_DEFAULT_TILES_BASE`

## Tests

```
gradle test
```
