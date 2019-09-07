package com.github.doyaaaaaken.kotlincsv.client

import java.io.BufferedReader
import java.io.Closeable

/**
 * buffered reader which can read line with line terminator
 */
internal class BufferedLineReader(
        private val br: BufferedReader
) : Closeable {

    fun readLineWithTerminator(): String? {
        val sb = StringBuilder()
        do {
            val c = br.read()

            if (c == -1) {
                if (sb.isEmpty()) {
                    return null
                } else {
                    break
                }
            }
            val ch = c.toChar()
            sb.append(ch)

            if (ch == '\n' || ch == '\u2028' || ch == '\u2029' || ch == '\u0085') {
                break
            }

            if (ch == '\r') {
                br.mark(1)
                val c2 = br.read()
                if (c2 == -1) {
                    break
                } else if (c2.toChar() == '\n') {
                    sb.append('\n')
                } else {
                    br.reset()
                }

                break
            }
        } while (true)
        return sb.toString()
    }

    override fun close() {
        br.close()
    }
}
