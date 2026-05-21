package com.jsoizo.kotlincsv.bench.shared

data class DataStats(
    val dataset: String,
    val rows: Int,
    val cols: Int,
    val seed: Long,
    val avgCellLen: Double,
    val maxCellLen: Int,
    val quoteRate: Double,
    val embeddedCommaRate: Double,
    val embeddedNewlineRate: Double,
    val utf8MultiByteCellRate: Double,
    val totalBytes: Long,
) {
    fun toJson(): String = buildString {
        append("{")
        append("\"dataset\":\"").append(dataset).append("\",")
        append("\"rows\":").append(rows).append(",")
        append("\"cols\":").append(cols).append(",")
        append("\"seed\":").append(seed).append(",")
        append("\"avgCellLen\":").append(avgCellLen).append(",")
        append("\"maxCellLen\":").append(maxCellLen).append(",")
        append("\"quoteRate\":").append(quoteRate).append(",")
        append("\"embeddedCommaRate\":").append(embeddedCommaRate).append(",")
        append("\"embeddedNewlineRate\":").append(embeddedNewlineRate).append(",")
        append("\"utf8MultiByteCellRate\":").append(utf8MultiByteCellRate).append(",")
        append("\"totalBytes\":").append(totalBytes)
        append("}")
    }
}
