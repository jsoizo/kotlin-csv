package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvWriterContext
import java.io.*

class CsvWriter(
        private val ctx: CsvWriterContext = CsvWriterContext()
) : ICsvWriterContext by ctx {

    fun writeTo(targetFileName: String, append: Boolean = false): CsvFileWriter {
        val targetFile = File(targetFileName)
        return writeTo(targetFile, append)
    }

    fun writeTo(targetFile: File, append: Boolean = false): CsvFileWriter {
        val fos = FileOutputStream(targetFile, append)
        return writeTo(fos)
    }

    fun writeTo(ops: OutputStream): CsvFileWriter {
        val osw = OutputStreamWriter(ops, ctx.charset)
        return CsvFileWriter(ctx, PrintWriter(osw))
    }

    fun writeTo(targetFileName: String, append: Boolean = false, write: CsvFileWriter.() -> Unit) {
        writeTo(targetFileName, append).write()
    }

    fun writeTo(targetFile: File, append: Boolean = false, write: CsvFileWriter.() -> Unit) {
        writeTo(targetFile, append).write()
    }

    fun writeTo(ops: OutputStream, write: CsvFileWriter.() -> Unit) {
        writeTo(ops).use { it.write() }
    }
}
