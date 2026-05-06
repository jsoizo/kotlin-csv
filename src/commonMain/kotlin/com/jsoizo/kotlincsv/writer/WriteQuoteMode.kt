package com.jsoizo.kotlincsv.writer

/** Strategy for when [CsvWriter] wraps a field in `quoteChar`. */
enum class WriteQuoteMode {
    /**
     * Quote only when required by RFC 4180 §2 (delimiter, line break, quote,
     * escape).
     */
    CANONICAL,

    /** Quote every field unconditionally. */
    ALL,

    /**
     * Quote any field that is not a decimal number (digits with at most one
     * `.`). Empty strings are non-numeric and therefore quoted.
     */
    NON_NUMERIC,
}
