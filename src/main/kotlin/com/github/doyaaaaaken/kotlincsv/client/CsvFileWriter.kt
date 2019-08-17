package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import java.io.Closeable
import java.io.Flushable
import java.io.PrintWriter

class CsvFileWriter internal constructor(
        private val ctx: CsvWriterContext,
        private val writer: PrintWriter
) : Closeable, Flushable {

    fun writeRow(row: List<Any?>) {
        writer.print(row.map { it.toString() }.joinToString(ctx.delimiter.toString()))
        writer.print(ctx.lineTerminator)
    }

    fun writeAll(rows: List<List<Any?>>) {
        TODO()
    }

    override fun flush() {
        writer.flush()
    }

    override fun close() {
        writer.close()
    }
}
