package com.jsoizo.kotlincsv.reader

/** Controls which empty CSV fields are exposed as `null` by nullable reader APIs. */
enum class CsvNullFieldIndicator {
    /** Treat quoted and unquoted empty fields as empty strings. */
    NEITHER,

    /** Treat unquoted empty fields as `null`. */
    EMPTY_SEPARATORS,

    /** Treat quoted empty fields as `null`. */
    EMPTY_QUOTES,

    /** Treat both quoted and unquoted empty fields as `null`. */
    BOTH
}
