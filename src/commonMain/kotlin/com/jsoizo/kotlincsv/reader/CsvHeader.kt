package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.exceptions.MalformedCsvException

/**
 * Treat the first row of this sequence as a header and zip subsequent rows
 * into [LinkedHashMap]s keyed by the header values.
 *
 * - The first row is consumed as the header. Iteration over the returned
 *   sequence skips it.
 * - Header / data rows of differing field counts are zipped to the shorter
 *   length (matching v1 semantics).
 * - When the header contains duplicates and [autoRenameDuplicateHeaders] is
 *   `false` (the default), iteration throws [MalformedCsvException] at the
 *   point the duplicate would first be observed.
 * - When [autoRenameDuplicateHeaders] is `true`, duplicate header names are
 *   renamed deterministically by appending the smallest unused `_n` suffix
 *   that does not collide with any header (original or already-renamed).
 *   Renaming always succeeds.
 * - The return type is [LinkedHashMap] (not [Map]) to preserve and expose
 *   header insertion order.
 *
 * @throws MalformedCsvException on terminal operation, when
 *   [autoRenameDuplicateHeaders] is `false` and the header row contains
 *   duplicate names. Not thrown when [autoRenameDuplicateHeaders] is `true`,
 *   since renaming is always successful.
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
