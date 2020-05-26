package com.github.doyaaaaaken.kotlincsv.client

/**
 * CSV writer state and handler
 * @author blacmko18
 */
sealed class CsvWriterState(val wroteFirstLine: Boolean, val wroteLastLineTerminator: Boolean)

object DefaultState: CsvWriterState(wroteFirstLine = false, wroteLastLineTerminator =  false)
object HasNotWroteLastLineTerminator: CsvWriterState(wroteFirstLine = true, wroteLastLineTerminator = false)
object HasWroteLastLineTerminator: CsvWriterState(wroteFirstLine = true, wroteLastLineTerminator = true)

class CsvWriterStateHandler {
    private var state: CsvWriterState = DefaultState

    fun hasWroteFirstLine(): Boolean {
        return state.wroteFirstLine
    }

    fun hasWroteLastLineTerminator(): Boolean {
        return state.wroteLastLineTerminator
    }

    fun wroteLastLineTerminatorState() {
        state = HasWroteLastLineTerminator
    }

    fun notWroteLastLineTerminatorState() {
        state = HasNotWroteLastLineTerminator
    }
}