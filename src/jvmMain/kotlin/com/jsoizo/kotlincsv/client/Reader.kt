package com.jsoizo.kotlincsv.client

import java.io.InputStream
import java.nio.charset.Charset

class ReaderJvmImpl(private val reader: java.io.BufferedReader) : Reader {
    override fun read(): Int {
        return reader.read()
    }

    override fun mark(readAheadLimit: Int) {
        reader.mark(readAheadLimit)
    }

    override fun reset() {
        reader.reset()
    }

    override fun close() {
        reader.close()
    }
}

internal fun InputStream.bufferedReader(charset: Charset = Charsets.UTF_8): Reader =
    ReaderJvmImpl(reader(charset).buffered())
