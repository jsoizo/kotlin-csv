# kotlin-csv v2 migration guide

A reference for upgrading from kotlin-csv 1.x to 2.0.

## 1. Highlights

- **Package layout**: `client/` and `dsl.context/` are gone. The public surface
  lives under `reader/`, `writer/`, `exceptions/`, and the package root.
- **Config + Dialect**: `*Context` mutable holders are replaced by
  immutable `CsvReaderConfig` / `CsvWriterConfig` data classes plus a shared
  `CsvDialect` value object.
- **Function-style I/O**: `csvReader().open(...) { ... }` becomes
  `reader.read(...) { rows -> ... }`, and `csvWriter().writeAll(rows, file)`
  becomes `writer.write(rows, file)`. The lambda owns the open resource.
- **Sequence-first core**: `read` returns `Sequence<List<String>>`, `write`
  takes `Sequence<List<String>>`. `take(n)` and `first()` short-circuit
  cleanly.
- **JVM-only charset**: charset is no longer a config field. It is an
  argument on the JVM `File` / `InputStream` / `OutputStream` overloads.
  `commonMain` and JS are UTF-8 only.
- **Removed**: `Logger`, `nullCode`, `autoRenameDuplicateHeaders` (config
  field — the option still exists on `withHeader()`), `skipMissMatchedRow`,
  `openAsync`, `readNext`, `openAndGetRawWriter`,
  `@KotlinCsvExperimental`, `@CsvDslMarker`.
- **Exception names** use Kotlin idiomatic `Csv` prefix:
  `MalformedCSVException` -> `MalformedCsvException`,
  `CSVParseFormatException` -> `CsvParseFormatException`,
  `CSVFieldNumDifferentException` -> `CsvFieldNumDifferentException`.
  Row indices on exceptions are now `Long`.
- **kotlinx-io**: added as a transitive dependency. JS gets file I/O for
  the first time (Node.js only).
- **No suspend API**: `openAsync` / `writeAllAsync` are removed. Wrap
  `read` / `write` calls in `withContext(Dispatchers.IO) { ... }` if needed.

## 2. Updating dependencies

`groupId` and `artifactId` are unchanged. Only the version moves:

### Gradle (Kotlin DSL)

```kotlin
implementation("com.jsoizo:kotlin-csv-jvm:2.0.0") // JVM
implementation("com.jsoizo:kotlin-csv-js:2.0.0")  // Kotlin/JS
```

### Gradle (Groovy DSL)

```groovy
implementation 'com.jsoizo:kotlin-csv-jvm:2.0.0'
implementation 'com.jsoizo:kotlin-csv-js:2.0.0'
```

### Maven

```xml
<dependency>
  <groupId>com.jsoizo</groupId>
  <artifactId>kotlin-csv-jvm</artifactId>
  <version>2.0.0</version>
</dependency>
```

### kscript

```kotlin
@file:DependsOn("com.jsoizo:kotlin-csv-jvm:2.0.0")
```

`kotlinx-io-core` is pulled in transitively. You should not need to add it
explicitly unless you want to construct `Source` / `Sink` / `Path` values
yourself.

## 3. Package moves

| 1.x | 2.0 |
| --- | --- |
| `com.jsoizo.kotlincsv.dsl.csvReader` | `com.jsoizo.kotlincsv.csvReader` |
| `com.jsoizo.kotlincsv.dsl.csvWriter` | `com.jsoizo.kotlincsv.csvWriter` |
| `com.jsoizo.kotlincsv.dsl.context.CsvReaderContext` | (removed — use `com.jsoizo.kotlincsv.reader.CsvReaderConfig`) |
| `com.jsoizo.kotlincsv.dsl.context.CsvWriterContext` | (removed — use `com.jsoizo.kotlincsv.writer.CsvWriterConfig`) |
| `com.jsoizo.kotlincsv.dsl.context.CsvWriteQuoteContext` | (removed — flat `CsvWriterConfig.quoteMode`) |
| `com.jsoizo.kotlincsv.client.CsvReader` | `com.jsoizo.kotlincsv.reader.CsvReader` |
| `com.jsoizo.kotlincsv.client.CsvWriter` | `com.jsoizo.kotlincsv.writer.CsvWriter` |
| `com.jsoizo.kotlincsv.client.CsvFileReader` / `CsvFileWriter` | (removed — function-style I/O extensions) |
| `com.jsoizo.kotlincsv.util.MalformedCSVException` | `com.jsoizo.kotlincsv.exceptions.MalformedCsvException` |
| `com.jsoizo.kotlincsv.util.CSVParseFormatException` | `com.jsoizo.kotlincsv.exceptions.CsvParseFormatException` |
| `com.jsoizo.kotlincsv.util.CSVFieldNumDifferentException` | `com.jsoizo.kotlincsv.exceptions.CsvFieldNumDifferentException` |
| `com.jsoizo.kotlincsv.util.CSVAutoRenameFailedException` | (removed — renaming is always deterministic in 2.0) |
| `com.jsoizo.kotlincsv.util.logger.*` | (removed — no logger hook) |

