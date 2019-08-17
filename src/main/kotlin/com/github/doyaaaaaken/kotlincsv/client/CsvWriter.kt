package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvWriterContext
import java.io.*

class CsvWriter(
        private val ctx: CsvWriterContext = CsvWriterContext()
) : ICsvWriterContext by ctx {

    fun writeTo(targetFile: File, append: Boolean = true, write: CsvFileWriter.() -> Unit) {
        val fos = FileOutputStream(targetFile, append)
        writeTo(fos, write)
    }

    fun writeTo(ops: OutputStream, write: CsvFileWriter.() -> Unit) {
        val osw = OutputStreamWriter(ops, ctx.charset)
        val writer = CsvFileWriter(PrintWriter(osw))
        writer.write()
    }
}

class CsvFileWriter(private val writer: PrintWriter) : Closeable, Flushable {
    fun writeRow(row: List<Any?>) {
        writer.write(row.map { it.toString() }.joinToString(","))
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
