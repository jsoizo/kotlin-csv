package com.jsoizo.kotlincsv.reader.internal

import kotlinx.io.Buffer
import kotlinx.io.RawSource

/**
 * In-memory [RawSource] backed by a fixed [ByteArray]. Tracks how many times
 * [close] is called so tests can assert resource lifecycle behaviour.
 */
internal class FakeRawSource(private val bytes: ByteArray) : RawSource {
    private var position: Int = 0
    var closeCount: Int = 0
        private set

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        check(byteCount >= 0L) { "byteCount must not be negative: $byteCount" }
        if (position >= bytes.size) return -1L
        val remaining = bytes.size - position
        val toRead = minOf(byteCount, remaining.toLong()).toInt()
        sink.write(bytes, position, position + toRead)
        position += toRead
        return toRead.toLong()
    }

    override fun close() {
        closeCount++
    }
}
