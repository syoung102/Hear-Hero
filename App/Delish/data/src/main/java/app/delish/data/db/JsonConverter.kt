/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.delish.data.db

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import javax.inject.Inject

internal class JsonConverter @Inject constructor(private val moshi: Moshi) {

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> toJson(clazz: T): String {
        return moshi.adapter<T>().toJson(clazz)
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> fromJson(json: String): T? {
        return moshi.adapter<T>().fromJson(json)
    }
}
