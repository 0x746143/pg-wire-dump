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

import x746143.pgwiredump.test.PgContainerLifecycle
import kotlin.test.AfterTest
import kotlin.test.Test

class DmlDump : PgContainerLifecycle() {

    @AfterTest
    fun tearDownEach() {
        if (container.isRunning) {
            container.jdbcClient.connect().use {
                it.executeUpdate("truncate table modifiable_table restart identity")
            }
        }
    }

    @Test
    fun dumpInsertUpdateDelete() {
        dumpJdbcPgTraffic("insert-update-delete.txt") {
            executeUpdate(
                "insert into modifiable_table (varchar_col) values (?)",
                "varchar_value_1"
            )
            executeUpdate(
                "update modifiable_table set varchar_col = ? where varchar_col = ?",
                "varchar_value_2", "varchar_value_1"
            )
            executeUpdate(
                "delete from modifiable_table where varchar_col = ?",
                "varchar_value_2"
            )
        }
    }

    @Test
    fun dumpInsertWithReturning() {
        dumpJdbcPgTraffic("insert-with-returning.txt") {
            executeUpdateWithReturning(
                "insert into modifiable_table (varchar_col) values (?)",
                "varchar_value_1"
            )
        }
    }
}