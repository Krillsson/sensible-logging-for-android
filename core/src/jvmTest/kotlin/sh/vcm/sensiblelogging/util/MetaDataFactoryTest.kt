package sh.vcm.sensiblelogging.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import sh.vcm.sensiblelogging.Category
import sh.vcm.sensiblelogging.Line
import sh.vcm.sensiblelogging.Logger
import sh.vcm.sensiblelogging.Meta
import sh.vcm.sensiblelogging.channel.DebugChannel
import sh.vcm.sensiblelogging.filter.AllowAllFilter
import sh.vcm.sensiblelogging.filter.Filter

internal class MetaDataFactoryTest {

    private val channel = object : DebugChannel() {
        val metas = mutableListOf<Meta>()
        override val filter: Filter = AllowAllFilter
        override val id: Int = 100
        override val default: Boolean = true
        override fun print(line: Line, meta: Meta) {
            metas += meta
        }
    }

    @AfterEach
    internal fun tearDown() {
        Logger.Setup.clearChannels()
    }

    @Test
    internal fun `should point meta at the function calling the logger`() {
        // GIVEN
        Logger.Setup.addChannels(listOf(channel))

        // WHEN
        Logger.d("Something happened", Category("UnitTest"))

        // THEN
        val meta = channel.metas.single()
        assertEquals("MetaDataFactoryTest", meta.simpleClassName)
        assertTrue(meta.functionName.startsWith("should point meta at the function calling the logger"), meta.functionName)
        assertEquals("MetaDataFactoryTest.kt", meta.fileName)
        assertEquals(Thread.currentThread().name, meta.threadName)
    }
}
