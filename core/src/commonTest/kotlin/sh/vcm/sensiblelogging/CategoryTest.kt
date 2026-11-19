package sh.vcm.sensiblelogging

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CategoryTest {

    @Test
    @Suppress("DEPRECATION")
    internal fun `should keep the value class style compiling`() {
        // GIVEN
        val category = Category("Network")

        // THEN
        assertEquals("Network", category)
        assertEquals("Network", category.name)
    }
}
