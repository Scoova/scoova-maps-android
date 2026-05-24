package com.scoova.maps

data class ScoovaLatLng(val lat: Double, val lon: Double)

data class ScoovaColors(
    val routePrimary: String = "#0EA5E9",
    val routeCasing: String = "#0369A1",
    val routeAlternate: String = "#94A3B8",
    val routeProgress: String = "#10B981",
    val markerFill: String = "#0EA5E9",
    val markerStroke: String = "#FFFFFF",
)

object ScoovaMapDefaults {
    const val styleUrl = "https://tiles.scoo-va.info/style.json"
    const val tilesUrl = "https://tiles.scoo-va.info/v1/{z}/{x}/{y}.mvt"
    const val attribution = "© Scoova · OpenStreetMap contributors"
    val defaultCenter = ScoovaLatLng(30.0444, 31.2357)
    const val defaultZoom = 12.0
    const val minZoom = 0.0
    const val maxZoom = 22.0
    val colors = ScoovaColors()

    /**
     * Returns [styleUrl] with `?locale=<locale>` appended. Use when feeding
     * a style URL into MapLibre Native Android so labels render in the
     * caller's locale.
     */
    fun styleUrlForLocale(locale: String): String =
        if (locale.isBlank()) styleUrl
        else "$styleUrl?locale=" + java.net.URLEncoder.encode(locale, Charsets.UTF_8).replace("+", "%20")
}
