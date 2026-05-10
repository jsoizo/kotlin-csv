package com.jsoizo.kotlincsv

import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of

internal val targetedChar: Arb<Char> = Arb.of(
    ',', '\t', '"', '\\',
    '\n', '\r', ' ', ' ', '',
    ' ', 'a', '0', '.',
)

internal val nonSurrogateChar: Arb<Char> =
    Arb.int(0..0xFFFF)
        .filter { it !in 0xD800..0xDFFF }
        .map { it.toChar() }

// weighted ~7:3 targeted vs full unicode
internal val fieldChar: Arb<Char> = Arb.choice(
    targetedChar, targetedChar, targetedChar, targetedChar,
    targetedChar, targetedChar, targetedChar,
    nonSurrogateChar, nonSurrogateChar, nonSurrogateChar,
)

internal val dialectArb: Arb<CsvDialect> = Arb.of(
    CsvDialect.RFC4180,
    CsvDialect.TSV,
    CsvDialect(escapeChar = '\\'),
)