## 4. Reader API migration

### DSL configuration

```kotlin
// 1.x
val reader = csvReader {
    quoteChar = '"'
    delimiter = '\t'
    escapeChar = '\\'
    skipEmptyLine = true
}

// 2.0
val reader = csvReader {
    dialect = CsvDialect(
        quoteChar = '"',
        delimiter = '\t',
        escapeChar = '\\',
    )
    skipEmptyLine = true
}
```

The four format characters (`delimiter` / `quoteChar` / `escapeChar` /
`lineTerminator`) move to the shared `CsvDialect`. Reader-specific policies
(`skipEmptyLine`, `excessFieldsRowBehaviour`,
`insufficientFieldsRowBehaviour`) stay on `CsvReaderConfig`.

| 1.x reader option (CsvReaderContext) | 2.0 location |
| --- | --- |
| `quoteChar` | `CsvDialect.quoteChar` |
| `delimiter` | `CsvDialect.delimiter` |
| `escapeChar` | `CsvDialect.escapeChar` |
| `skipEmptyLine` | `CsvReaderConfig.skipEmptyLine` |
| `excessFieldsRowBehaviour` | `CsvReaderConfig.excessFieldsRowBehaviour` |
| `insufficientFieldsRowBehaviour` | `CsvReaderConfig.insufficientFieldsRowBehaviour` |
| `charset` | I/O argument on JVM overloads (see §6) |
| `logger` | (removed — see §7) |
| `autoRenameDuplicateHeaders` | argument on `withHeader()` (see below) |
| `skipMissMatchedRow` | (removed — see §7) |

### Reading

```kotlin
// 1.x
val rows: List<List<String>> = csvReader().readAll(file)

// 2.0
val rows: List<List<String>> = reader.read(file) { it.toList() }
```

`open { ... }` blocks become straight `read(file) { rows -> ... }` lambdas.
The block receives a `Sequence<List<String>>`; the underlying `Source` is
closed when the block returns or throws.

### Reading with header

```kotlin
// 1.x
val rows: List<Map<String, String>> = csvReader().readAllWithHeader(file)

// 2.0
val rows: List<Map<String, String>> = reader.read(file) {
    it.withHeader().toList()
}
```

`withHeader()` is now an extension on `Sequence<List<String>>` and returns
`Sequence<LinkedHashMap<String, String>>` so header insertion order is
preserved at the type level.

## 5. Writer API migration

### DSL configuration

```kotlin
// 1.x
val writer = csvWriter {
    delimiter = '\t'
    lineTerminator = "\n"
    nullCode = "NULL"
    outputLastLineTerminator = true
    quote {
        mode = WriteQuoteMode.ALL
        char = '\''
    }
}

// 2.0
val writer = csvWriter {
    dialect = CsvDialect(
        delimiter = '\t',
        quoteChar = '\'',
        lineTerminator = "\n",
    )
    outputLastLineTerminator = true
    quoteMode = WriteQuoteMode.ALL
}
```

The nested `quote { ... }` block is gone. `quote.char` moves to
`CsvDialect.quoteChar`, and `quote.mode` becomes a flat
`CsvWriterConfig.quoteMode`.

| 1.x writer option | 2.0 location |
| --- | --- |
| `delimiter` | `CsvDialect.delimiter` |
| `lineTerminator` | `CsvDialect.lineTerminator` |
| `quote.char` | `CsvDialect.quoteChar` |
| `quote.mode` | `CsvWriterConfig.quoteMode` |
| `outputLastLineTerminator` | `CsvWriterConfig.outputLastLineTerminator` |
| `charset` | I/O argument on JVM overloads (see §6) |
| `prependBOM` | `CsvWriteIoOptions.prependBom` (see §6) |
| `nullCode` | (removed — see §7 and §10.11) |

### Writing

```kotlin
// 1.x
csvWriter().writeAll(rows, file)
csvWriter().open(file) {
    writeRow("a", "b", "c")
    writeRows(rows)
}

// 2.0
writer.write(rows, file)
writer.write(
    sequence {
        yield(listOf("a", "b", "c"))
        yieldAll(rows)
    },
    file,
)
```

