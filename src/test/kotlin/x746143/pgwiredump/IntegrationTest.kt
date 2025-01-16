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

import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationTest : PgContainerLifecycle() {

    @Test
    fun testSimpleQuery() {
        val output = ByteArrayOutputStream()
        container.jdbcClient.connect().use {
            container.dumpPgTraffic(output) {
                val count = it.simpleQuery("select * from basic_types_table")
                assertEquals(2, count)
            }
        }
        output.verifyContent("basic_types_table.txt")
    }

    @Test
    fun testTcpFragmentedDataParsing() {
        val output = ByteArrayOutputStream()
        container.jdbcClient.connect().use {
            container.dumpPgTraffic(output) {
                val count = it.simpleQuery("select * from md5_test_data")
                assertEquals(10000, count)
            }
        }
        output.verifyLineCount(10076)
    }
}