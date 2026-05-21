package com.jsoizo.kotlincsv.bench.shared

enum class DatasetSpec(
    val rows: Int,
    val cols: Int,
    val quoteRate: Double,
    val embeddedCommaRate: Double,
    val embeddedNewlineRate: Double,
    val utf8MultiByteRate: Double,
) {
    SMALL(rows = 1_000, cols = 10, quoteRate = 0.0, embeddedCommaRate = 0.0, embeddedNewlineRate = 0.0, utf8MultiByteRate = 0.0),
    MEDIUM(rows = 100_000, cols = 20, quoteRate = 0.0, embeddedCommaRate = 0.0, embeddedNewlineRate = 0.0, utf8MultiByteRate = 0.0),
    LARGE(rows = 1_000_000, cols = 10, quoteRate = 0.0, embeddedCommaRate = 0.0, embeddedNewlineRate = 0.0, utf8MultiByteRate = 0.0),
    HARD(rows = 10_000, cols = 10, quoteRate = 0.30, embeddedCommaRate = 0.10, embeddedNewlineRate = 0.05, utf8MultiByteRate = 0.20),
}
