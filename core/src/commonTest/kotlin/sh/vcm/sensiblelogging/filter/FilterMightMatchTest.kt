package sh.vcm.sensiblelogging.filter

import sh.vcm.sensiblelogging.Category
import sh.vcm.sensiblelogging.Level
import sh.vcm.sensiblelogging.Line
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FilterMightMatchTest {

    private val network = Category("Network")
    private val ui = Category("UI")

    private val filters = mapOf(
        "allow all" to Filter.allowAll(),
        "level" to Filter.level(Level.WARN),
        "categories" to Filter.categories(listOf(network)),
        "level or categories" to (Filter.level(Level.WARN) or Filter.categories(listOf(network))),
        "level and categories" to (Filter.level(Level.WARN) and Filter.categories(listOf(network))),
    )

    @Test
    internal fun `should agree with matches for every level and category`() {
        // GIVEN
        val lines = Level.entries.flatMap { level ->
            listOf(network, ui).map { category ->
                Line(0L, category, level, "Something happened", false, null, emptyMap())
            }
        }

        filters.forEach { (name, filter) ->
            lines.forEach { line ->
                // WHEN
                val matches = filter.matches(line)
                val mightMatch = filter.mightMatch(line.level, line.category)

                // THEN
                assertEquals(matches, mightMatch, "$name for ${line.level} ${line.category.name}")
            }
        }
    }

    @Test
    internal fun `should default to true for filters that do not override it`() {
        // GIVEN
        val filter = object : Filter {
            override fun matches(line: Line): Boolean = false
        }

        // WHEN
        val result = filter.mightMatch(Level.VERBOSE, ui)

        // THEN
        assertTrue(result)
    }
}
