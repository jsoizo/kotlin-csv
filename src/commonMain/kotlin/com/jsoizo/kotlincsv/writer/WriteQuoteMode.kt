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
     * Quote any field that contains a character other than digits or a single
     * `.`. Empty strings are quoted. A decimal-like field is also quoted when
     * [CANONICAL] would quote it, e.g. when the dialect uses `.` or a digit as
     * its delimiter or quote character.
     */
    NON_NUMERIC,
}
