package com.jsoizo.kotlincsv.bench.shared

import kotlin.random.Random

object CsvDataGen {
    const val DEFAULT_SEED: Long = 42L

    private val asciiPrintable: CharArray =
        (('a'..'z') + ('A'..'Z') + ('0'..'9') + " .-_/".toList()).toCharArray()

    private val hiragana: CharArray = ('ぁ'..'ん').toList().toCharArray()

    data class Generated(
        val rows: List<List<String>>,
        val csvText: String,
        val csvBytes: ByteArray,
        val stats: DataStats,
    )

    fun generate(spec: DatasetSpec, seed: Long = DEFAULT_SEED): Generated {
        val random = Random(seed)
        val rows = ArrayList<List<String>>(spec.rows)
        var totalLen = 0L
        var maxLen = 0
        var quoteCells = 0
        var commaCells = 0
        var newlineCells = 0
        var multiByteCells = 0
        for (r in 0 until spec.rows) {
            val row = ArrayList<String>(spec.cols)
            for (c in 0 until spec.cols) {
                val len = 4 + random.nextInt(12) // 4..15
                val sb = StringBuilder(len)
                var hasQuote = false
                var hasComma = false
                var hasNewline = false
                var hasMultiByte = false
                for (i in 0 until len) {
                    val pickMulti = spec.utf8MultiByteRate > 0.0 && random.nextDouble() < spec.utf8MultiByteRate / 2.0
                    val ch: Char = when {
                        pickMulti -> {
                            hasMultiByte = true
                            hiragana[random.nextInt(hiragana.size)]
                        }
                        else -> asciiPrintable[random.nextInt(asciiPrintable.size)]
                    }
                    sb.append(ch)
                }
                if (spec.embeddedCommaRate > 0.0 && random.nextDouble() < spec.embeddedCommaRate) {
                    sb.insert(random.nextInt(sb.length + 1), ',')
                    hasComma = true
                }
                if (spec.embeddedNewlineRate > 0.0 && random.nextDouble() < spec.embeddedNewlineRate) {
                    sb.insert(random.nextInt(sb.length + 1), '\n')
                    hasNewline = true
                }
                if (spec.quoteRate > 0.0 && random.nextDouble() < spec.quoteRate) {
                    sb.insert(random.nextInt(sb.length + 1), '"')
                    hasQuote = true
                }
                val cell = sb.toString()
                row.add(cell)
                totalLen += cell.length
                if (cell.length > maxLen) maxLen = cell.length
                if (hasQuote) quoteCells++
                if (hasComma) commaCells++
                if (hasNewline) newlineCells++
                if (hasMultiByte) multiByteCells++
            }
            rows.add(row)
        }
        val csvText = encode(rows)
        val csvBytes = csvText.toByteArray(Charsets.UTF_8)
        val totalCells = spec.rows.toLong() * spec.cols
        val stats = DataStats(
            dataset = spec.name,
            rows = spec.rows,
            cols = spec.cols,
            seed = seed,
            avgCellLen = if (totalCells == 0L) 0.0 else totalLen.toDouble() / totalCells,
            maxCellLen = maxLen,
            quoteRate = if (totalCells == 0L) 0.0 else quoteCells.toDouble() / totalCells,
            embeddedCommaRate = if (totalCells == 0L) 0.0 else commaCells.toDouble() / totalCells,
            embeddedNewlineRate = if (totalCells == 0L) 0.0 else newlineCells.toDouble() / totalCells,
            utf8MultiByteCellRate = if (totalCells == 0L) 0.0 else multiByteCells.toDouble() / totalCells,
            totalBytes = csvBytes.size.toLong(),
        )
        return Generated(rows, csvText, csvBytes, stats)
    }

    private fun encode(rows: List<List<String>>): String {
        val sb = StringBuilder()
        for (row in rows) {
            var first = true
            for (cell in row) {
                if (!first) sb.append(',')
                first = false
                if (cell.contains('"') || cell.contains(',') || cell.contains('\n') || cell.contains('\r')) {
                    sb.append('"').append(cell.replace("\"", "\"\"")).append('"')
                } else {
                    sb.append(cell)
                }
            }
            sb.append("\r\n")
        }
        return sb.toString()
    }
}
