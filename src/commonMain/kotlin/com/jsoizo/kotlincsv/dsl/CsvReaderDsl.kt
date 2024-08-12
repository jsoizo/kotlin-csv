package com.jsoizo.kotlincsv.dsl

import com.jsoizo.kotlincsv.client.CsvReader
import com.jsoizo.kotlincsv.dsl.context.CsvReaderContext

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
 *  reader.read("a,b,c\nd,e,f))
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
