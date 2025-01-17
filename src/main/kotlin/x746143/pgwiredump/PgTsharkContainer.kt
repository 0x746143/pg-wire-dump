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

import com.github.dockerjava.api.command.InspectContainerResponse
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.shaded.org.apache.commons.io.output.TeeOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PgTsharkContainer(var hostPort: Int = 0) : GenericContainer<PgTsharkContainer>(
    ImageFromDockerfile("pg-wire-dump", false).withDockerfile(Path.of("docker/Dockerfile"))
) {
    private val pgPort = 5432
    private val tsharkParser = TsharkParser()
    private val executor = Executors.newSingleThreadExecutor()

    private var username = "postgres"
    private var password = "postgres"
    private var dbname = "postgres"
    var jdbcUrl = ""

    val jdbcClient get() = JdbcClient(jdbcUrl, username, password)

    override fun configure() {
        withEnv("POSTGRES_USER", username)
        withEnv("POSTGRES_PASSWORD", password)
        withEnv("POSTGRES_DB", dbname)
        withExposedPorts(pgPort)
        if (hostPort != 0) {
            withCreateContainerCmdModifier {
                it.withHostConfig(
                    HostConfig().withPortBindings(PortBinding(Ports.Binding.bindPort(hostPort), ExposedPort(pgPort)))
                )
            }
        }
        withCommand("postgres", "-c", "fsync=off")
        withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("PostgreSQL")));
        waitStrategy = Wait.forListeningPort()
    }

    override fun containerIsStarted(containerInfo: InspectContainerResponse?) {
        hostPort = firstMappedPort
        jdbcUrl = "jdbc:postgresql://$host:$hostPort/$dbname"
        logger().info("Container is started (JDBC URL: $jdbcUrl)")
        println()
    }

    override fun containerIsStopping(containerInfo: InspectContainerResponse?) {
        executor.shutdownNow()
        executor.awaitTermination(1, TimeUnit.SECONDS)
    }

    private fun runTshark(block: (Process) -> Unit) {
        val process = ProcessBuilder(
            "docker", "exec", containerName, "tshark",
            "-l", "-i", "any", "-f", "tcp port 5432", "-Y", "pgsql", "-T", "fields",
            "-e", "pgsql.frontend", "-e", "pgsql.type", "-e", "tcp.pdu.size", "-e", "tcp.payload"
        ).start()
        try {
            block(process)
        } finally {
            process.destroyForcibly().waitFor(1, TimeUnit.SECONDS)
        }
    }

    fun monitorPgTraffic() {
        runTshark { process ->
            parseAndWriteMessages(process, System.out)
        }
    }

    fun dumpPgTraffic(filename: String, block: () -> Unit) {
        FileOutputStream("dumps/$filename").use {
            dumpPgTraffic(it, block)
        }
    }

    fun dumpPgTraffic(output: OutputStream, block: () -> Unit) {
        runTshark { process ->
            executor.execute {
                val teeWriter = TeeOutputStream(System.out, output)
                parseAndWriteMessages(process, teeWriter)
            }
            Thread.sleep(1000)
            block()
            Thread.sleep(1000)
        }
    }

    private fun parseAndWriteMessages(process: Process, output: OutputStream) {
        val printWriter = PrintWriter(output.bufferedWriter())
        process.inputReader().useLines { sequence ->
            sequence.forEach { line ->
                tsharkParser.parseLine(line).forEach { msg ->
                    printWriter.println(msg)
                }
                printWriter.println()
                printWriter.flush()
            }
        }
    }
}
