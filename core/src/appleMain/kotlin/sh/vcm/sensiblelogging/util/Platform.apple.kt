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

internal actual fun createMeta(stackDepth: Int): Meta = Meta(
    className = NOT_AVAILABLE,
    simpleClassName = NOT_AVAILABLE,
    functionName = NOT_AVAILABLE,
    lineNumber = 0,
    threadName = currentThreadName(),
    fileName = NOT_AVAILABLE
)

internal fun currentThreadName(): String {
    val thread = NSThread.currentThread
    return thread.name?.takeIf { it.isNotEmpty() } ?: if (thread.isMainThread) "main" else NOT_AVAILABLE
}

private const val NOT_AVAILABLE = "n/a"
