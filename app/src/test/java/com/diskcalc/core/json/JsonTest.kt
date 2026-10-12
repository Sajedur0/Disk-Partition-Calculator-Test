package com.diskcalc.core.json

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonTest {

    @Test
    fun `serializes nested structures compactly`() {
        val value = jsonObject(
            "id" to "abc".asJson(),
            "count" to 4.asJson(),
            "ratio" to 119226.75.asJson(),
            "enabled" to true.asJson(),
            "missing" to JsonValue.Null,
            "items" to JsonValue.Arr(listOf(JsonValue.Num(1.0), JsonValue.Str("two"))),
        )

        val expected = """{"id":"abc","count":4,"ratio":119226.75,"enabled":true,""" +
            """"missing":null,"items":[1,"two"]}"""

        assertEquals(expected, value.toJsonString())
    }

    @Test
    fun `round trips through the parser`() {
        val json =
            """{"a":[1,2,{"b":"c\n\t\"quoted\""}],"empty":{},"list":[],"nul":null,"f":false}"""
        val parsed = JsonParser.parse(json) as JsonValue.Obj

        assertEquals(json, parsed.toJsonString())
        assertEquals("""c${'\n'}${'\t'}"quoted"""", parsed.arrOrNull("a")!!.items[2].let {
            (it as JsonValue.Obj).string("b")
        })
        assertTrue(parsed.arrOrNull("list")!!.items.isEmpty())
        assertEquals(JsonValue.Null, parsed.entries["nul"])
    }

    @Test
    fun `parses unicode escapes`() {
        val parsed = JsonParser.parse("""{"label":"119230.75\u00d7 MB"}""") as JsonValue.Obj
        assertEquals("119230.75× MB", parsed.string("label"))
    }

    @Test
    fun `rejects malformed payloads without throwing`() {
        assertNull(JsonParser.parse(null))
        assertNull(JsonParser.parse(""))
        assertNull(JsonParser.parse("{"))
        assertNull(JsonParser.parse("""{"a":}"""))
        assertNull(JsonParser.parse("""{"a":1,}"""))
        assertNull(JsonParser.parse("[1,2"))
        assertNull(JsonParser.parse("""{"a":01.}"""))
        assertNull(JsonParser.parse("""{"a":1} trailing"""))
        assertNull(JsonParser.parse("nope"))
    }

    @Test
    fun `number output keeps integers integral and fractions exact`() {
        assertEquals("0", JsonValue.Num(0.0).toJsonString())
        assertEquals("-3", JsonValue.Num(-3.0).toJsonString())
        assertEquals("119230.75", JsonValue.Num(119230.75).toJsonString())
        assertEquals("0.5", JsonValue.Num(0.5).toJsonString())
        assertEquals("null", JsonValue.Num(Double.NaN).toJsonString())
    }

    @Test
    fun `accessors fall back to defaults`() {
        val parsed = JsonParser.parse("""{"name":"x","size":1024}""") as JsonValue.Obj

        assertEquals("x", parsed.string("name"))
        assertEquals("fallback", parsed.string("unknown", default = "fallback"))
        assertEquals(1024, parsed.int("size"))
        assertEquals(7, parsed.int("absent", default = 7))
        assertNull(parsed.doubleOrNull("absent"))
        assertNull(parsed.stringOrNull("size"))
    }
}
