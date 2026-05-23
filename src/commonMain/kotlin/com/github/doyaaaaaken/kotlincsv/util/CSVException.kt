package com.github.doyaaaaaken.kotlincsv.util

/**
 * General purpose Exception
 */
@Deprecated(
    message = "Replaced by com.jsoizo.kotlincsv.exceptions.MalformedCsvException in v2.0. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
open class MalformedCSVException(message: String) : RuntimeException(message)

/**
 * Exception when parsing each csv row
 */
@Deprecated(
    message = "Replaced by com.jsoizo.kotlincsv.exceptions.CsvParseFormatException in v2.0. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
@Suppress("DEPRECATION")
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
@Deprecated(
    message = "Replaced by com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException in v2.0. " +
            "The constructor and property names change, so rewrite usages manually. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
@Suppress("DEPRECATION")
class CSVFieldNumDifferentException(
    val fieldNum: Int,
    val fieldNumOnFailedRow: Int,
    val csvRowNum: Int
) : MalformedCSVException("Fields num seems to be $fieldNum on each row, but on ${csvRowNum}th csv row, fields num is $fieldNumOnFailedRow.")

@Deprecated(
    message = "v2.0 removes this failure type; header auto-rename is always deterministic and has no " +
            "counterpart exception. See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
@Suppress("DEPRECATION")
class CSVAutoRenameFailedException :
    MalformedCSVException("auto renaming by 'autoRenameDuplicateHeaders' option is failed.")
