package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvWriterContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

/**
 * CSV Writer class, which decides where to write and returns CsvFileWriter class (class for controlling File I/O).
 *
 * @see CsvFileWriter
 *
 * @author doyaaaaaken
 */
actual class CsvWriter actual constructor(
    private val ctx: CsvWriterContext
) : ICsvWriterContext by ctx {

    actual fun open(targetFileName: String, append: Boolean, write: ICsvFileWriter.() -> Unit) {
        val targetFile = File(targetFileName)
        open(targetFile, append, write)
    }

    actual suspend fun openAsync(targetFileName: String, append: Boolean, write: suspend ICsvFileWriter.() -> Unit) {
        val targetFile = File(targetFileName)
        openAsync(targetFile, append, write)
    }

    fun open(targetFile: File, append: Boolean = false, write: ICsvFileWriter.() -> Unit) {
        val fos = FileOutputStream(targetFile, append)
        open(fos, write)
    }

    suspend fun openAsync(targetFile: File, append: Boolean = false, write: suspend ICsvFileWriter.() -> Unit) =
        withContext(Dispatchers.IO) {
            val fos = FileOutputStream(targetFile, append)
            openAsync(fos, write)
        }

    fun open(ops: OutputStream, write: ICsvFileWriter.() -> Unit) {
        val osw = OutputStreamWriter(ops, ctx.charset)
        val writer = CsvFileWriter(ctx, PrintWriter(osw))
        writer.use { it.write() }
    }

    suspend fun openAsync(ops: OutputStream, write: suspend ICsvFileWriter.() -> Unit) = withContext(Dispatchers.IO) {
        val osw = OutputStreamWriter(ops, ctx.charset)
        val writer = CsvFileWriter(ctx, PrintWriter(osw))
        writer.use { it.write() }
    }

    /**
     * *** ONLY for long-running write case ***
     *
     * Get and use [CsvFileWriter] directly.
     * MUST NOT forget to close [CsvFileWriter] after using it.
     *
     * Use this method If you want to close file writer manually (i.e. streaming scenario).
     */
    @KotlinCsvExperimental
    fun openAndGetRawWriter(targetFileName: String, append: Boolean = false): CsvFileWriter {
        val targetFile = File(targetFileName)
        return openAndGetRawWriter(targetFile, append)
    }

    /**
     * *** ONLY for long-running write case ***
     *
     * Get and use [CsvFileWriter] directly.
     * MUST NOT forget to close [CsvFileWriter] after using it.
     *
     * Use this method If you want to close file writer manually (i.e. streaming scenario).
     */
    @KotlinCsvExperimental
    fun openAndGetRawWriter(targetFile: File, append: Boolean = false): CsvFileWriter {
        val fos = FileOutputStream(targetFile, append)
        return openAndGetRawWriter(fos)
    }

    /**
     * *** ONLY for long-running write case ***
     *
     * Get and use [CsvFileWriter] directly.
     * MUST NOT forget to close [CsvFileWriter] after using it.
     *
     * Use this method If you want to close file writer manually (i.e. streaming scenario).
     */
    @KotlinCsvExperimental
    fun openAndGetRawWriter(ops: OutputStream): CsvFileWriter {
        val osw = OutputStreamWriter(ops, ctx.charset)
        return CsvFileWriter(ctx, PrintWriter(osw))
    }

    /**
     * write all rows on assigned target file
     */
    actual fun writeAll(rows: List<List<Any?>>, targetFileName: String, append: Boolean) {
        open(targetFileName, append) { writeRows(rows) }
    }

    /**
     * write all rows on assigned target file
     */
    actual suspend fun writeAllAsync(rows: List<List<Any?>>, targetFileName: String, append: Boolean) {
        open(targetFileName, append) { writeRows(rows) }
    }

    /**
     * write all rows on assigned target file
     */
    fun writeAll(rows: List<List<Any?>>, targetFile: File, append: Boolean = false) {
        open(targetFile, append) { writeRows(rows) }
    }

    /**
     * write all rows on assigned target file
     */
    suspend fun writeAllAsync(rows: List<List<Any?>>, targetFile: File, append: Boolean = false) {
        open(targetFile, append) { writeRows(rows) }
    }

    /**
     * write all rows on assigned output stream
     */
    fun writeAll(rows: List<List<Any?>>, ops: OutputStream) {
        open(ops) { writeRows(rows) }
    }

    /**
     * write all rows on assigned output stream
     */
    suspend fun writeAllAsync(rows: List<List<Any?>>, ops: OutputStream) {
        open(ops) { writeRows(rows) }
    }
}
