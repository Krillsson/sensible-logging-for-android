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


package sh.vcm.sensiblelogging

import sh.vcm.sensiblelogging.util.currentThreadName
import sh.vcm.sensiblelogging.util.metaFromSourceLocation
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.ShouldRefineInSwift

@OptIn(ExperimentalObjCRefinement::class)
object SwiftLogger {
    @ShouldRefineInSwift
    fun log(
        level: Level,
        message: String,
        category: String,
        parameters: Map<String, String>,
        fileId: String,
        function: String,
        line: Int
    ) {
        Logger.log(
            level = level,
            message = message,
            preFormattedMessage = false,
            meta = metaFromSourceLocation(fileId, function, line, currentThreadName()),
            category = Category(category),
            parameters = parameters
        )
    }
}
