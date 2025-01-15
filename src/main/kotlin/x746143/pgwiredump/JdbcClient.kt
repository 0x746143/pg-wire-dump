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

import java.sql.Connection
import java.sql.DriverManager

class JdbcClient(
    private val url: String,
    private val user: String,
    private val password: String
) : AutoCloseable {
    private var connection: Connection? = null

    fun connect(): JdbcClient {
        if (connection != null) {
            println("The connection has already been established before")
        } else {
            connection = DriverManager.getConnection(url, user, password)
        }
        return this
    }

    fun simpleQuery(sql: String): Int {
        var count = 0
        connection?.createStatement()?.use {
            val resultSet = it.executeQuery(sql)
            while (resultSet.next()) {
                count++
            }
        }
        return count
    }

    fun preparedQuery(sql: String, vararg params: Any): Int {
        var count = 0
        connection?.prepareStatement(sql)?.use {
            params.forEachIndexed { index, value ->
                when (value) {
                    is Int -> it.setInt(index + 1, value)
                    is String -> it.setString(index + 1, value)
                    else -> throw Exception("Unsupported type: ${value::class.qualifiedName}")
                }
            }
            val resultSet = it.executeQuery()
            while (resultSet.next()) {
                count++
            }
        }
        return count
    }

    override fun close() {
        connection?.close()
    }

}