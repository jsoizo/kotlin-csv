package com.jsoizo.kotlincsv.reader

/**
 * Strategy for handling rows that have fewer fields than expected.
 *
 * The expected field count is determined by the first row of the CSV input.
 */
enum class InsufficientFieldsRowBehaviour {
    /** Throw [com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException] (default). */
    ERROR,

    /** Skip the row and continue. */
    IGNORE,

    /** Pad the row with empty strings up to the expected field count. */
    EMPTY_STRING,
}

/**
 * Strategy for handling rows that have more fields than expected.
 *
 * The expected field count is determined by the first row of the CSV input.
 */
enum class ExcessFieldsRowBehaviour {
    /** Throw [com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException] (default). */
    ERROR,

    /** Skip the row and continue. */
    IGNORE,

    /** Truncate the row to the expected field count. */
    TRIM,
}
