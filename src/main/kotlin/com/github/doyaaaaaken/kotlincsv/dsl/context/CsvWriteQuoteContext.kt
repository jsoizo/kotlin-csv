package com.github.doyaaaaaken.kotlincsv.dsl.context

import com.github.doyaaaaaken.kotlincsv.util.CsvDslMarker

/**
 * DSL method for Quote settings on writing csv.
 *
 * @author doyaaaaaken
 */
@CsvDslMarker
class CsvWriteQuoteContext {
    var quoteChar: Char = '"'
    var quoteMode: WriteQuoteMode = WriteQuoteMode.MINIMAL
}

/**
 * Mode for writing quote
 *
 * Usage
 *
 *  MINIMAL:
 *      Not quote normally, but quote special characters (quoteChar, delimiter, line feed).
 *      This is specification of CSV.
 *      See https://tools.ietf.org/html/rfc4180#section-2
 *  ALL:
 *      Quote all fields.
 *  NONE:
 *      Not quote each fields.
 */
enum class WriteQuoteMode {
    MINIMAL,
    ALL,
    NONE
}
