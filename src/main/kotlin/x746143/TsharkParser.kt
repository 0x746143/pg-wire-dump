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

class TsharkParser {
    private var prevFragment: String = ""
    val messages = mutableListOf<String>()

    fun parseLine(line: String) {
        val fields = line.split("\t")
        val sourceTypes = fields[0].split(",")
        val messageTypes = fields[1].split(",")
        val pduSizes = fields[2].split(",")
        val tcpPayload = prevFragment + fields[3]
        var pduStartIndex = 0
        repeat(sourceTypes.size) { i ->
            val sourceType = if (sourceTypes[i] == "1") "F" else "B"
            val messageType = messageTypes[i]
            val pduSize = pduSizes[i].toInt()
            val pduEndIndex = pduStartIndex + pduSize * 2
            val hexValue = tcpPayload.substring(pduStartIndex, pduEndIndex)
            pduStartIndex = pduEndIndex
            val message = "$sourceType - $messageType: ${hexValue.toMixedHex()}"
            messages.add(message)
            prevFragment = tcpPayload.substring(pduStartIndex, tcpPayload.length)
        }
    }

    private fun String.toMixedHex(): String {
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

    private fun Char.hexToInt(i: Int): Int {
        return when (this) {
            in '0'..'9' -> this - '0'
            in 'a'..'f' -> this - 'a' + 10
            else -> throw IllegalArgumentException("Invalid hex character $this at index $i")
        }
    }
}