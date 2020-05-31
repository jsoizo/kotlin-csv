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
) : ICsvFileWriter, Closeable, Flushable {

    /**
     * state handling to write terminator for next line
     */
    private val stateHandler = CsvWriterStateHandler()


    /**
     * write one row
     */
    override fun writeRow(row: List<Any?>) {
        willWritePreTerminator()
        writeNext(row)
        willWriteEndTerminator()

        if (writer.checkError()) {
            throw IOException("Failed to write")
        }
    }

    /**
     * write rows
     */
    override fun writeRows(rows: List<List<Any?>>) {
        willWritePreTerminator()
        rows.forEachIndexed{ index, list ->
            writeNext(list)
            if (index < rows.size - 1) {
                writeTerminator()
            }
        }
        willWriteEndTerminator()
        if (writer.checkError()) {
            throw IOException("Failed to write")
        }
    }

    /**
     * write rows from Sequence
     */
    override fun writeRows(rows: Sequence<List<Any?>>) {
        willWritePreTerminator()

        val itr = rows.iterator()
        while(itr.hasNext()) {
            writeNext(itr.next())
            if(itr.hasNext()) writeTerminator()
        }

        willWriteEndTerminator()
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
    }

    private fun willWriteEndTerminator() {
        if (ctx.outputLastLineTerminator) {
            writeTerminator()
            stateHandler.wroteLineEndTerminatorState()
        } else {
            stateHandler.notWroteLineEndTerminatorState()
        }
    }

    /**
     * Will write terminator if and only if
     *  1. has wrote first line
     *  2. state is set to has not wrote last line terminator
     */
    private fun willWritePreTerminator() {
        if (stateHandler.hasWroteFirstLine() && !stateHandler.hasWroteLineEndTerminator()) {
            writeTerminator()
        }
    }

    /**
     * write terminator for next line
     */
    private fun writeTerminator() {
        writer.print(ctx.lineTerminator)
        stateHandler.wroteLineEndTerminatorState()
    }

    private fun attachQuote(field: String): String {
        return when (ctx.quote.mode) {
            WriteQuoteMode.ALL -> "\"$field\""
            WriteQuoteMode.CANONICAL -> {
                val quoteNeededChars = setOf('\r', '\n', ctx.quote.char, ctx.delimiter)
                val shouldQuote = field.any { ch -> quoteNeededChars.contains(ch) }

                buildString {
                    if (shouldQuote) append(ctx.quote.char)
                    field.forEach { ch ->
                        if (ch == ctx.quote.char) {
                            append(ctx.quote.char)
                        }
                        append(ch)
                    }
                    if (shouldQuote) append(ctx.quote.char)
                }
            }
        }
    }
}
