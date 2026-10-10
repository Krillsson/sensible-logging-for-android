package sh.vcm.sensiblelogging.util

import sh.vcm.sensiblelogging.Meta
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SourceLocationMetaTest {

    @Test
    internal fun `should split a swift file id into module and file`() {
        // WHEN
        val meta = metaFromSourceLocation("MoniteeiOS/ServerListView.swift", "body", 42, "main")

        // THEN
        assertEquals(
            Meta(
                className = "MoniteeiOS.ServerListView",
                simpleClassName = "ServerListView",
                functionName = "body",
                lineNumber = 42,
                threadName = "main",
                fileName = "ServerListView.swift"
            ),
            meta
        )
    }

    @Test
    internal fun `should use the file name alone when there is no module`() {
        // WHEN
        val meta = metaFromSourceLocation("ServerListView.swift", "load(serverId:)", 7, "worker")

        // THEN
        assertEquals("ServerListView", meta.className)
        assertEquals("ServerListView", meta.simpleClassName)
        assertEquals("ServerListView.swift", meta.fileName)
        assertEquals("load(serverId:)", meta.functionName)
    }
}
