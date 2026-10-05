package sh.vcm.sensiblelogging

import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import sh.vcm.sensiblelogging.channel.DebugChannel
import sh.vcm.sensiblelogging.filter.AllowAllFilter
import sh.vcm.sensiblelogging.filter.Filter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class LoggerBridgeTest {

    private val channel = RecordingDebugChannel()

    @AfterTest
    internal fun tearDown() {
        Logger.Setup.clearChannels()
    }

    @Test
    internal fun `should log the swift call site and error`() {
        // GIVEN
        Logger.Setup.addChannels(listOf(channel))
        val error = NSError.errorWithDomain(
            "SampleError",
            7,
            mapOf<Any?, Any?>(NSLocalizedDescriptionKey to "sample failure")
        )

        // WHEN
        LoggerBridge.log(
            level = Level.WARN,
            message = "Could not connect",
            category = "Network",
            error = error,
            parameters = mapOf("host" to "nas.local"),
            fileId = "MoniteeiOS/ServerListView.swift",
            function = "body",
            line = 42
        )

        // THEN
        val (line, meta) = channel.entries.single()
        assertEquals(Level.WARN, line.level)
        assertEquals(Category("Network"), line.category)
        assertEquals("Could not connect", line.message)
        assertEquals(mapOf("host" to "nas.local"), line.parameters)
        assertEquals("sample failure", assertIs<NSErrorException>(line.throwable).message)
        assertEquals("ServerListView.swift", meta.fileName)
        assertEquals("body", meta.functionName)
        assertEquals(42, meta.lineNumber)
        assertTrue(meta.threadName.isNotEmpty())
    }

    private class RecordingDebugChannel : DebugChannel() {
        val entries = mutableListOf<Pair<Line, Meta>>()
        override val filter: Filter = AllowAllFilter
        override val id: Int = 100
        override val default: Boolean = true
        override fun print(line: Line, meta: Meta) {
            entries += line to meta
        }
    }
}
