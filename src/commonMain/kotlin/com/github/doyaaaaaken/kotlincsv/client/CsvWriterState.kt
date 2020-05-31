package com.github.doyaaaaaken.kotlincsv.client

/**
 * CSV writer state and handler
 * @author blacmko18
 */
internal class CsvWriterStateHandler {

    private sealed class CsvWriterState(val wroteFirstLine: Boolean, val wroteLineEndTerminator: Boolean) {
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