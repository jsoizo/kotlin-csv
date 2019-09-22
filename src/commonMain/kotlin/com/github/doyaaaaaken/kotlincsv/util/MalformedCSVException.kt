package com.github.doyaaaaaken.kotlincsv.util

/**
 * @author doyaaaaaken
 */
open class MalformedCSVException(message: String) : RuntimeException(message)

class CSVParseFormatException(
        message: String = "Exception happened on parsing csv",
        val rowNum: Long,
        val colIndex: Long
) : MalformedCSVException("$message [rowNum = $rowNum, colIndex = $colIndex]")
