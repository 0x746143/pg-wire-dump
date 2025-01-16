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
package x746143.pgwiredump.test

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import x746143.pgwiredump.JdbcClient
import x746143.pgwiredump.PgTsharkContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class PgContainerLifecycle {
    protected lateinit var container: PgTsharkContainer

    @BeforeAll
    fun setUp() {
        container = PgTsharkContainer()
        container.start()
    }

    @AfterAll
    fun tearDown() {
        container.stop()
    }

    fun dumpJdbcPgTraffic(filename: String, cache: Boolean = false, block: JdbcClient.() -> Unit) {
        container.jdbcClient.connect(cache).use {
            container.dumpPgTraffic(filename) {
                it.block()
            }
        }
    }
}