package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.ICsvWriterContext
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.io.OutputStream

class CsvWriter(
        ctx: CsvWriterContext = CsvWriterContext()
) : ICsvWriterContext by ctx {

    fun writeTo(targetFile: File, append: Boolean = true, write: CsvFileWriter.() -> Unit) {
        //TODO: file open and call write method
        TODO()
    }

    fun writeTo(osp: OutputStream, append: Boolean = true, write: CsvFileWriter.() -> Unit) {
        //TODO: file open and call write method
        TODO()
    }
}

class CsvFileWriter() : Closeable, Flushable {
    fun writeRow(row: List<Any?>) {
        TODO()
    }

    fun writeAll(rows: List<List<Any?>>) {
        TODO()
    }

    override fun flush() {
        TODO()
    }

    override fun close() {
        TODO()
    }
}
