package com.jsoizo.kotlincsv.util

/**
 * General purpose Exception
 */
open class MalformedCSVException(message: String) : RuntimeException(message)

/**
 * Exception when parsing each csv row
 */
class CSVParseFormatException(
    val rowNum: Long,
    val colIndex: Long,
    val char: Char,
    message: String = "Exception happened on parsing csv"
) : MalformedCSVException("$message [rowNum = $rowNum, colIndex = $colIndex, char = $char]")

/**
 * Exception when field's num is different on each csv row.
 *
 * This is according to [CSV Specification](https://tools.ietf.org/html/rfc4180#section-2).
 * > Each line should contain the same number of fields throughout the file.
 *
 * For example, below csv data is invalid on 2nd csv row (`d, e`).
 * <pre>
 * a,b,c
 * d,e
 * f,g,h
 * </pre>
 */
class CSVFieldNumDifferentException(
    val fieldNum: Int,
    val fieldNumOnFailedRow: Int,
    val csvRowNum: Int
) : MalformedCSVException("Fields num seems to be $fieldNum on each row, but on ${csvRowNum}th csv row, fields num is $fieldNumOnFailedRow.")

class CSVAutoRenameFailedException :
    MalformedCSVException("auto renaming by 'autoRenameDuplicateHeaders' option is failed.")
