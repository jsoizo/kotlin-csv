package org.doyaaaaaken.kotlincsv.dsl

import org.doyaaaaaken.kotlincsv.client.CsvReader
import org.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext

fun csvReader(init: CsvReaderContext.() -> Unit = {}): CsvReader {
    val context = CsvReaderContext().apply(init)
    return CsvReader(context)
}
