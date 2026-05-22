# Module kotlin-csv

Pure Kotlin Multiplatform CSV reader and writer.

## Overview

The library is split along the read/write axis:

- [com.jsoizo.kotlincsv.reader] for parsing CSV input
- [com.jsoizo.kotlincsv.writer] for encoding CSV output
- [com.jsoizo.kotlincsv.exceptions] for failures surfaced from both
- [com.jsoizo.kotlincsv.CsvDialect] is the shared format specification
  (`delimiter`, `quoteChar`, `escapeChar`, `lineTerminator`) consumed by both
  sides

## Construction

The two top-level entry points are:

- `csvReader { ... }` builds a [com.jsoizo.kotlincsv.reader.CsvReader] from a
  DSL block.
- `csvWriter { ... }` builds a [com.jsoizo.kotlincsv.writer.CsvWriter] from a
  DSL block.

Both also accept a pre-built `Config` for cases where the configuration is
composed elsewhere (`csvReader(config)` / `csvWriter(config)`).

## Stateless reader/writer

`CsvReader` and `CsvWriter` are stateless. The same instance can be reused
safely across calls and threads — all configuration is captured in the
immutable `Config` data class held by the instance. The returned `Sequence`
follows the standard cold-sequence contract: iterating a single sequence
from multiple threads is unsafe, but separate `read` / `write` calls on the
same instance are independent and can proceed concurrently.

## Lazy parsing / encoding

The core APIs operate on `Sequence`:

- Reader: `Sequence<Char>` to `Sequence<List<String>>`
- Writer: `Sequence<List<String>>` to `Sequence<Char>`

The returned sequences are **cold**. No work happens until a terminal
operation (`forEach`, `toList`, `first`, `take(n).toList()`, ...) pulls the
first element. Two consequences follow:

1. Callers can short-circuit. `take(n)` over a reader sequence stops pulling
   bytes after `n` rows. The I/O layer respects this through the lambda-based
   API (see "Resource management" below).
2. Exceptions are deferred. Format errors and field-count mismatches surface
   only once iteration reaches the offending row, not at the time the
   sequence is built. Functions that return a sequence document this with
   `@throws ... on terminal operation`.

## Resource management

I/O extensions that own a `Source` or `Sink` take a `block: (...) -> T`
parameter rather than returning a `Sequence` directly. The underlying
`Source` / `Sink` is opened, handed to the block, and closed when the block
returns or throws — so `take(n)` short-circuits and abrupt exceptions both
still close the resource. The pattern reads as:

```kotlin
reader.readFromFile(file) { rows ->
    rows.take(100).forEach { println(it) }
}  // file closed here
```

The contract is that callers must consume the `Sequence` inside the block.
Returning it leaks a handle to a now-closed source, and later iteration may
fail with `IOException` or surface garbage. The signature `(...) -> T` cannot
forbid this at the type level, so it is a contract callers are expected to
honour.

When you need a fully materialised `List<List<String>>`, prefer the eager
`readAllFromFile` overloads instead of writing `readFromFile(file) { it.toList() }` by hand:

```kotlin
val rows: List<List<String>> = reader.readAllFromFile(file)
```

`readAll` / `readAllFromFile` overloads exist for the same source shapes as
`read` / `readFromFile` (common: `Source` / `Path` / `String`; JVM:
`File` / `InputStream`).

# Package com.jsoizo.kotlincsv

Top-level entry points and the shared CSV format specification.

The two DSL builders ([csvReader] / [csvWriter]) and their config-accepting
overloads live here, along with [CsvDialect], the value object describing
the four characters that define a CSV format (`delimiter`, `quoteChar`,
`escapeChar`, `lineTerminator`). Built-in presets are exposed as
`CsvDialect.RFC4180` and `CsvDialect.TSV`.

# Package com.jsoizo.kotlincsv.reader

Reader API for parsing CSV input.

## Lazy parsing model

`CsvReader.read(chars)` returns a cold `Sequence<List<String>>`. Iteration
triggers parsing, and the parser pulls characters from the input sequence on
demand. Callers can `take(n)`, `first()`, or break out of `forEach { }` to
stop reading. Format errors raised by the parser
([com.jsoizo.kotlincsv.exceptions.CsvParseFormatException]) and
field-count mismatches
([com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException]) are
thrown when iteration reaches the offending row, not at the moment the
sequence is built.

The eager wrapper `CsvReader.readAll(text)` parses the whole input up-front
and returns a `List<List<String>>`. Exceptions propagate from the call site.

## Field-count policies

The first row sets the expected field count for the rest of the input. Two
config fields decide what happens to subsequent rows that disagree with that
count:

- [CsvReaderConfig.excessFieldsRowBehaviour] for rows that have **more**
  fields than expected. `ERROR` (default) throws
  [com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException]; `IGNORE`
  drops the row; `TRIM` truncates to the expected count.
- [CsvReaderConfig.insufficientFieldsRowBehaviour] for rows that have
  **fewer** fields than expected. `ERROR` (default) throws; `IGNORE` drops;
  `EMPTY_STRING` pads with empty strings to the expected count.

`CsvReaderConfig.skipEmptyLine` filters out fully empty rows before the
field-count check.

## Header processing

Header support is not part of the reader core; it is provided as the
extension [Sequence.withHeader]. Given the first row as a header, it
zips subsequent rows into `LinkedHashMap<String, String>` values, preserving
header order at the type level. Duplicate headers either throw
[com.jsoizo.kotlincsv.exceptions.MalformedCsvException] (default) or are
deterministically renamed with `_2`, `_3`, ... suffixes when
`autoRenameDuplicateHeaders = true`.

## I/O-layer behaviour

The I/O extensions wrap the core in a `block: (Sequence<List<String>>) -> T`
lambda so the caller never owns the open resource:

