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

import sh.vcm.sensiblelogging.Meta

private val SYMBOL = Regex("""kfun:(.+?) \+ \d+""")
private val SOURCE_LOCATION = Regex("""\(([^()]+):(\d+):\d+\)\s*$""")
private const val LOGGER = "sh.vcm.sensiblelogging.Logger"

/**
 * Finds the frame that called [sh.vcm.sensiblelogging.Logger] in a Kotlin/Native stack trace.
 * [extraFrames] skips that many more frames, for callers that wrap the logger.
 */
internal fun callSiteFrame(frames: Array<String>, extraFrames: Int): String? {
    val loggerFrame = frames.indexOfLast { frame ->
        val symbol = SYMBOL.find(frame)?.groupValues?.get(1).orEmpty()
        symbol.startsWith("$LOGGER#") || symbol.startsWith("$LOGGER.")
    }
    if (loggerFrame < 0) return null
    return frames.getOrNull(loggerFrame + 1 + extraFrames)
}

/**
 * Reads a Kotlin/Native stack frame such as
 * `kfun:com.example.Foo#bar(){} + 52 (/src/com/example/Foo.kt:12:5)`.
 * The source location is only there when the binary was built with debug info.
 */
internal fun metaFromStackFrame(frame: String, threadName: String): Meta? {
    val symbol = SYMBOL.find(frame)?.groupValues?.get(1) ?: return null
    val name = symbol.substringBefore('(')
    val owner = name.substringBefore('#')
    val member = name.substringAfter('#', missingDelimiterValue = "")
    // Private members are named `Owner.member#internal`
    val (declaringName, functionName) = if (member == "internal") {
        owner.substringBeforeLast('.') to owner.substringAfterLast('.')
    } else {
        owner to member
    }
    val location = SOURCE_LOCATION.find(frame)?.groupValues
    val fileName = location?.get(1)?.substringAfterLast('/') ?: NOT_AVAILABLE
    // Top level functions are owned by their package, which starts with a lower case letter
    val isTopLevel = declaringName.substringAfterLast('.').firstOrNull()?.isLowerCase() == true
    val className = if (isTopLevel && fileName != NOT_AVAILABLE) {
        "$declaringName.${fileName.substringBeforeLast('.')}Kt"
    } else {
        declaringName
    }
    return Meta(
        className = className,
        simpleClassName = className.substringAfterLast('.'),
        functionName = functionName.substringBefore("__at__"),
        lineNumber = location?.get(2)?.toIntOrNull() ?: 0,
        threadName = threadName,
        fileName = fileName
    )
}
