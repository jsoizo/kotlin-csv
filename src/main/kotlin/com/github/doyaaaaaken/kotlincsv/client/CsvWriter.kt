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

    fun open(targetFileName: String, append: Boolean = false, write: CsvFileWriter.() -> Unit) {
        val targetFile = File(targetFileName)
        open(targetFile, append, write)
    }

    fun open(targetFile: File, append: Boolean = false, write: CsvFileWriter.() -> Unit) {
        val fos = FileOutputStream(targetFile, append)
        open(fos, write)
    }

    fun open(ops: OutputStream, write: CsvFileWriter.() -> Unit) {
        val osw = OutputStreamWriter(ops, ctx.charset)
        val writer = CsvFileWriter(ctx, PrintWriter(osw))
        writer.use { it.write() }
    }
}
