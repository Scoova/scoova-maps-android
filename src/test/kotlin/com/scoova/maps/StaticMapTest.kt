/*
 * Copyright 2026 Scoova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.scoova.maps

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StaticMapUrlTest {
    @Test fun explicitCenterPointsAtGateway() {
        val url = staticMapUrl(StaticMapOptions(
            style = "scoova-light", width = 600, height = 400,
            center = ScoovaLatLng(30.0444, 31.2357), zoom = 13.0,
            apiKey = "k123",
        ))
        assertTrue(url.startsWith("$SCOOVA_DEFAULT_API_BASE/staticmap/scoova-light/static/"))
        assertTrue(url.contains("/static/31.2357,30.0444,13.0/"))
        assertTrue(url.contains("600x400.png"))
        assertTrue(url.contains("api_key=k123"))
    }

    @Test fun noCenterUsesAuto() {
        val url = staticMapUrl(StaticMapOptions(
            style = "scoova-dark", width = 100, height = 100,
            markers = listOf(StaticMapMarker(lat = 30.0, lon = 31.0)),
            apiKey = "k",
        ))
        assertTrue(url.contains("/static/auto/"))
    }

    @Test fun markerWithColorAndIconIsSerialised() {
        val url = staticMapUrl(StaticMapOptions(
            style = "s", width = 1, height = 1,
            markers = listOf(StaticMapMarker(lat = 30.0, lon = 31.0, color = "#FF6A00", icon = "pin")),
            apiKey = "k",
        ))
        assertTrue(url.contains("marker=color:%23FF6A00|icon:pin|30.0,31.0"))
    }

    @Test fun pathsWithFewerThanTwoCoordsAreDropped() {
        val url = staticMapUrl(StaticMapOptions(
            style = "s", width = 1, height = 1,
            paths = listOf(
                StaticMapPath(
                    coordinates = listOf(ScoovaLatLng(30.0, 31.0), ScoovaLatLng(31.0, 32.0)),
                    stroke = "#0EA5E9", width = 4,
                ),
                StaticMapPath(coordinates = listOf(ScoovaLatLng(0.0, 0.0))),
            ),
            apiKey = "k",
        ))
        assertTrue(url.contains("path=stroke:%230EA5E9|width:4|30.0,31.0|31.0,32.0"))
        assertEquals(1, url.split("path=").size - 1)
    }

    @Test fun localeIsForwarded() {
        val url = staticMapUrl(StaticMapOptions(
            style = "s", width = 1, height = 1, apiKey = "k", locale = "ar-EG",
        ))
        assertTrue(url.contains("locale=ar-EG"))
    }

    @Test fun apiBaseOverrideAndTrailingSlashStripped() {
        val url = staticMapUrl(StaticMapOptions(
            style = "s", width = 1, height = 1, apiKey = "k",
            apiBase = "https://gateway.example.test/api/v1/",
        ))
        assertTrue(url.startsWith("https://gateway.example.test/api/v1/staticmap/"))
    }
}

class StyleUrlTest {
    @Test fun pointsAtTilesByDefault() {
        val url = styleUrl("scoova-light", StyleUrlOptions(apiKey = "k"))
        assertTrue(url.startsWith("$SCOOVA_DEFAULT_TILES_BASE/styles/scoova-light/style.json?"))
        assertTrue(url.contains("api_key=k"))
    }

    @Test fun includesLocaleAndSupportsTilesBaseOverride() {
        val url = styleUrl("scoova-dark", StyleUrlOptions(
            apiKey = "k", locale = "pt-BR", tilesBase = "https://my-tiles.example.test/",
        ))
        assertTrue(url.startsWith("https://my-tiles.example.test/styles/scoova-dark/style.json?"))
        assertTrue(url.contains("locale=pt-BR"))
    }
}
