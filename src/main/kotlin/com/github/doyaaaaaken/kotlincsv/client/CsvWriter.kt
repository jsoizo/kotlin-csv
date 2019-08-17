package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvWriterContext
import java.io.*

class CsvWriter(
        private val ctx: CsvWriterContext = CsvWriterContext()
) : ICsvWriterContext by ctx {

    fun writeTo(targetFile: File, append: Boolean = false, write: CsvFileWriter.() -> Unit) {
        val fos = FileOutputStream(targetFile, append)
        writeTo(fos, write)
    }

    fun writeTo(ops: OutputStream, write: CsvFileWriter.() -> Unit) {
        val osw = OutputStreamWriter(ops, ctx.charset)
        val writer = CsvFileWriter(ctx, PrintWriter(osw))
        writer.use { it.write() }
    }
}

class CsvFileWriter(
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
