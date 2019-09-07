package com.github.doyaaaaaken.kotlincsv.dsl

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext

/**
 * DSL Method which provides `CsvReader`
 *
 * @return CsvReader
 *
 * Usage example:
 *
 * 1. Use default setting
 *  <pre>
 *  val reader: CsvReader = csvReader()
 *  reader.read(File("test.csv"))
 *  </pre>
 *
 * 2. Customize Setting
 *  <pre>
 *  val reader: CsvReader = csvReader {
 *      delimiter = '\t'
 *      //...
 *  }
 *  </pre>
 *
 * @see CsvReaderContext
 * @see CsvReader
 *
 * @author doyaaaaaken
 */
fun csvReader(init: CsvReaderContext.() -> Unit = {}): CsvReader {
    val context = CsvReaderContext().apply(init)
    return CsvReader(context)
}
