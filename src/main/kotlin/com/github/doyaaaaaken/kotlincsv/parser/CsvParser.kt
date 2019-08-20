package com.github.doyaaaaaken.kotlincsv.parser

/**
 * Csv Parse logic while reading csv
 *
 * @author doyaaaaaken
 */
internal class CsvParser {

    fun parseLine(line: String, quoteChar: Char, delimiter: Char, escapeChar: Char): List<String>? {
        val stateMachine = ParseStateMachine(quoteChar, delimiter, escapeChar)
        var lastCh: Char? = null
        line.zipWithNext { ch, nextCh ->
            stateMachine.next(ch, nextCh)
            lastCh = nextCh
        }
        lastCh?.let { stateMachine.next(it, null) }
        return stateMachine.getResult()
    }
}