`write` accepts either a `List<List<String>>` (eager) or
`Sequence<List<String>>` (lazy). Build the sequence yourself instead of
calling `writeRow` inside an `open` block.

## 6. I/O API migration

### File path values

`commonMain` exposes `kotlinx.io.files.Path` and a `String` convenience
overload that wraps `Path(filePath)` for callers who do not want to import
`kotlinx.io.files`. Relative paths follow `SystemFileSystem` platform
behaviour.

```kotlin
reader.read(Path("data.csv")) { ... }   // kotlinx-io Path
reader.read("data.csv") { ... }         // String convenience overload
```

### Charset

Charset is no longer a config field. Pass it as an argument on the JVM
overloads:

```kotlin
// 1.x
val reader = csvReader { charset = "Shift_JIS" }
reader.readAll(file)

// 2.0
val reader = csvReader()
reader.read(file, charset = "Shift_JIS") { it.toList() }
```

`commonMain` and JS overloads are UTF-8 only. Java charset aliases
(`"SJIS"`, `"Shift_JIS"`, ...) work as before through `Charset.forName`.

### BOM

`prependBOM` moves from the writer config to a per-call I/O option. BOM
stripping on read is a new option and is **on by default** so files
produced by Excel are read cleanly.

```kotlin
// 1.x
val writer = csvWriter { prependBOM = true }
writer.writeAll(rows, file)

// 2.0
writer.write(rows, file, options = CsvWriteIoOptions(prependBom = true))

// Reading: strip the leading U+FEFF (default = true, opt out if needed)
reader.read(file, options = CsvReadIoOptions(stripBom = false)) { ... }
```

## 7. Removed features and replacements

| Removed in 2.0 | Replacement |
| --- | --- |
| `Logger` / `LoggerImpl` and `csvReader { logger = ... }` | Wrap your own logging around the call site. |
| `csvReader { autoRenameDuplicateHeaders = true }` | Pass it to `withHeader(autoRenameDuplicateHeaders = true)` (see §10.3). |
| `csvReader { skipMissMatchedRow = true }` | Set `excessFieldsRowBehaviour = IGNORE` and / or `insufficientFieldsRowBehaviour = IGNORE`. |
| `csvWriter { nullCode = "NULL" }` | Map nulls explicitly: `rows.map { row -> row.map { it ?: "NULL" } }`. See §10.11. |
| `csvReader().openAsync { ... }` / `csvWriter().openAsync { ... }` | Wrap the synchronous call in `withContext(Dispatchers.IO) { reader.read(file) { ... } }`. See §10.6. |
| `csvReader().open { readNext() }` (line-by-line) | `reader.read(file) { it.first() }` or `it.iterator()`. See §10.5. |
| `csvWriter().openAndGetRawWriter(file)` (manual close) | Hold a `kotlinx.io.Sink` yourself and call `writer.write(rows, sink)`. See §10.15. |
| `csvWriter().writeAll(rows, file, append = true)` | Open a JVM `FileOutputStream(file, append = true)` and pass it to `writer.write(rows, stream)`. See §10.16. JVM only. |
| `@KotlinCsvExperimental` | Removed; the APIs it guarded are either stable or removed. |
| `@CsvDslMarker` | Removed; v2 has no nested DSL blocks for `@DslMarker` to disambiguate. |
| `MalformedCSVException` / `CSVParseFormatException` / `CSVFieldNumDifferentException` (uppercase `CSV`) | `MalformedCsvException` / `CsvParseFormatException` / `CsvFieldNumDifferentException` in `com.jsoizo.kotlincsv.exceptions`. |
| `CSVAutoRenameFailedException` | Removed; `withHeader(autoRenameDuplicateHeaders = true)` is always successful. |

## 8. New capabilities

- **`CsvDialect`** is the shared format value object. Two presets are
  built in: `CsvDialect.RFC4180` (the default) and `CsvDialect.TSV`.
- **JS file I/O** (Node.js): `reader.read(path) { ... }` and
  `writer.write(rows, path)` work on Kotlin/JS for the first time. See §9
  for the streaming caveat.
- **`Sequence`-first core**: `reader.read(chars: Sequence<Char>)` returns
  a cold `Sequence<List<String>>`; `writer.write(rows: Sequence<List<String>>)`
  returns a cold `Sequence<Char>`. Combine with `take(n)` and friends to
  short-circuit.
