package com.github.doyaaaaaken.kotlincsv.client

/**
 * CSV writer state and handler
 */
internal class CsvWriterStateHandler {

    private var mustWriteTerminatorOnNextLineHead: Boolean = false

    fun mustWriteTerminatorOnLineHead(): Boolean {
        return mustWriteTerminatorOnNextLineHead
    }

    fun setMustWriteTerminatorOnNextLineHead(must: Boolean) {
        mustWriteTerminatorOnNextLineHead = must
    }
}
