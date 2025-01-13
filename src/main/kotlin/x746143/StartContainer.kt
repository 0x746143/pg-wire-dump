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
package x746143

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path

fun main() {
    val pgPort = 5432
    val hostPort = 54329 // host port is random if useFixedHostPort = false
    val useFixedHostPort = true
    val logger = LoggerFactory.getLogger("pg-wire-dump")
    val image = ImageFromDockerfile("pg-wire-dump", false)
        .withDockerfile(Path.of("docker/Dockerfile"))
    val container = GenericContainer(image).apply {
        withEnv("POSTGRES_PASSWORD", "postgres")
        withExposedPorts(pgPort)
        if (useFixedHostPort) {
            withCreateContainerCmdModifier {
                it.withHostConfig(
                    HostConfig().withPortBindings(PortBinding(Ports.Binding.bindPort(hostPort), ExposedPort(pgPort)))
                )
            }
        }
        start()
        waitingFor(Wait.forListeningPorts(firstMappedPort))
        logger.info("Container is started (JDBC URL: jdbc:postgresql://$host:$firstMappedPort/postgres)")
    }
    val processBuilder = ProcessBuilder(
        "docker", "exec", container.containerName, "tshark",
        "-i", "any", "-f", "tcp port 5432", "-Y", "pgsql", "-T", "fields",
        "-e", "pgsql.frontend", "-e", "pgsql.type", "-e", "tcp.pdu.size", "-e", "tcp.payload"
    )
    val process = processBuilder.start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    generateSequence {
        reader.readLine()
    }.forEach(::printMessages)
}

fun printMessages(packet: String) {
    val fields = packet.split("\t")
    val frontends = fields[0].split(",")
    val commandNames = fields[1].split(",")
    val pduSizes = fields[2].split(",")
    val tcpPayload = fields[3]
    var pduStartIndex = 0
    repeat(frontends.size) { i ->
        val f = if (frontends[i] == "1") "F" else "B"
        val commandName = commandNames[i]
        val pduSize = pduSizes[i].toInt()
        val pduEndIndex = pduStartIndex + pduSize * 2
        val hex = tcpPayload.substring(pduStartIndex, pduEndIndex)
        pduStartIndex = pduEndIndex
        val message = "$f - $commandName: ${hex.toMixedHex()}"
        println(message)
    }
    println()
}

fun String.toMixedHex(): String {
    val buffer = StringBuffer()
    var hexCount = 0
    var i = 0
    while (i < length) {
        val isMessageLength = i in 2..9
        val ch1 = this[i++]
        val ch2 = this[i++]
        val highNibble = ch1.hexToInt(i) shl 4
        val lowNibble = ch2.hexToInt(i)
        val code = highNibble or lowNibble
        if (isMessageLength || code < 33 || code > 126) {
            if (hexCount == 0) {
                buffer.append("[")
            }
            buffer.append(ch1).append(ch2)
            hexCount++
        } else {
            if (hexCount > 0) {
                hexCount = 0
                buffer.append("]")
            }
            buffer.append(code.toChar())
        }
    }
    if (hexCount > 0) {
        buffer.append("]")
    }
    return buffer.toString()
}

fun Char.hexToInt(i: Int): Int {
    return when (this) {
        in '0'..'9' -> this - '0'
        in 'a'..'f' -> this - 'a' + 10
        else -> throw IllegalArgumentException("Invalid hex character $this at index $i")
    }
}