- **`String` path overload**: `reader.read("data.csv") { ... }` /
  `writer.write(rows, "out.csv")` skip the explicit `Path(...)` import.
- **`withHeader()`** as an extension on `Sequence<List<String>>`. Returns
  `Sequence<LinkedHashMap<String, String>>` so header order is preserved.
- **`escapeChar != quoteChar` writer support**: when the dialect's escape
  character differs from the quote character, the writer emits an
  explicit-escape style (a CSV extension already accepted by the v1
  reader). The default `escapeChar == quoteChar` keeps RFC 4180 doubling.

## 9. Behavioural changes

- **Lazy exception timing**. `Sequence`-returning APIs raise format errors
  and field-count mismatches at the *terminal operation*
  (`forEach` / `toList` / `first` / ...), not when the sequence is built.
  Catch them around iteration, not around the call that creates the
  sequence:

  ```kotlin
  reader.read(file) { rows ->
      try {
          rows.forEach { ... }
      } catch (e: CsvParseFormatException) { ... }
  }
  ```

- **`CsvDialect.lineTerminator` is writer-only**. The reader auto-detects
  line terminators (LF / CRLF / U+2028 / U+2029 / U+0085) regardless of
  the configured `lineTerminator`. Setting
  `CsvDialect(lineTerminator = "\n")` does **not** make the reader strict
  about LF. This matches v1 reader behaviour but the field is now
  declared on the dialect.

- **JS file I/O loads the whole file into memory.** At the time of
  writing (kotlinx-io 0.7.0), `SystemFileSystem.source(path)` on Node.js
  reads the entire file via `fs.readFileSync` on its first read.
  Streaming with `take(n)` is effectively JVM-only despite the
  `Sequence` shape; the JS `Sequence` yields from an in-memory buffer.
  If the input does not fit in memory, slice it outside kotlin-csv.

- **BOM stripping defaults to ON.** v1 silently surfaced a leading
  `U+FEFF` to callers; 2.0 drops it by default. Set
  `CsvReadIoOptions(stripBom = false)` to keep the v1 behaviour.

- **Field-count exception field names changed.** On
  `CsvFieldNumDifferentException`, `fieldNum` -> `expectedFieldCount`,
  `fieldNumOnFailedRow` -> `actualFieldCount`, `csvRowNum` -> `rowNum`
  (now `Long`).

## 10. Cookbook

Each entry shows the v1 form on top and the v2 form below.

In v2 the DSL builders return reusable instances, so the samples assume:

```kotlin
val reader = csvReader()
val writer = csvWriter()
```

### Read

#### 10.1 Read a string into `List<List<String>>`

```kotlin
// 1.x
val rows = csvReader().readAll("a,b,c\nd,e,f")

// 2.0
val rows = reader.readAll("a,b,c\nd,e,f")
```

#### 10.2 Read a file into `List<List<String>>`

```kotlin
// 1.x
val rows = csvReader().readAll(File("data.csv"))

// 2.0
val rows = reader.read(File("data.csv")) { it.toList() }
// or: reader.read("data.csv") { it.toList() }
```

#### 10.3 Read with header

```kotlin
// 1.x
val rows = csvReader().readAllWithHeader(File("data.csv"))

// 2.0
val rows = reader.read(File("data.csv")) { it.withHeader().toList() }

// Auto-rename duplicate headers
val deduped = reader.read(File("data.csv")) {
    it.withHeader(autoRenameDuplicateHeaders = true).toList()
}
```

#### 10.4 Read as a streaming Sequence (lazy)

```kotlin
// 1.x
csvReader().open(File("data.csv")) {
    readAllAsSequence().forEach { row -> /* ... */ }
}

// 2.0 — short-circuit with take(n)
reader.read(File("data.csv")) { rows ->
    rows.take(100).forEach { println(it) }
}
```

The lambda boundary closes the file when `take` finishes consuming.

#### 10.5 Read row by row (peek the next row only)

```kotlin
// 1.x
csvReader().open(File("data.csv")) {
    val first = readNext()
    /* ... */
}

// 2.0
reader.read(File("data.csv")) { rows ->
    val first: List<String>? = rows.firstOrNull()
    /* ... */
}

// 2.0 — manual iterator if you want to step through one at a time
reader.read(File("data.csv")) { rows ->
    val iter = rows.iterator()
    while (iter.hasNext()) {
        val row = iter.next()
        /* ... */
    }
}
```

#### 10.6 Read inside a coroutine

