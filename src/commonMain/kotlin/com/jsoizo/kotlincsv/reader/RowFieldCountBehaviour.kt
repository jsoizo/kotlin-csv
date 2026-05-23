package com.jsoizo.kotlincsv.reader

/** Strategy for rows with fewer fields than the first row. */
enum class InsufficientFieldsRowBehaviour {
    /** Throw [com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException] (default). */
    ERROR,

    /** Drop the row and continue. */
    IGNORE,

    /** Pad the row with empty strings up to the expected field count. */
    EMPTY_STRING,
}

/** Strategy for rows with more fields than the first row. */
enum class ExcessFieldsRowBehaviour {
    /** Throw [com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException] (default). */
    ERROR,

    /** Drop the row and continue. */
    IGNORE,

    /** Truncate excess fields off the end of the row. */
    TRIM,
}
