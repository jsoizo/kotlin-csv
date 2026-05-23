package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.util.V2_MIGRATION_GUIDE_URL

@Deprecated(
    message = "Replaced by com.jsoizo.kotlincsv.writer.CsvWriter in v2.0, " +
            "which encodes lazily into Sequence<Char> instead of streaming to a file handle. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
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
