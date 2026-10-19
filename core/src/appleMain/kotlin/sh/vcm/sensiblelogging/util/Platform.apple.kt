/*
 * Copyright 2022 Volvo Cars Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sh.vcm.sensiblelogging.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSRecursiveLock
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.posix.fflush
import platform.posix.fprintf
import platform.posix.stderr
import sh.vcm.sensiblelogging.Meta
import sh.vcm.sensiblelogging.util.Constants.DEFAULT_STACK_DEPTH
import kotlin.experimental.ExperimentalNativeApi

private val dateFormatter = NSDateFormatter().apply {
    dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    locale = NSLocale(localeIdentifier = "en_US_POSIX")
}

internal actual fun createLock(): Lock = object : Lock {
    private val lock = NSRecursiveLock()

    override fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}

internal actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

internal actual fun formatTimestamp(timestamp: Long): String =
    dateFormatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0))

@OptIn(ExperimentalForeignApi::class)
internal actual fun printToStandardError(message: String) {
    fprintf(stderr, "%s\n", message)
    fflush(stderr)
}

/**
 * Reads the call site from the stack trace. Kotlin/Native only has symbol names and source locations in it for debug
 * binaries, so release builds get `n/a` for everything but the thread name.
 */
@OptIn(ExperimentalNativeApi::class)
internal actual fun createMeta(stackDepth: Int): Meta {
    // MetaDataFactory adds one frame for itself on top of the depth Logger asks for
    val extraFrames = stackDepth - 1 - DEFAULT_STACK_DEPTH
    val threadName = currentThreadName()
    return callSiteFrame(Throwable().getStackTrace(), extraFrames)
        ?.let { metaFromStackFrame(it, threadName) }
        ?: unknownMeta(threadName)
}

private fun unknownMeta(threadName: String) = Meta(
    className = NOT_AVAILABLE,
    simpleClassName = NOT_AVAILABLE,
    functionName = NOT_AVAILABLE,
    lineNumber = 0,
    threadName = threadName,
    fileName = NOT_AVAILABLE
)

internal fun currentThreadName(): String {
    val thread = NSThread.currentThread
    return thread.name?.takeIf { it.isNotEmpty() } ?: if (thread.isMainThread) "main" else NOT_AVAILABLE
}

internal const val NOT_AVAILABLE = "n/a"
