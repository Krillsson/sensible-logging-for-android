package sh.vcm.sensiblelogging.util

import sh.vcm.sensiblelogging.Meta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class StackFrameMetaTest {

    @Test
    internal fun `should read a member function with its source location`() {
        // GIVEN
        val frame = "2   test.kexe   0x10672eafb   kfun:com.example.ServerList#refresh(kotlin.Boolean){} + 59 " +
            "(/src/com/example/ServerList.kt:6:28)"

        // WHEN
        val meta = metaFromStackFrame(frame, "main")

        // THEN
        assertEquals(Meta("com.example.ServerList", "ServerList", "refresh", 6, "main", "ServerList.kt"), meta)
    }

    @Test
    internal fun `should read a private function`() {
        // GIVEN
        val frame = "1   test.kexe   0x10672e9e2   kfun:com.example.ServerList.load#internal + 450 " +
            "(/src/com/example/ServerList.kt:5:51)"

        // WHEN
        val meta = metaFromStackFrame(frame, "main")

        // THEN
        assertEquals("com.example.ServerList", meta?.className)
        assertEquals("load", meta?.functionName)
    }

    @Test
    internal fun `should name top level functions after their file`() {
        // GIVEN
        val frame = "3   App   0x1053f4de7   kfun:com.example#startSync(){} + 599 (/src/com/example/Sync.kt:26:88)"

        // WHEN
        val meta = metaFromStackFrame(frame, "main")

        // THEN
        assertEquals("com.example.SyncKt", meta?.className)
        assertEquals("SyncKt", meta?.simpleClassName)
        assertEquals("startSync", meta?.functionName)
    }

    @Test
    internal fun `should drop the receiver from extension functions`() {
        // GIVEN
        val frame = "3   App   0x1053f4de7   kfun:com.example#asThrowable__at__platform.Foundation.NSError(){}" +
            "kotlin.Throwable + 599 (/src/com/example/Errors.kt:26:88)"

        // WHEN
        val meta = metaFromStackFrame(frame, "main")

        // THEN
        assertEquals("asThrowable", meta?.functionName)
    }

    @Test
    internal fun `should keep the symbol when there is no source location`() {
        // GIVEN
        val frame = "2   App   0x10672eafb   kfun:com.example.ServerList#refresh(){} + 59 "

        // WHEN
        val meta = metaFromStackFrame(frame, "main")

        // THEN
        assertEquals(Meta("com.example.ServerList", "ServerList", "refresh", 0, "main", "n/a"), meta)
    }

    @Test
    internal fun `should not read frames without a kotlin symbol`() {
        assertNull(metaFromStackFrame("9   App   0x105214df8   main + 8 ", "main"))
    }

    @Test
    internal fun `should pick the frame after the last logger frame`() {
        // GIVEN
        val frames = arrayOf(
            "0   App   0x1   kfun:kotlin.Throwable#<init>(){} + 66 ",
            "1   App   0x2   kfun:sh.vcm.sensiblelogging.util#createMeta(kotlin.Int){} + 1 ",
            "2   App   0x3   kfun:sh.vcm.sensiblelogging.Logger#d(kotlin.String){} + 1 ",
            "3   App   0x4   kfun:sh.vcm.sensiblelogging.Logger#d\$default(kotlin.String){} + 1 ",
            "4   App   0x5   kfun:com.example.ServerList#refresh(){} + 1 ",
            "5   App   0x6   kfun:com.example.Screen#show(){} + 1 "
        )

        // THEN
        assertEquals(frames[4], callSiteFrame(frames, 0))
        assertEquals(frames[5], callSiteFrame(frames, 1))
        assertNull(callSiteFrame(frames.copyOfRange(4, 6), 0))
    }
}
