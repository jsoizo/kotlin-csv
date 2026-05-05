package com.jsoizo.kotlincsv.reader.internal

import java.io.FilterInputStream
import java.io.InputStream

/**
 * [InputStream] wrapper that counts how many times [close] is called so
 * tests can assert the caller-owned-stream contract on JVM read overloads.
 * Mirrors the role of `FakeRawSource` in `commonTest` for an actual JVM
 * stream object.
 */
internal class CountingInputStream(stream: InputStream) : FilterInputStream(stream) {
    var closeCount: Int = 0
        private set

    override fun close() {
        closeCount++
        super.close()
    }
}
