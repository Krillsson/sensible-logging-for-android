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

import platform.Foundation.NSError

class NSErrorException(val error: NSError) : Exception(error.localizedDescription) {
    override fun toString(): String = "NSErrorException(domain=${error.domain}, code=${error.code}): $message"
}

fun NSError.asThrowable(): Throwable = userInfo[KOTLIN_EXCEPTION_KEY] as? Throwable ?: NSErrorException(this)

private const val KOTLIN_EXCEPTION_KEY = "KotlinException"
