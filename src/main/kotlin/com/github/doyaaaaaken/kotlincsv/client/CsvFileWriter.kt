package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.WriteQuoteMode
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

    fun writeAll(rows: Sequence<List<Any?>>) {
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
        val rowStr = row.map { field ->
            if (field == null) {
                ctx.nullCode
            } else {
                attachQuote(field.toString())
            }
        }.joinToString(ctx.delimiter.toString())
        writer.print(rowStr)
        writer.print(ctx.lineTerminator)
    }

    private fun attachQuote(field: String): String {
        return when (ctx.quote.quoteMode) {
            WriteQuoteMode.ALL -> "\"$field\""
            WriteQuoteMode.NONE -> field
            WriteQuoteMode.MINIMAL -> {
                val quoteNeededChars = listOf('\r', '\n', ctx.quote.quoteChar, ctx.delimiter)
                val shouldQuote = field.any { ch -> quoteNeededChars.contains(ch) }
                if (shouldQuote) "\"$field\"" else field
            }
        }
    }
}
