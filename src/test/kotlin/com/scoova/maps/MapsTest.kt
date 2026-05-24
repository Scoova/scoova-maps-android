package com.scoova.maps

import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DefaultsTest {
    @Test fun pointsAtScoovaDomain() {
        assertEquals("https://tiles.scoo-va.info/style.json", ScoovaMapDefaults.styleUrl)
        assertEquals("https://tiles.scoo-va.info/v1/{z}/{x}/{y}.mvt", ScoovaMapDefaults.tilesUrl)
        assertEquals(30.0444, ScoovaMapDefaults.defaultCenter.lat, 0.0001)
        assertEquals(31.2357, ScoovaMapDefaults.defaultCenter.lon, 0.0001)
    }
}

class InlineStyleTest {
    @Test fun producesV8StyleWithVectorSource() {
        val style = buildInlineStyle()
        assertEquals(8, style.getInt("version"))
        val src = style.getJSONObject("sources").getJSONObject("scoova-vector")
        assertEquals("vector", src.getString("type"))
        assertEquals(ScoovaMapDefaults.tilesUrl, src.getJSONArray("tiles").getString(0))
        val layers = style.getJSONArray("layers")
        var hasBuildings = false
        for (i in 0 until layers.length()) {
            if (layers.getJSONObject(i).getString("id") == "buildings-3d") hasBuildings = true
        }
        assertTrue(hasBuildings)
    }

    @Test fun omitsBuildings3dWhenDisabled() {
        val style = buildInlineStyle(ScoovaStyleOptions(buildings3d = false))
        val layers = style.getJSONArray("layers")
        for (i in 0 until layers.length()) {
            assertFalse(layers.getJSONObject(i).getString("id") == "buildings-3d")
        }
    }

    @Test fun addsRasterWhenUrlsProvided() {
        val style = buildInlineStyle(ScoovaStyleOptions(rasterUrls = listOf("https://example.test/{z}/{x}/{y}.png")))
        assertNotNull(style.getJSONObject("sources").optJSONObject("scoova-raster"))
        val layers = style.getJSONArray("layers")
        var hasRaster = false
        for (i in 0 until layers.length()) {
            if (layers.getJSONObject(i).getString("id") == "raster") hasRaster = true
        }
        assertTrue(hasRaster)
    }
}

class FeatureBuilderTest {
    @Test fun routeFeaturePrimaryStyle() {
        val f = routeFeature(listOf(31.24 to 30.04, 31.25 to 30.05))
        val geom = f.shape.getJSONObject("geometry")
        assertEquals("LineString", geom.getString("type"))
        assertEquals(ScoovaMapDefaults.colors.routePrimary, f.linePaint.getString("line-color"))
        assertEquals(ScoovaMapDefaults.colors.routeCasing, f.casingPaint.getString("line-color"))
    }

    @Test fun routeFeatureAlternateStyle() {
        val f = routeFeature(listOf(0.0 to 0.0, 1.0 to 1.0), alternate = true)
        assertEquals(ScoovaMapDefaults.colors.routeAlternate, f.linePaint.getString("line-color"))
        val dash = f.linePaint.getJSONArray("line-dasharray")
        assertEquals(2, dash.length())
    }

    @Test fun markerFeatureBuildsPoint() {
        val props = JSONObject().put("name", "X")
        val f = markerFeature(ScoovaLatLng(30.04, 31.24), properties = props)
        val geom = f.shape.getJSONObject("geometry")
        assertEquals("Point", geom.getString("type"))
        val coords = geom.getJSONArray("coordinates")
        assertEquals(31.24, coords.getDouble(0), 0.0001)
        assertEquals(30.04, coords.getDouble(1), 0.0001)
        assertEquals("X", f.shape.getJSONObject("properties").getString("name"))
    }
}

class BboxTest {
    @Test fun returnsNullForEmpty() {
        assertNull(bboxOf(emptyList()))
    }

    @Test fun computesBounds() {
        val bb = bboxOf(listOf(
            ScoovaLatLng(30.0, 31.0),
            ScoovaLatLng(32.0, 29.0),
            ScoovaLatLng(31.0, 33.0),
        ))
        assertNotNull(bb)
        assertEquals(listOf(listOf(29.0, 30.0), listOf(33.0, 32.0)), bb)
    }
}
