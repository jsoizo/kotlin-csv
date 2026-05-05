package com.jsoizo.kotlincsv.reader.internal

import java.io.FilterInputStream
import java.io.InputStream

/**
 * [InputStream] wrapper that records [close] calls and total bytes read so
 * tests can assert the caller-owned-stream contract and the lazy-pull
 * behaviour of JVM read overloads. Mirrors the role of `FakeRawSource` in
 * `commonTest` for an actual JVM stream object.
 */
internal class CountingInputStream(stream: InputStream) : FilterInputStream(stream) {
    var closeCount: Int = 0
        private set
    var bytesRead: Long = 0L
        private set

    override fun read(): Int {
        val ch = super.read()
        if (ch != -1) bytesRead++
        return ch
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val n = super.read(b, off, len)
        if (n > 0) bytesRead += n
        return n
    }

    override fun close() {
        closeCount++
        super.close()
    }
}
