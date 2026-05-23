@file:Suppress("DEPRECATION")
package com.github.doyaaaaaken.kotlincsv.dsl.context

import com.github.doyaaaaaken.kotlincsv.util.CsvDslMarker
import com.github.doyaaaaaken.kotlincsv.util.V2_MIGRATION_GUIDE_URL

/**
 * DSL method for Quote settings on writing csv.
 *
 * @author doyaaaaaken
 */
@Deprecated(
    message = "Replaced in v2.0: quote character moves to com.jsoizo.kotlincsv.CsvDialect.quoteChar, " +
            "and mode moves to com.jsoizo.kotlincsv.writer.CsvWriterConfig.quoteMode. " +
            "Rewrite the writer DSL block manually. See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
@CsvDslMarker
class CsvWriteQuoteContext {
    /**
     * Character to quote each fields
     */
    var char: Char = '"'

    /**
     * Quote mode
     *
     * CANONICAL:
     *      Not quote normally, but quote special characters (quoteChar, delimiter, line feed).
     *      This is specification of CSV.
     *      See https://tools.ietf.org/html/rfc4180#section-2
     *  ALL:
     *      Quote all fields.
     */
    var mode: WriteQuoteMode = WriteQuoteMode.CANONICAL
}

/**
 * Mode for writing quote
 *
 * Usage
 *
 *  CANONICAL:
 *      Not quote normally, but quote special characters (quoteChar, delimiter, line feed).
 *      This is specification of CSV.
 *      See https://tools.ietf.org/html/rfc4180#section-2
 *  ALL:
 *      Quote all fields.
 */
@Deprecated(
    message = "Replaced by com.jsoizo.kotlincsv.writer.WriteQuoteMode in v2.0. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
enum class WriteQuoteMode {
    CANONICAL,
    ALL,
    NON_NUMERIC
}
