package com.github.doyaaaaaken.kotlincsv.client

interface ICsvFileWriter {
    fun writeRow(row: List<Any?>)

    fun writeAll(rows: List<List<Any?>>)

    fun writeAll(rows: Sequence<List<Any?>>)

    fun flush()

    fun close()
}
