@file:Suppress("DEPRECATION")
package com.github.doyaaaaaken.kotlincsv.dsl

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.util.V2_MIGRATION_GUIDE_URL

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
@Deprecated(
    message = "Migrate to com.jsoizo.kotlincsv.csvReader. " +
            "The DSL block receiver changes from CsvReaderContext to CsvReaderConfigBuilder, " +
            "and properties such as `delimiter` and `quoteChar` move under `dialect = CsvDialect(...)`. " +
            "Rewrite the block body manually; IDE Quick Fix cannot translate it. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
fun csvReader(init: CsvReaderContext.() -> Unit = fun CsvReaderContext.() {

}
): CsvReader {
    val context = CsvReaderContext().apply(init)
    return CsvReader(context)
}
