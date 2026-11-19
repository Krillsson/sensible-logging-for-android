package sh.vcm.sensiblelogging

import sh.vcm.sensiblelogging.channel.DebugChannel
import sh.vcm.sensiblelogging.filter.AllowAllFilter
import sh.vcm.sensiblelogging.filter.Filter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AppleMetaTest {

    private val channel = RecordingDebugChannel()

    @AfterTest
    internal fun tearDown() {
        Logger.Setup.clearChannels()
    }

    @Test
    internal fun `should point meta at the function calling the logger`() {
        // GIVEN
        Logger.Setup.addChannels(listOf(channel))

        // WHEN
        Logger.d("Something happened", "UnitTest")

        // THEN
        val meta = channel.metas.single()
        assertEquals("sh.vcm.sensiblelogging.AppleMetaTest", meta.className)
        assertEquals("AppleMetaTest", meta.simpleClassName)
        assertEquals("should point meta at the function calling the logger", meta.functionName)
        assertEquals("AppleMetaTest.kt", meta.fileName)
        assertTrue((meta.lineNumber ?: 0) > 0)
    }

    @Test
    internal fun `should skip extra frames for callers wrapping the logger`() {
        // GIVEN
        Logger.Setup.addChannels(listOf(channel))

        // WHEN
        logThroughWrapper("Something happened")

        // THEN
        assertEquals("should skip extra frames for callers wrapping the logger", channel.metas.single().functionName)
    }

    private fun logThroughWrapper(message: String) {
        Logger.log(Level.DEBUG, message, false, stackDepth = 5)
    }

    private class RecordingDebugChannel : DebugChannel() {
        val metas = mutableListOf<Meta>()
        override val filter: Filter = AllowAllFilter
        override val id: Int = 100
        override val default: Boolean = true
        override fun print(line: Line, meta: Meta) {
            metas += meta
        }
    }
}
