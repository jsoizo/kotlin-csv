package com.jsoizo.kotlincsv.dsl.context

import com.jsoizo.kotlincsv.util.CsvDslMarker

/**
 * DSL method for Quote settings on writing csv.
 *
 * @author doyaaaaaken
 */
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
enum class WriteQuoteMode {
    CANONICAL,
    ALL,
    NON_NUMERIC
}
