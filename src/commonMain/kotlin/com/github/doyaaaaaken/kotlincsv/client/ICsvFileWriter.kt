package com.github.doyaaaaaken.kotlincsv.client

@Deprecated(
    message = "Replaced by com.jsoizo.kotlincsv.writer.CsvWriter in v2.0, " +
            "which encodes lazily into Sequence<Char> instead of streaming to a file handle.",
    level = DeprecationLevel.WARNING
)
interface ICsvFileWriter {
    fun writeRow(row: List<Any?>)

    fun writeRow(vararg entry: Any?)

    fun writeRows(rows: List<List<Any?>>)

    fun writeRows(rows: Sequence<List<Any?>>)

    fun flush()

    fun close()
}