```kotlin
// 1.x
csvReader().openAsync(File("data.csv")) {
    readAllAsSequence().asFlow().collect { /* ... */ }
}

// 2.0
withContext(Dispatchers.IO) {
    reader.read(File("data.csv")) { rows ->
        rows.forEach { /* ... */ }
    }
}
```

There is no dedicated suspend API in 2.0. The synchronous call is
moved to `Dispatchers.IO` by the caller.

#### 10.7 Read a TSV

```kotlin
// 1.x
val tsvReader = csvReader { delimiter = '\t' }
val rows = tsvReader.readAll(File("data.tsv"))

// 2.0
val tsvReader = csvReader { dialect = CsvDialect.TSV }
val rows = tsvReader.read(File("data.tsv")) { it.toList() }
```

#### 10.8 Read with a non-UTF-8 charset (JVM only)

```kotlin
// 1.x
val sjisReader = csvReader { charset = "Shift_JIS" }
val rows = sjisReader.readAll(File("data.csv"))

// 2.0 — charset moves to the I/O call
val rows = reader.read(File("data.csv"), charset = "Shift_JIS") { it.toList() }
```

### Write

#### 10.9 Write `List<List<String>>` to a file

```kotlin
// 1.x
csvWriter().writeAll(rows, "out.csv")

// 2.0
writer.write(rows, "out.csv")
```

#### 10.10 Write row by row

```kotlin
// 1.x
csvWriter().open("out.csv") {
    writeRow("a", "b", "c")
    writeRow(listOf("d", "e", "f"))
}

// 2.0
writer.write(
    sequence {
        yield(listOf("a", "b", "c"))
        yield(listOf("d", "e", "f"))
    },
    "out.csv",
)
```

#### 10.11 Write nullable values (`nullCode` equivalent)

```kotlin
// 1.x
val writer = csvWriter { nullCode = "NULL" }
writer.writeAll(listOf(listOf("a", null, "c")), "out.csv")

// 2.0 — map the nulls yourself
val rows: List<List<String?>> = listOf(listOf("a", null, "c"))
writer.write(rows.map { row -> row.map { it ?: "NULL" } }, "out.csv")
```

#### 10.12 Write to a `String`

```kotlin
// 1.x
val csv: String = csvWriter().writeAllAsString(rows)

// 2.0
val csv: String = writer.writeAll(rows)
```

#### 10.13 Write with a UTF-8 BOM (Excel)

```kotlin
// 1.x
csvWriter { prependBOM = true }.writeAll(rows, "out.csv")

// 2.0
writer.write(rows, "out.csv", options = CsvWriteIoOptions(prependBom = true))
```

#### 10.14 Always quote every field

```kotlin
// 1.x
val writer = csvWriter {
    quote { mode = WriteQuoteMode.ALL }
}

// 2.0
val writer = csvWriter { quoteMode = WriteQuoteMode.ALL }
```

#### 10.15 Hold the file open across many writes (raw writer)

```kotlin
// 1.x
@OptIn(KotlinCsvExperimental::class)
val raw = csvWriter().openAndGetRawWriter("out.csv")
raw.writeRow("a", "b")
raw.close()

// 2.0 — manage the Sink yourself
val sink = SystemFileSystem.sink(Path("out.csv")).buffered()
sink.use {
    writer.write(sequenceOf(listOf("a", "b")), it)
    writer.write(sequenceOf(listOf("c", "d")), it)
}
```

#### 10.16 Append to an existing file (JVM only)

```kotlin
// 1.x
csvWriter().writeAll(rows, "out.csv", append = true)

// 2.0 — JVM only; common / JS overloads are truncate-only
FileOutputStream(File("out.csv"), /* append = */ true).use { stream ->
    writer.write(rows, stream)
}
```

### Cross-platform / Custom

#### 10.17 Custom dialect

```kotlin
// 1.x
val reader = csvReader {
    delimiter = ';'
    quoteChar = '"'
    escapeChar = '\\'
}

// 2.0
val reader = csvReader {
    dialect = CsvDialect(
        delimiter = ';',
        quoteChar = '"',
        escapeChar = '\\',
    )
}
```

`CsvDialect` rejects inconsistent combinations
(`delimiter == quoteChar`, empty `lineTerminator`, ...) at construction
time with `IllegalArgumentException`.

#### 10.18 Read a CSV from Node.js

```kotlin
// 1.x — not supported on Kotlin/JS
// (No file I/O APIs were exposed.)

// 2.0 — Node.js only, UTF-8
val rows = reader.read("data.csv") { it.toList() }
```

The browser target has no file I/O. Pass already-loaded text to
`reader.readAll(text)` instead.
