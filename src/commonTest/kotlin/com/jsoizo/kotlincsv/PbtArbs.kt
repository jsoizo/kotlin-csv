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

internal val surrogateChar: Arb<Char> =
    Arb.int(0xD800..0xDFFF).map { it.toChar() }

// weighted ~7:3 targeted vs full unicode
internal val fieldChar: Arb<Char> = Arb.choice(
    targetedChar, targetedChar, targetedChar, targetedChar,
    targetedChar, targetedChar, targetedChar,
    nonSurrogateChar, nonSurrogateChar, nonSurrogateChar,
)

// fieldChar with occasional lone surrogates mixed in (~1/10 surrogate).
// Use only in tests that must accept arbitrary `String`s, e.g. parser
// crash-freedom; round-trip writes assume well-formed Unicode.
internal val anyChar: Arb<Char> = Arb.choice(
    fieldChar, fieldChar, fieldChar, fieldChar, fieldChar,
    fieldChar, fieldChar, fieldChar, fieldChar,
    surrogateChar,
)

internal val dialectArb: Arb<CsvDialect> = Arb.of(
    CsvDialect.RFC4180,
    CsvDialect.TSV,
    CsvDialect(escapeChar = '\\'),
)
