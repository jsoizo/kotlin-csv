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

    internal sealed class CsvWriterState(val wroteFirstLine: Boolean, val wroteLineEndTerminator: Boolean) {
        object InitialState: CsvWriterState(wroteFirstLine = false, wroteLineEndTerminator =  false)
        object HasNotWroteLineEndTerminator: CsvWriterState(wroteFirstLine = true, wroteLineEndTerminator = false)
        object HasWroteLineEndTerminator: CsvWriterState(wroteFirstLine = true, wroteLineEndTerminator = true)
    }

    private var state: CsvWriterState = CsvWriterState.InitialState

    fun hasWroteFirstLine(): Boolean {
        return state.wroteFirstLine
    }

    fun hasWroteLineEndTerminator(): Boolean {
        return state.wroteLineEndTerminator
    }

    fun wroteLineEndTerminatorState() {
        state = CsvWriterState.HasWroteLineEndTerminator
    }

    fun notWroteLineEndTerminatorState() {
        state = CsvWriterState.HasNotWroteLineEndTerminator
    }
}