package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.exceptions.MalformedCsvException

/**
 * Treat the first row as a header and zip subsequent rows into
 * [LinkedHashMap]s keyed by the header values.
 *
 * Duplicate headers throw [MalformedCsvException] by default; with
 * [autoRenameDuplicateHeaders] = `true` they are deterministically renamed
 * with the smallest unused `_n` suffix.
 *
 * @throws MalformedCsvException on terminal operation when the header has
 *   duplicates and [autoRenameDuplicateHeaders] is `false`.
 */
fun Sequence<List<String>>.withHeader(
    autoRenameDuplicateHeaders: Boolean = false,
): Sequence<LinkedHashMap<String, String>> = sequence {
    val iter = iterator()
    if (!iter.hasNext()) return@sequence

    val rawHeader = iter.next()
    val header = if (autoRenameDuplicateHeaders) {
        renameDuplicates(rawHeader)
    } else {
        ensureUnique(rawHeader)
        rawHeader
    }

    while (iter.hasNext()) {
        val row = iter.next()
        val map = LinkedHashMap<String, String>(header.size)
        val limit = minOf(header.size, row.size)
        for (i in 0 until limit) {
            map[header[i]] = row[i]
        }
        yield(map)
    }
}

/**
 * Treat the first nullable row as a non-null header and zip subsequent rows
 * into [LinkedHashMap]s keyed by the header values.
 *
 * Header values are column names, so `null` header fields are normalized to
 * empty headers (`""`). Duplicate handling matches [withHeader].
 */
fun Sequence<List<String?>>.withNullableHeader(
    autoRenameDuplicateHeaders: Boolean = false,
): Sequence<LinkedHashMap<String, String?>> = sequence {
    val iter = iterator()
    if (!iter.hasNext()) return@sequence

    val rawHeader = iter.next().map { it ?: "" }
    val header = if (autoRenameDuplicateHeaders) {
        renameDuplicates(rawHeader)
    } else {
        ensureUnique(rawHeader)
        rawHeader
    }

    while (iter.hasNext()) {
        val row = iter.next()
        val map = LinkedHashMap<String, String?>(header.size)
        val limit = minOf(header.size, row.size)
        for (i in 0 until limit) {
            map[header[i]] = row[i]
        }
        yield(map)
    }
}

private fun ensureUnique(header: List<String>) {
    val seen = mutableSetOf<String>()
    for (h in header) {
        if (!seen.add(h)) {
            throw MalformedCsvException("header '$h' is duplicated.")
        }
    }
}

private fun renameDuplicates(header: List<String>): List<String> {
    val taken = header.toMutableSet()
    val emitted = mutableSetOf<String>()
    return header.map { h ->
        if (emitted.add(h)) {
            h
        } else {
            var n = 2
            while ("${h}_${n}" in taken) n++
            val newName = "${h}_${n}"
            emitted.add(newName)
            taken.add(newName)
            newName
        }
    }
}
