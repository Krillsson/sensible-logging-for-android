package sh.vcm.sensiblelogging.formatter

import sh.vcm.sensiblelogging.Category
import sh.vcm.sensiblelogging.Level
import sh.vcm.sensiblelogging.Line
import sh.vcm.sensiblelogging.util.currentTimeMillis
import kotlin.test.Test
import kotlin.test.assertTrue

internal class SimpleFormatterTest {

    @Test
    internal fun `should start with a millisecond timestamp followed by level and category`() {
        // GIVEN
        val line = Line(
            currentTimeMillis(),
            Category("UnitTest"),
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
            Category("UnitTest"),
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
}
