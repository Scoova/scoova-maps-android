/*
 * Copyright 2026 Scoova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.scoova.maps

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Standalone static-map URL helpers + style URL builder.
 *
 * These are intentionally decoupled from any MapLibre Android dependency —
 * use them from a server-side Kotlin/JVM job, a background worker, or an
 * `ImageView` loader without pulling in the GL renderer.
 *
 * Gateway:
 *   static map  -> https://api.scoo-va.info/api/v1/staticmap/{style}/static/{center}/{w}x{h}.png?...
 *   style URL   -> https://tiles.scoo-va.info/styles/{style}/style.json?...
 *
 * Locale: the gateway honours `?locale=` and `Accept-Language`.
 */

const val SCOOVA_DEFAULT_API_BASE: String = "https://api.scoo-va.info/api/v1"
const val SCOOVA_DEFAULT_TILES_BASE: String = "https://tiles.scoo-va.info"

data class StaticMapMarker(
    val lat: Double,
    val lon: Double,
    /** Hex (`#FF6A00`) or named color (`red`). */
    val color: String? = null,
    /** Built-in icon name, e.g. `pin`, `flag`. */
    val icon: String? = null,
)

data class StaticMapPath(
    val coordinates: List<ScoovaLatLng>,
    /** Stroke color, hex or named. */
    val stroke: String? = null,
    /** Line width in pixels. */
    val width: Int? = null,
)

data class StaticMapOptions(
    /** Style name, e.g. `scoova-light`, `scoova-dark`, `scoova-satellite`. */
    val style: String,
    /** Image width in pixels. */
    val width: Int,
    /** Image height in pixels. */
    val height: Int,
    /** Image center. Omit (and `zoom`) to auto-fit markers/paths. */
    val center: ScoovaLatLng? = null,
    /** Zoom level. Required when `center` is set. */
    val zoom: Double? = null,
    /** Padding in pixels when auto-fitting markers/paths. */
    val padding: Int? = null,
    val markers: List<StaticMapMarker> = emptyList(),
    val paths: List<StaticMapPath> = emptyList(),
    /** API key — appended as `?api_key=...` (works for ImageView/HTML img). */
    val apiKey: String,
    /** Override the API base, default [SCOOVA_DEFAULT_API_BASE]. */
    val apiBase: String = SCOOVA_DEFAULT_API_BASE,
    /** BCP-47 locale (`en`, `fr`, `ar-EG`, ...). Forwarded to the gateway. */
    val locale: String? = null,
)

data class StyleUrlOptions(
    /** API key — required by the gateway. */
    val apiKey: String,
    /** Override the tiles base, default [SCOOVA_DEFAULT_TILES_BASE]. */
    val tilesBase: String = SCOOVA_DEFAULT_TILES_BASE,
    /** Optional BCP-47 locale to localise place labels. */
    val locale: String? = null,
)

/** Pure URL builder for the static map endpoint. No network. */
fun staticMapUrl(opts: StaticMapOptions): String {
    val base = opts.apiBase.trimEnd('/')
    val parts = mutableListOf<String>()
    opts.padding?.let { parts.add("padding=$it") }
    for (m in opts.markers) {
        val tokens = mutableListOf<String>()
        m.color?.let { tokens.add("color:" + it.replace("#", "%23")) }
        m.icon?.let { tokens.add("icon:" + enc(it)) }
        tokens.add("${m.lat},${m.lon}")
        parts.add("marker=" + tokens.joinToString("|"))
    }
    for (p in opts.paths) {
        if (p.coordinates.size < 2) continue
        val tokens = mutableListOf<String>()
        p.stroke?.let { tokens.add("stroke:" + it.replace("#", "%23")) }
        p.width?.let { tokens.add("width:$it") }
        for (c in p.coordinates) tokens.add("${c.lat},${c.lon}")
        parts.add("path=" + tokens.joinToString("|"))
    }
    opts.locale?.let { parts.add("locale=" + enc(it)) }
    parts.add("api_key=" + enc(opts.apiKey))

    val size = "${opts.width}x${opts.height}"
    val centerSeg = if (opts.center != null && opts.zoom != null)
        "${opts.center.lon},${opts.center.lat},${opts.zoom}"
    else "auto"
    return "$base/staticmap/${enc(opts.style)}/static/$centerSeg/$size.png?" + parts.joinToString("&")
}

/**
 * Fetch the static-map PNG and return its bytes. Blocking — call from a
 * background thread / coroutine dispatcher. Throws [IOException] on
 * non-2xx responses.
 */
@Throws(IOException::class)
fun staticMapBytes(opts: StaticMapOptions): ByteArray {
    val url = URL(staticMapUrl(opts))
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15_000
        readTimeout = 15_000
        opts.locale?.let { setRequestProperty("Accept-Language", it) }
    }
    try {
        val code = conn.responseCode
        if (code !in 200..299) {
            throw IOException("staticMap: HTTP $code ${conn.responseMessage ?: ""}")
        }
        return conn.inputStream.use { it.readBytes() }
    } finally {
        conn.disconnect()
    }
}

/**
 * Scoova-compatible style URL. Drop into
 * `Style.Builder().fromUri(styleUrl("scoova-dark", StyleUrlOptions(apiKey = "...")))`.
 */
fun styleUrl(styleName: String, opts: StyleUrlOptions): String {
    val base = opts.tilesBase.trimEnd('/')
    val parts = mutableListOf("api_key=" + enc(opts.apiKey))
    opts.locale?.let { parts.add("locale=" + enc(it)) }
    return "$base/styles/${enc(styleName)}/style.json?" + parts.joinToString("&")
}

private fun enc(s: String): String =
    URLEncoder.encode(s, Charsets.UTF_8).replace("+", "%20")
