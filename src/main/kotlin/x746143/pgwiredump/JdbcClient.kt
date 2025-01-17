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
import java.sql.PreparedStatement
import java.sql.Statement

class JdbcClient(
    private val url: String,
    private val user: String,
    private val password: String
) : AutoCloseable {
    private var connection: Connection? = null

    fun connect(cache: Boolean = false): JdbcClient {
        if (connection != null) {
            println("The connection has already been established before")
        } else {
            val cacheParam = if (cache) "?prepareThreshold=1" else ""
            connection = DriverManager.getConnection(url + cacheParam, user, password)
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

    fun preparedQuery(sql: String, vararg params: Any) {
        connection?.prepareStatement(sql)?.use {
            it.executeQuery(*params)
        }
    }

    fun executeUpdate(sql: String, vararg params: Any) {
        connection?.prepareStatement(sql)?.use {
            it.executeUpdate(*params)
        }
    }

    fun executeUpdateWithReturning(sql: String, vararg params: Any) {
        connection?.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)?.use {
            it.executeUpdate(*params)
        }
    }

    fun preparedStatement(sql: String, block: PreparedStatement.() -> Unit) {
        connection?.prepareStatement(sql)?.use {
            it.block()
        }
    }

    override fun close() {
        connection?.close()
    }
}

fun PreparedStatement.executeQuery(vararg params: Any) {
    setParams(*params)
    executeQuery()
}

fun PreparedStatement.executeUpdate(vararg params: Any) {
    setParams(*params)
    executeUpdate()
}

private fun PreparedStatement.setParams(vararg params: Any) {
    params.forEachIndexed { index, value ->
        when (value) {
            is Int -> setInt(index + 1, value)
            is String -> setString(index + 1, value)
            else -> throw Exception("Unsupported type: ${value::class.qualifiedName}")
        }
    }
}