@file:Suppress("DEPRECATION")
package com.github.doyaaaaaken.kotlincsv.dsl

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.util.V2_MIGRATION_GUIDE_URL

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
@Deprecated(
    message = "Migrate to com.jsoizo.kotlincsv.csvWriter. " +
            "The DSL block receiver changes from CsvWriterContext to CsvWriterConfigBuilder, " +
            "and properties such as `delimiter` and `quoteChar` move under `dialect = CsvDialect(...)`; " +
            "`quote { mode = ... }` moves to top-level `quoteMode = WriteQuoteMode.X`. " +
            "Rewrite the block body manually; IDE Quick Fix cannot translate it. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
@Suppress("DEPRECATION")
fun csvWriter(init: CsvWriterContext.() -> Unit = {}): CsvWriter {
    val context = CsvWriterContext().apply(init)
    return CsvWriter(context)
}
