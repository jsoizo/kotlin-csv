package com.github.doyaaaaaken.kotlincsv.dsl

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext

fun csvReader(init: CsvReaderContext.() -> Unit = {}): CsvReader {
    val context = CsvReaderContext().apply(init)
    return CsvReader(context)
}
