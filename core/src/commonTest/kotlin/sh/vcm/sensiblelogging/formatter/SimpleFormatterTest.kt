package sh.vcm.sensiblelogging.formatter

import sh.vcm.sensiblelogging.Level
import sh.vcm.sensiblelogging.Line
import sh.vcm.sensiblelogging.Meta
import sh.vcm.sensiblelogging.util.currentTimeMillis
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SimpleFormatterTest {

    @Test
    internal fun `should start with a millisecond timestamp followed by level and category`() {
        // GIVEN
        val line = Line(
            currentTimeMillis(),
            "UnitTest",
            Level.WARN,
            "Something happened",
            false,
            null,
            emptyMap()
        )

        // WHEN
        val result = SimpleFormatter.format(line, null)

        // THEN
        val expected = Regex("""^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3} \[WARN] \[UnitTest] {4}- {4}Something happened""")
        assertTrue(expected.containsMatchIn(result), result)
    }

    @Test
    internal fun `should append the stack trace of the throwable`() {
        // GIVEN
        val line = Line(
            currentTimeMillis(),
            "UnitTest",
            Level.ERROR,
            "Something happened",
            false,
            IllegalStateException("boom"),
            emptyMap()
        )

        // WHEN
        val result = SimpleFormatter.format(line, null)

        // THEN
        assertTrue(result.contains(" exception[boom]\n"), result)
        assertTrue(result.contains("IllegalStateException"), result)
    }

    @Test
    internal fun `should print thread file and function from meta`() {
        // GIVEN
        val meta = Meta("com.example.ServerList", "ServerList", "refresh", 12, "main", "ServerList.kt")

        // WHEN
        val result = SimpleFormatter.format(line(), meta)

        // THEN
        assertTrue(result.contains("[UnitTest] main ServerList.kt:refresh    -    Something happened"), result)
    }

    @Test
    internal fun `should leave out meta fields that are missing`() {
        // GIVEN
        val meta = Meta(null, null, null, null, "main", null)

        // WHEN
        val result = SimpleFormatter.format(line(), meta)

        // THEN
        assertTrue(result.contains("[UnitTest] main    -    Something happened"), result)
        assertFalse(result.contains("null"), result)
    }

    private fun line() = Line(currentTimeMillis(), "UnitTest", Level.INFO, "Something happened", false, null, emptyMap())
}
