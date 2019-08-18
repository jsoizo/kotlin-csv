package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvWriterContext
import java.io.*

/**
 * CSV Writer class, which decides where to write and returns CsvFileWriter class (class for controlling File I/O).
 *
 * @see CsvFileWriter
 *
 * @author doyaaaaaken
 */
class CsvWriter(
        private val ctx: CsvWriterContext = CsvWriterContext()
) : ICsvWriterContext by ctx {

    fun writeTo(targetFileName: String, append: Boolean = false, write: CsvFileWriter.() -> Unit) {
        val targetFile = File(targetFileName)
        writeTo(targetFile, append, write)
    }

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
