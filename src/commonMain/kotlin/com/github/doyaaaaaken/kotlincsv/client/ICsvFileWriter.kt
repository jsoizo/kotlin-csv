package com.github.doyaaaaaken.kotlincsv.client

interface ICsvFileWriter {
    fun writeRow(row: List<Any?>)

    fun writeRows(rows: List<List<Any?>>)

    fun writeRows(rows: Sequence<List<Any?>>)

    fun flush()

    fun close()
}
