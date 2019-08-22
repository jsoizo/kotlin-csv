package com.github.doyaaaaaken.kotlincsv.parser

/**
 * Csv Parse logic while reading csv
 *
 * @author doyaaaaaken
 */
internal class CsvParser(
        private val quoteChar: Char,
        private val delimiter: Char,
        private val escapeChar: Char
) {

    /**
     * @return return parsed row fields
     *         return null, if passed line string is on the way of csv row.
     */
    fun parseRow(line: String): List<String>? {
        val stateMachine = ParseStateMachine(quoteChar, delimiter, escapeChar)
        var lastCh: Char? = null
        var skipCount = 0
        line.zipWithNext { ch, nextCh ->
            if (skipCount > 0) {
                skipCount--
            } else {
                skipCount = stateMachine.read(ch, nextCh) - 1
            }
            lastCh = nextCh
        }
        lastCh?.let { stateMachine.read(it, null) }
        return stateMachine.getResult()
    }
}
