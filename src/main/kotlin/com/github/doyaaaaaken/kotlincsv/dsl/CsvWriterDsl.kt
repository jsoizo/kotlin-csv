package com.github.doyaaaaaken.kotlincsv.dsl

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext

fun csvWriter(init: CsvWriterContext.() -> Unit = {}): CsvWriter {
    val context = CsvWriterContext().apply(init)
    return CsvWriter(context)
}
