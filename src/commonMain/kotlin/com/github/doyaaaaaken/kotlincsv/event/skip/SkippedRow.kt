package com.github.doyaaaaaken.kotlincsv.event.skip

/**
 *  data class containing relevant information
 */
data class SkippedRow(
    /**
     * row index in the csv file
     */
    val idx: Int,
    /**
     * row values
     */
    val row: List<String>,
    /**
     * reason for skipping
     */
    val message: String,
    /**
     * type of skip
     */
    val skipType: SkipType

)
