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

import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

fun ByteArrayOutputStream.verifyContent(filename: String) {
    val resource = ClassLoader.getSystemClassLoader().getResource(filename) ?: throw Exception("File $filename not found")
    val expectedLines = Files.readAllLines(Path.of(resource.path))
    val actualLines = toString().split(System.lineSeparator())
    assertContentEquals(expectedLines, actualLines)
}

fun ByteArrayOutputStream.verifyLineCount(expectedLineCount: Int) {
    val actualLines = toString().split(System.lineSeparator()).size
    assertEquals(expectedLineCount, actualLines)
}
