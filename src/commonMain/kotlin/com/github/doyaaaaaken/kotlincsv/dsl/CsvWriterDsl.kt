package com.github.doyaaaaaken.kotlincsv.dsl

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext

/**
 * DSL Method which provides `CsvWriter`
 *
 * @return CsvWriter
 *
 * Usage example:
 *
 * 1. Use default setting
 *  <pre>
 *  val writer: CsvWriter = csvWriter()
 *  </pre>
 *
 * 2. Customize Setting
 *  <pre>
 *  val writer: CsvWriter = csvWriter {
 *      charset = Charsets.ISO_8859_1
 *      //...
 *  }
 *  </pre>
 *
 * @see CsvWriterContext
 * @see CsvWriter
 *
 * @author doyaaaaaken
 */
fun csvWriter(init: CsvWriterContext.() -> Unit = {}): CsvWriter {
    val context = CsvWriterContext().apply(init)
    return CsvWriter(context)
}
