package com.scoova.maps

import org.json.JSONArray
import org.json.JSONObject

data class ScoovaStyleOptions(
    val rasterUrls: List<String> = emptyList(),
    val buildings3d: Boolean = true,
)

/**
 * Build an inline MapLibre style spec pointing at Scoova's vector tiles.
 *
 * Returns a [JSONObject] you can pass to MapLibre Native Android's
 * `Style.Builder().fromJson(json.toString())`.
 */
fun buildInlineStyle(options: ScoovaStyleOptions = ScoovaStyleOptions()): JSONObject {
    val sources = JSONObject()
    sources.put("scoova-vector", JSONObject().apply {
        put("type", "vector")
        put("tiles", JSONArray().put(ScoovaMapDefaults.tilesUrl))
        put("minzoom", ScoovaMapDefaults.minZoom)
        put("maxzoom", ScoovaMapDefaults.maxZoom)
        put("attribution", ScoovaMapDefaults.attribution)
    })
    if (options.rasterUrls.isNotEmpty()) {
        sources.put("scoova-raster", JSONObject().apply {
            put("type", "raster")
            put("tiles", JSONArray().apply { options.rasterUrls.forEach { put(it) } })
            put("tileSize", 256)
        })
    }

    val layers = JSONArray()
    layers.put(JSONObject().apply {
        put("id", "background")
        put("type", "background")
        put("paint", JSONObject().put("background-color", "#F8FAFC"))
    })
    if (options.rasterUrls.isNotEmpty()) {
        layers.put(JSONObject().apply {
            put("id", "raster")
            put("type", "raster")
            put("source", "scoova-raster")
        })
    }
    if (options.buildings3d) {
        layers.put(JSONObject().apply {
            put("id", "buildings-3d")
            put("type", "fill-extrusion")
            put("source", "scoova-vector")
            put("source-layer", "building")
            put("minzoom", 15)
            put("paint", JSONObject().apply {
                put("fill-extrusion-color", "#E2E8F0")
                put("fill-extrusion-height", JSONArray().put("coalesce")
                    .put(JSONArray().put("get").put("render_height")).put(10))
                put("fill-extrusion-base", JSONArray().put("coalesce")
                    .put(JSONArray().put("get").put("render_min_height")).put(0))
                put("fill-extrusion-opacity", 0.85)
            })
        })
    }

    return JSONObject().apply {
        put("version", 8)
        put("name", "Scoova Default")
        put("sources", sources)
        put("layers", layers)
    }
}

data class ScoovaRouteFeature(
    val shape: JSONObject,
    val casingPaint: JSONObject,
    val linePaint: JSONObject,
)

fun routeFeature(
    coords: List<Pair<Double, Double>>,
    color: String? = null,
    casingColor: String? = null,
    width: Double = 6.0,
    alternate: Boolean = false,
): ScoovaRouteFeature {
    val c = color ?: if (alternate) ScoovaMapDefaults.colors.routeAlternate else ScoovaMapDefaults.colors.routePrimary
    val cc = casingColor ?: ScoovaMapDefaults.colors.routeCasing
    val coordArr = JSONArray().apply {
        coords.forEach { put(JSONArray().put(it.first).put(it.second)) }
    }
    val shape = JSONObject().apply {
        put("type", "Feature")
        put("properties", JSONObject())
        put("geometry", JSONObject().put("type", "LineString").put("coordinates", coordArr))
    }
    val casing = JSONObject().apply {
        put("line-color", cc)
        put("line-width", width + 3)
        put("line-opacity", if (alternate) 0.4 else 0.7)
        put("line-cap", "round")
        put("line-join", "round")
    }
    val line = JSONObject().apply {
        put("line-color", c)
        put("line-width", width)
        put("line-opacity", if (alternate) 0.6 else 1.0)
        put("line-cap", "round")
        put("line-join", "round")
        if (alternate) put("line-dasharray", JSONArray().put(2).put(2))
    }
    return ScoovaRouteFeature(shape, casing, line)
}

data class ScoovaMarkerFeature(
    val shape: JSONObject,
    val circlePaint: JSONObject,
)

fun markerFeature(
    position: ScoovaLatLng,
    color: String? = null,
    radius: Double = 8.0,
    properties: JSONObject = JSONObject(),
): ScoovaMarkerFeature {
    val shape = JSONObject().apply {
        put("type", "Feature")
        put("properties", properties)
        put("geometry", JSONObject()
            .put("type", "Point")
            .put("coordinates", JSONArray().put(position.lon).put(position.lat)))
    }
    val circle = JSONObject().apply {
        put("circle-radius", radius)
        put("circle-color", color ?: ScoovaMapDefaults.colors.markerFill)
        put("circle-stroke-width", 2)
        put("circle-stroke-color", ScoovaMapDefaults.colors.markerStroke)
    }
    return ScoovaMarkerFeature(shape, circle)
}

/** `[[minLon, minLat], [maxLon, maxLat]]` for fitting the camera to a list of points. */
fun bboxOf(points: List<ScoovaLatLng>): List<List<Double>>? {
    if (points.isEmpty()) return null
    var minLon = points[0].lon
    var maxLon = points[0].lon
    var minLat = points[0].lat
    var maxLat = points[0].lat
    for (p in points) {
        if (p.lon < minLon) minLon = p.lon
        if (p.lon > maxLon) maxLon = p.lon
        if (p.lat < minLat) minLat = p.lat
        if (p.lat > maxLat) maxLat = p.lat
    }
    return listOf(listOf(minLon, minLat), listOf(maxLon, maxLat))
}
