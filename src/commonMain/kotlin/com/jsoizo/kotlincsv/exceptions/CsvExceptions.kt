package com.jsoizo.kotlincsv.exceptions

/** Base type for all CSV parse failures raised by kotlin-csv. */
open class MalformedCsvException(message: String) : RuntimeException(message)

/** Parse failure at a specific row/column/character. */
class CsvParseFormatException(
    val rowNum: Long,
    val colIndex: Long,
    val char: Char,
    message: String = "Exception happened on parsing csv"
) : MalformedCsvException("$message [rowNum = $rowNum, colIndex = $colIndex, char = $char]")

/**
 * A row's field count differs from the count established by the first row.
 *
 * See [RFC 4180 §2](https://tools.ietf.org/html/rfc4180#section-2):
 * > Each line should contain the same number of fields throughout the file.
 */
class CsvFieldNumDifferentException(
    val expectedFieldCount: Int,
    val actualFieldCount: Int,
    val rowNum: Long
) : MalformedCsvException(
    "Fields num seems to be $expectedFieldCount on each row, but on ${rowNum}th csv row, fields num is $actualFieldCount."
)
