package com.github.doyaaaaaken.kotlincsv.client

/**
 * CSV writer state and handler
 * @author blacmko18
 */
internal class CsvWriterStateHandler {

    private sealed class CsvWriterState {
        object InitialState : CsvWriterState()
        object HasNotWroteLineEndTerminator : CsvWriterState()
        object HasWroteLineEndTerminator : CsvWriterState()
    }

    private var state: CsvWriterState = CsvWriterState.InitialState

    fun hasNotWroteLineEndTerminator(): Boolean {
        return state is CsvWriterState.HasNotWroteLineEndTerminator
    }

    fun setStateOfWroteLineEndTerminator() {
        state = CsvWriterState.HasWroteLineEndTerminator
    }

    fun setStateOfNotWroteLineEndTerminator() {
        state = CsvWriterState.HasNotWroteLineEndTerminator
    }
}
