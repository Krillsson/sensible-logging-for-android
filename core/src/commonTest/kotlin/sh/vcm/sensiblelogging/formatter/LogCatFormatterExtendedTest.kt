package sh.vcm.sensiblelogging.formatter

import sh.vcm.sensiblelogging.Level
import sh.vcm.sensiblelogging.Line
import sh.vcm.sensiblelogging.Meta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LogCatFormatterExtendedTest {

    private val line = Line(0, "Network", Level.INFO, "Loaded", false, null, emptyMap())

    @Test
    internal fun `should print thread file line and function from meta`() {
        // GIVEN
        val meta = Meta("com.example.ServerList", "ServerList", "refresh", 12, "main", "ServerList.kt")

        // WHEN
        val result = LogCatFormatterExtended.format(line, meta)

        // THEN
        assertTrue(result.contains("main] : ServerList.kt:12.refresh(…)"), result)
        assertTrue(result.endsWith(" : Loaded"), result)
    }

    @Test
    internal fun `should leave out meta fields that are missing`() {
        // GIVEN
        val meta = Meta(null, null, null, null, "main", null)

        // WHEN
        val result = LogCatFormatterExtended.format(line, meta)

        // THEN
        assertTrue(result.endsWith("main] : Loaded"), result)
        assertFalse(result.contains("null"), result)
    }

    @Test
    internal fun `should print the function without a file`() {
        // GIVEN
        val meta = Meta(null, null, "refresh", null, null, null)

        // WHEN
        val result = LogCatFormatterExtended.format(line, meta)

        // THEN
        assertTrue(result.contains(" -- refresh(…)"), result)
    }

    @Test
    internal fun `should print only categories and message without meta`() {
        // WHEN
        val result = LogCatFormatterExtended.format(line, null)

        // THEN
        assertEquals("Network", result.substringBefore(" -- ").trimEnd('\u2007'))
        assertTrue(result.endsWith(" -- Loaded"), result)
    }
}
