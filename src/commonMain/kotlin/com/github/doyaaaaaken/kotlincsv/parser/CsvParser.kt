package com.github.doyaaaaaken.kotlincsv.parser

/**
 * Csv Parse logic while reading csv
 *
 * @author doyaaaaaken
 */
internal class CsvParser(
    private val quoteChar: Char,
    private val delimiter: Char,
    private val escapeChar: Char,
    private val withFieldAsNull: ParserNullFieldIndicator
) {

    /**
     * parse line (or lines if there is any quoted field containing line terminator)
     * and return csv row's fields as List<String>.
     *
     * @return return parsed row fields
     *         return null, if passed line string is on the way of csv row.
     */
    fun parseRow(line: String, rowNum: Long = 1): List<String?>? {
        val stateMachine = ParseStateMachine(quoteChar, delimiter, escapeChar, withFieldAsNull)
        var lastCh: Char? = line.firstOrNull()
        var skipCount = 0L
        line.zipWithNext { ch, nextCh ->
            if (skipCount > 0) {
                skipCount--
            } else {
                skipCount = stateMachine.read(ch, nextCh, rowNum) - 1
            }
            lastCh = nextCh
        }
        if (lastCh != null && skipCount == 0L) {
            stateMachine.read(requireNotNull(lastCh), null, rowNum)
        }
        return stateMachine.getResult()
    }
}
