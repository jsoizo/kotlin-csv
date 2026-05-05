package com.jsoizo.kotlincsv.writer.internal

import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.readByteArray

/**
 * In-memory [RawSink] that accumulates written bytes for inspection and tracks
 * how many times [close] is invoked.
 */
internal class FakeRawSink : RawSink {
    private val store = Buffer()
    var closeCount: Int = 0
        private set
    var flushCount: Int = 0
        private set

    override fun write(source: Buffer, byteCount: Long) {
        source.readTo(store, byteCount)
    }

    override fun flush() {
        flushCount++
    }

    override fun close() {
        closeCount++
    }

    /** Snapshot the bytes received so far without disturbing the sink. */
    fun snapshot(): ByteArray = store.copy().readByteArray()
}
