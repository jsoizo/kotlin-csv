package com.github.doyaaaaaken.kotlincsv.util

/**
 * @author doyaaaaaken
 */
open class MalformedCSVException(message: String) : RuntimeException(message)

class CSVParseFormatException(
        val rowNum: Long,
        val colIndex: Long,
        val char: Char,
        message: String = "Exception happened on parsing csv"
) : MalformedCSVException("$message [rowNum = $rowNum, colIndex = $colIndex, char = $char]")
