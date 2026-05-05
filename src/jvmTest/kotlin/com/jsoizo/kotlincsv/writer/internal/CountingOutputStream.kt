package com.jsoizo.kotlincsv.writer.internal

import java.io.FilterOutputStream
import java.io.OutputStream

/**
 * [OutputStream] wrapper that counts how many times [close] and [flush] are
 * called so tests can assert resource lifecycle behaviour on JVM write
 * overloads. Mirrors the role of `FakeRawSink` in `commonTest` for an actual
 * JVM stream object.
 */
internal class CountingOutputStream(stream: OutputStream) : FilterOutputStream(stream) {
    var closeCount: Int = 0
        private set
    var flushCount: Int = 0
        private set

    override fun close() {
        closeCount++
        super.close()
    }

    override fun flush() {
        flushCount++
        super.flush()
    }
}
