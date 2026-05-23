package com.jsoizo.kotlincsv.reader

import com.jsoizo.kotlincsv.exceptions.MalformedCsvException

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
        for (index in 0 until limit) {
            map[header[index]] = row[index]
        }
        yield(map)
    }
}

private fun ensureUnique(header: List<String>) {
    val seen = mutableSetOf<String>()
    for (name in header) {
        if (!seen.add(name)) {
            throw MalformedCsvException("header '$name' is duplicated.")
        }
    }
}

private fun renameDuplicates(header: List<String>): List<String> {
    val taken = header.toMutableSet()
    val emitted = mutableSetOf<String>()
    return header.map { name ->
        if (emitted.add(name)) {
            name
        } else {
            var suffix = 2
            while ("${name}_${suffix}" in taken) suffix++
            val newName = "${name}_${suffix}"
            emitted.add(newName)
            taken.add(newName)
            newName
        }
    }
}