- common: `read(source: Source, ...)`, `read(path: Path, ...)`,
  `read(filePath: String, ...)` — UTF-8 only.
- JVM: `read(file: File, charset, ...)`, `read(stream: InputStream, charset, ...)`
  — accept Java charset names (e.g. `"SJIS"`, `"Shift_JIS"`,
  `"ISO-8859-1"`); resolved via `Charset.forName`.

The default for [CsvReadIoOptions.stripBom] is `true`, so a leading U+FEFF
is dropped after charset decoding. This works for any encoding that surfaces
the BOM as U+FEFF in the decoded character stream (UTF-8, UTF-16, ...).

## JS / Node.js streaming caveat

At the time of writing (kotlinx-io 0.9.0), the `FileSource` returned by
`SystemFileSystem.source(path)` on Node.js loads the entire file into memory
via `fs.readFileSync` on its first read. The `Sequence<Char>` shape is
preserved on JS for API uniformity, but on JS the in-memory footprint scales
with file size — streaming is effectively JVM-only. If the Node.js input
does not fit in memory, slice the file outside kotlin-csv and pass the
slices in.

## Reader and lineTerminator

The reader auto-detects line terminators (LF, CRLF, U+2028, U+2029, U+0085)
regardless of [CsvDialect.lineTerminator][com.jsoizo.kotlincsv.CsvDialect.lineTerminator]. The dialect's `lineTerminator`
field is consulted only by the writer. This keeps the reader permissive
across files produced by different platforms while still letting writer
output respect the dialect — `CsvDialect.RFC4180` writes CRLF and
`CsvDialect.TSV` writes LF.

# Package com.jsoizo.kotlincsv.writer

Writer API for encoding CSV output.

## Lazy encoding model

`CsvWriter.write(rows)` returns a cold `Sequence<Char>`. Iteration triggers
encoding, and the encoder pulls rows from the input sequence on demand. The
sequence itself never throws; failures arise only from the I/O layer that
ultimately consumes the characters.

The eager wrapper `CsvWriter.writeAll(rows)` joins the encoded characters
into a single `String`.

## Quote modes

[CsvWriterConfig.quoteMode] selects how aggressively the encoder wraps
fields in `quoteChar`:

- `CANONICAL` (default): quote only when necessary — when the field contains
  the delimiter, the quote character, or a line terminator.
- `ALL`: always quote every field.
- `NON_NUMERIC`: quote fields that contain anything other than digits and at
  most one dot. This is a simple lexical heuristic, not locale-aware number
  parsing.

## Escape character output rules

The writer's escape behaviour follows the configured
[CsvDialect.escapeChar][com.jsoizo.kotlincsv.CsvDialect.escapeChar]:

- When `escapeChar == quoteChar` (the default, both `"`), the encoder uses
  the RFC 4180 doubling style: a literal quote inside a quoted field is
  emitted as two quote characters (`a"b` -> `"a""b"`).
- When `escapeChar != quoteChar`, the encoder uses an explicit escape
  style — a CSV extension matching the reader. A literal quote becomes
  `<escapeChar><quoteChar>`, and a literal `escapeChar` becomes
  `<escapeChar><escapeChar>` (e.g. with `escapeChar = '\\'`,
  `a"b\c` -> `"a\"b\\c"`).

## Trailing line terminator

[CsvWriterConfig.outputLastLineTerminator] (default `true`) controls whether
the last row is followed by a line terminator. RFC 4180 §2 allows either
form; the default matches Excel and Google Sheets. An empty input sequence
produces an empty output sequence regardless of the flag.

## I/O-layer behaviour

The I/O extensions accept a `Sink` / `Path` / file-path string directly, or
a JVM-only `File` / `OutputStream`:

- common: `write(rows, sink: Sink, ...)`, `write(rows, path: Path, ...)`,
  `write(rows, filePath: String, ...)` — UTF-8 only.
- JVM: `write(rows, file: File, charset, ...)`,
  `write(rows, stream: OutputStream, charset, ...)`.

[CsvWriteIoOptions.prependBom] (default `false`) prepends U+FEFF before the
encoded body. With `"UTF-8"` it produces the standard `EF BB BF`.
Encodings that emit their own BOM during charset encoding (`"UTF-16"`)
produce a double BOM in combination with `prependBom = true`; pick
`"UTF-16BE"` / `"UTF-16LE"` if you only want one. Encodings without a BOM
concept (`"Shift_JIS"`, `"ISO-8859-1"`) typically replace U+FEFF with the
encoder's substitution character.

# Package com.jsoizo.kotlincsv.exceptions

Exceptions raised during CSV parsing.

## Hierarchy

```
RuntimeException
└── MalformedCsvException
    ├── CsvParseFormatException        (parse-level: malformed quote, ...)
    └── CsvFieldNumDifferentException  (row field count mismatch)
```

[MalformedCsvException] is the shared base; catching it covers every parse
failure raised by kotlin-csv itself. The two subclasses carry structured
context for the more specific failures.

## Field semantics

- [CsvParseFormatException] carries `rowNum: Long`, `colIndex: Long`, and
  `char: Char` — the row, column, and character that the parser refused.
- [CsvFieldNumDifferentException] carries `expectedFieldCount: Int`,
  `actualFieldCount: Int`, and `rowNum: Long`. The expected count is fixed
  by the first row.

Row and column indices are `Long` so files with more than `Int.MAX_VALUE`
rows can still report meaningful positions.

## Timing

For Sequence-returning APIs (`CsvReader.read`, `Sequence.withHeader`,
I/O-layer `read(...) { ... }`) all of these exceptions surface at the
terminal operation that drives iteration past the offending row, not at the
time the sequence is built. For eager APIs (`CsvReader.readAll`) they
propagate from the call site.
