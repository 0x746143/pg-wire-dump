/*
 * Copyright 2025 0x746143
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package x746143.pgwiredump

import kotlin.test.Test

class QueryDump : PgContainerLifecycle() {
    @Test
    fun dumpSimpleSelect() {
        container.jdbcClient.connect().use {
            container.dumpPgTraffic("simple-select.txt") {
                it.simpleQuery("select * from basic_types_table")
            }
        }
    }

    @Test
    fun dumpPreparedSelect() {
        container.jdbcClient.connect().use {
            container.dumpPgTraffic("prepared-select.txt") {
                val sql = """
                    select integer_data_col, varchar_data_col
                    from filter_query_table
                    where integer_filter_col = ? and varchar_filter_col = ?
                    """.trimIndent()
                it.preparedQuery(sql, 1, "varchar_filter_1")
            }
        }
    }
}
