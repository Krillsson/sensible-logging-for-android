package sh.vcm.sensiblelogging.channel

import sh.vcm.sensiblelogging.Level
import sh.vcm.sensiblelogging.Line
import sh.vcm.sensiblelogging.Meta
import sh.vcm.sensiblelogging.filter.AllowAllFilter
import sh.vcm.sensiblelogging.formatter.LogCatFormatterExtended
import kotlin.test.Test

internal class NSLogChannelTest {

    private val channel = NSLogChannel(LogCatFormatterExtended, AllowAllFilter)
    private val meta = Meta("ServerList", "ServerList", "refresh", 12, "main", "ServerList.kt")

    @Test
    internal fun `should print messages containing format specifiers`() {
        channel.print(line("Loaded %@ servers, 100% done, %s %d %n"), meta)
    }

    @Test
    internal fun `should print pre formatted messages containing format specifiers`() {
        channel.print(line("Loaded %@ servers, 100% done, %s %d %n", preFormatted = true), meta)
    }

    private fun line(message: String, preFormatted: Boolean = false) = Line(
        timestamp = 0,
        category = "Network",
        level = Level.DEBUG,
        message = message,
        preFormatted = preFormatted,
        throwable = IllegalStateException("100% broken %@"),
        parameters = mapOf("query" to "a%20b")
    )
}
