package com.github.doyaaaaaken.kotlincsv.client

/**
 * CSV writer state and handler
 * @author blacmko18
 */
internal class CsvWriterStateHandler {

    private sealed class CsvWriterState {
        object InitialState : CsvWriterState()
        object HasWroteTerminator : CsvWriterState()
        object MustWriteTerminatorOnNextLineHead : CsvWriterState()
    }

    private var state: CsvWriterState = CsvWriterState.InitialState

    fun hasNotWroteLineEndTerminator(): Boolean {
        return state is CsvWriterState.MustWriteTerminatorOnNextLineHead
    }

    fun setStateOfHasWroteTerminator() {
        state = CsvWriterState.HasWroteTerminator
    }

    fun setStateOfMustWriteTerminatorOnNextLineHead() {
        state = CsvWriterState.MustWriteTerminatorOnNextLineHead
    }
}
