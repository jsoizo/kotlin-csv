package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import java.io.Closeable
import java.io.Flushable
import java.io.IOException
import java.io.PrintWriter

/**
 * CSV Writer class, which controls file I/O flow.
 *
 * @author doyaaaaaken
 */
class CsvFileWriter internal constructor(
        private val ctx: CsvWriterContext,
        private val writer: PrintWriter
) : Closeable, Flushable {

    fun writeRow(row: List<Any?>) {
        writeNext(row)
        if (writer.checkError()) {
            throw IOException("Failed to write")
        }
    }

    fun writeAll(rows: List<List<Any?>>) {
        rows.forEach { writeNext(it) }
        if (writer.checkError()) {
            throw IOException("Failed to write")
        }
    }

    override fun flush() {
        writer.flush()
    }

    override fun close() {
        writer.close()
    }

    private fun writeNext(row: List<Any?>) {
        writer.print(row.map { it.toString() }.joinToString(ctx.delimiter.toString()))
        writer.print(ctx.lineTerminator)
    }
}
