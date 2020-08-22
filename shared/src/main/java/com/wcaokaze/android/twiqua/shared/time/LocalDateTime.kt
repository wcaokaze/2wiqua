/*
 * Copyright 2020 wcaokaze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wcaokaze.android.twiqua.shared.time

import java.io.Serializable
import java.util.*

/**
 * [java.util.Date]とほぼ同じ。タイムゾーンの概念を持たず
 * 常に実行環境のデフォルトタイムゾーンの時刻を表すものとします
 */
inline class LocalDateTime(val timeMillis: Long) : Serializable, Comparable<LocalDateTime> {
    constructor(date: Date) : this(date.time)

    override fun compareTo(other: LocalDateTime) = timeMillis.compareTo(other.timeMillis)
}
