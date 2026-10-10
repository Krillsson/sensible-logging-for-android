package sh.vcm.sensiblelogging

import sh.vcm.sensiblelogging.channel.DebugChannel
import sh.vcm.sensiblelogging.channel.ReleaseChannel
import sh.vcm.sensiblelogging.filter.AllowAllFilter
import sh.vcm.sensiblelogging.filter.Filter
import sh.vcm.sensiblelogging.util.currentTimeMillis
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class LoggerTest {

    private val category = Category("UnitTest")

    @AfterTest
    internal fun tearDown() {
        Logger.Setup.clearChannels()
    }

    @Test
    internal fun `should deliver the line to a release channel`() {
        // GIVEN
        val channel = RecordingReleaseChannel()
        val error = IllegalStateException("boom")
        Logger.Setup.addChannels(listOf(channel))
        val before = currentTimeMillis()

        // WHEN
        Logger.e("Something happened", error, category)

        // THEN
        val line = channel.lines.single()
        assertEquals(Level.ERROR, line.level)
        assertEquals(category, line.category)
        assertEquals("Something happened", line.message)
        assertSame(error, line.throwable)
        assertTrue(line.timestamp in before..currentTimeMillis())
    }

    @Test
    internal fun `should give debug channels meta with a thread name`() {
        // GIVEN
        val channel = RecordingDebugChannel()
        Logger.Setup.addChannels(listOf(channel))

        // WHEN
        Logger.d("Something happened", category)

        // THEN
        assertTrue(channel.metas.single().threadName.isNotEmpty())
    }

    @Test
    internal fun `should not deliver to channels removed from setup`() {
        // GIVEN
        val channel = RecordingReleaseChannel()
        Logger.Setup.addChannels(listOf(channel))
        Logger.Setup.removeChannels(channel)

        // WHEN
        Logger.i("Something happened")

        // THEN
        assertTrue(channel.lines.isEmpty())
    }

    @Test
    internal fun `should give debug channels the meta passed to log`() {
        // GIVEN
        val channel = RecordingDebugChannel()
        val meta = Meta("ContentView", "ContentView", "body", 42, "main", "ContentView.swift")
        Logger.Setup.addChannels(listOf(channel))

        // WHEN
        Logger.log(Level.INFO, "Something happened", false, meta, category)

        // THEN
        assertSame(meta, channel.metas.single())
    }

    private class RecordingReleaseChannel : ReleaseChannel() {
        val lines = mutableListOf<Line>()
        override val filter: Filter = AllowAllFilter
        override val id: Int = 100
        override val default: Boolean = true
        override fun print(line: Line) {
            lines += line
        }
    }

    private class RecordingDebugChannel : DebugChannel() {
        val metas = mutableListOf<Meta>()
        override val filter: Filter = AllowAllFilter
        override val id: Int = 101
        override val default: Boolean = true
        override fun print(line: Line, meta: Meta) {
            metas += meta
        }
    }
}
