package com.jsoizo.kotlincsv.writer

/**
 * Strategy used by [CsvWriter] for deciding when a field is wrapped in
 * `quoteChar`s.
 */
enum class WriteQuoteMode {
    /**
     * Quote a field only when it contains a delimiter, line break, the quote
     * character, or the escape character (RFC 4180 §2). The default.
     */
    CANONICAL,

    /** Quote every field unconditionally. */
    ALL,

    /**
     * Quote any field that does not parse as a decimal number — digits with at
     * most one '.'. Empty strings are not numeric, so they get quoted.
     */
    NON_NUMERIC,
}
