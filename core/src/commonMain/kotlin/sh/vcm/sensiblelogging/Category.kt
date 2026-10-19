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

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * A category is a plain string, so it reads as `String` from Swift and Java too.
 */
typealias Category = String

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Deprecated("Category is a String, use the string itself", ReplaceWith("name"))
@Suppress("FunctionName", "NOTHING_TO_INLINE")
inline fun Category(name: String): Category = name

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Deprecated("Category is a String, use the category itself", ReplaceWith("this"))
inline val Category.name: String
    get() = this
