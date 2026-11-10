# kotlin-csv

<p>
  <img alt="Version" src="https://img.shields.io/badge/version-2.0.0-blue.svg?cacheSeconds=2592000" />
  <a href="https://github.com/jsoizo/kotlin-csv/blob/main/LICENSE">
    <img alt="License: Apache License 2.0" src="https://img.shields.io/badge/License-Apache License 2.0-yellow.svg" target="_blank" />
  </a>
  <a href="https://codecov.io/gh/jsoizo/kotlin-csv">
    <img src="https://codecov.io/gh/jsoizo/kotlin-csv/branch/main/graph/badge.svg" alt="codecov" />
  </a>
  <a href="https://www.codefactor.io/repository/github/jsoizo/kotlin-csv">
    <img src="https://www.codefactor.io/repository/github/jsoizo/kotlin-csv/badge" alt="CodeFactor" />
  </a>
</p>

Pure Kotlin Multiplatform CSV reader and writer.

## Setup

### Gradle (Kotlin DSL)

```kotlin
implementation("com.jsoizo:kotlin-csv:2.0.0")
```

The multiplatform artifact resolves JVM, JS, Kotlin/Wasm, and supported
Kotlin/Native variants from Kotlin Multiplatform projects. Published
Native targets are `macosArm64`, `iosArm64`, `iosSimulatorArm64`,
`linuxX64`, `linuxArm64`, and `mingwX64`. Published Wasm targets are
`wasmJs` and `wasmWasi`.

Single-platform Gradle projects can also depend on the platform artifact
directly, for example `kotlin-csv-jvm`, `kotlin-csv-js`,
`kotlin-csv-wasm-js`, `kotlin-csv-macosarm64`, or `kotlin-csv-linuxx64`.

### Gradle (Groovy DSL)

```groovy
implementation 'com.jsoizo:kotlin-csv:2.0.0'
```

### Maven

```xml
<dependency>
  <groupId>com.jsoizo</groupId>
  <artifactId>kotlin-csv-jvm</artifactId>
  <version>2.0.0</version>
</dependency>
<dependency>
  <groupId>com.jsoizo</groupId>
  <artifactId>kotlin-csv-js</artifactId>
  <version>2.0.0</version>
</dependency>
```

### SNAPSHOT builds

Snapshots of the next development version are published to
[Sonatype Central Portal Snapshots](https://central.sonatype.com/repository/maven-snapshots/) from the active development branch.
SNAPSHOTs are unstable and may change at any time, but they let you try in-progress changes early.

Add the snapshots repository to your build, then declare the dependency with the corresponding `-SNAPSHOT` version
(see the active development branch's `build.gradle.kts` for the current value, for example `2.0.0-SNAPSHOT`).

#### Gradle (Kotlin DSL)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            mavenContent { snapshotsOnly() }
        }
    }
}
```

```kotlin
// build.gradle.kts
implementation("com.jsoizo:kotlin-csv:<VERSION>-SNAPSHOT")
```

## Quick start

The DSL builders return reusable, stateless instances; create them once and
share them across calls.

```kotlin
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.csvWriter
import com.jsoizo.kotlincsv.reader.CsvNullFieldIndicator
import com.jsoizo.kotlincsv.reader.readFromFile
import com.jsoizo.kotlincsv.reader.withHeader
import com.jsoizo.kotlincsv.writer.WriteQuoteMode
import com.jsoizo.kotlincsv.writer.writeToFile

val reader = csvReader()
val writer = csvWriter()
```

### Read

```kotlin
// From a String — eager
val rows: List<List<String>> = reader.readAll("a,b,c\nd,e,f")

// From a File — lambda owns the open resource and closes it on exit
reader.readFromFile(File("data.csv")) { rows ->
    rows.forEach { println(it) }
}

// With a header row (returns LinkedHashMap to preserve column order)
reader.readFromFile(File("data.csv")) { rows ->
    val records = rows.withHeader().toList()
    println(records.first()["id"])
}

// Nullable reads can distinguish "" from an unquoted empty field.
val nullableRows = csvReader {
    nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS
}.readAllNullable("\"empty\",\"null\"\n\"\",")
```

`reader.readFromFile(...)` accepts `String` paths, `kotlinx.io.files.Path`,
and (on JVM) `java.io.File`. The block receives a cold
`Sequence<List<String>>`; `take(n)` and friends short-circuit cleanly
without parsing the rest of the file. For in-memory streams use
`reader.read(source)` (commonMain `kotlinx.io.Source`) or
`reader.read(stream)` (JVM `java.io.InputStream`).

### Write

```kotlin
val rows = listOf(
    listOf("a", "b", "c"),
    listOf("d", "e", "f"),
)

// To a String — eager
val csv: String = writer.writeAll(rows)

// To a File
writer.writeToFile(rows, File("out.csv"))

// Nullable writes emit null as an unquoted empty field.
val nullableCsv = csvWriter {
    quoteMode = WriteQuoteMode.ALL
}.writeAllNullable(listOf(listOf(null, "", "value")))
```

`writer.writeToFile(...)` accepts `String` paths, `kotlinx.io.files.Path`,
and (on JVM) `java.io.File`. For in-memory streams use
`writer.write(rows, sink)` (commonMain `kotlinx.io.Sink`) or
`writer.write(rows, stream)` (JVM `java.io.OutputStream`). Both
`Sequence<List<String>>` and `List<List<String>>` are accepted as the row
source.

## Configuration

Reader and writer share a `CsvDialect` value object that holds the four
characters defining the CSV format itself: `delimiter`, `quoteChar`,
`escapeChar`, `lineTerminator`. Format-independent policies stay on the
respective `Config`.

```kotlin
val tsvReader = csvReader {
    dialect = CsvDialect.TSV
    skipEmptyLine = true
}

val customWriter = csvWriter {
    dialect = CsvDialect(delimiter = ';', escapeChar = '\\')
    quoteMode = WriteQuoteMode.ALL
}
```

| Reader option | Default | Description |
| --- | --- | --- |
| `dialect` | `CsvDialect.RFC4180` | Shared CSV format (delimiter / quote / escape / line terminator). |
| `skipEmptyLine` | `false` | Drop rows that are entirely empty before the field-count check. |
| `excessFieldsRowBehaviour` | `ERROR` | What to do when a row has more fields than the first row: `ERROR` / `IGNORE` / `TRIM`. |
| `insufficientFieldsRowBehaviour` | `ERROR` | What to do when a row has fewer fields: `ERROR` / `IGNORE` / `EMPTY_STRING`. |
| `nullFieldIndicator` | `NEITHER` | Which empty fields nullable reader APIs expose as `null`: `NEITHER` / `EMPTY_SEPARATORS` / `EMPTY_QUOTES` / `BOTH`. |

`CsvFieldNumDifferentException.rowNum` counts CSV rows after reader filters
such as `skipEmptyLine`; it is not a physical source line number.

Nullable reader APIs (`readNullable`, `readAllNullable`, and nullable I/O
overloads) return `String?` values. `withNullableHeader()` keeps header keys
as non-null `String`; a null-looking empty header is treated as the empty
header name `""`, while data values remain nullable.

| Writer option | Default | Description |
| --- | --- | --- |
| `dialect` | `CsvDialect.RFC4180` | Shared CSV format (delimiter / quote / escape / line terminator). |
| `outputLastLineTerminator` | `true` | Emit a trailing line terminator after the final row. |
| `quoteMode` | `CANONICAL` | When to wrap fields in `quoteChar`: `CANONICAL` (only when needed), `ALL`, or `NON_NUMERIC`. |

Charset is JVM-only and is passed as an argument on the I/O call:

```kotlin
reader.readFromFile(File("data.csv"), charset = "windows-31j") { it.toList() }
writer.writeToFile(rows, File("out.csv"), charset = "UTF-16LE")
```

`commonMain` and JS overloads are UTF-8 only.

kotlin-csv's JVM I/O does not throw when the charset cannot map a
character. `"windows-31j"` and `"Shift_JIS"` write `?` in its place. On
read, bytes the charset cannot decode become U+FFFD.

For Japanese text, use `"windows-31j"` (alias `"MS932"`) rather than Java's
`"Shift_JIS"` (alias `"SJIS"`):

- To read, no extra step is needed. `"windows-31j"` decodes every byte
  sequence that `"Shift_JIS"` decodes, plus Windows characters such as `①`,
  `髙`, and `№`.
- To write, first replace `〜` (U+301C), `−` (U+2212), `—` (U+2014), and
  `‖` (U+2016) with `～` (U+FF5E), `－` (U+FF0D), `―` (U+2015), and `∥`
  (U+2225). `"windows-31j"` writes the original four as `?`. The
  replacements produce the bytes that `"Shift_JIS"` writes for the
  originals. Text typed on macOS or read with `"Shift_JIS"` can contain the
  originals.

BOM stripping on read defaults to ON (matches Excel-produced files):

```kotlin
reader.readFromFile(File("data.csv"), options = CsvReadIoOptions(stripBom = false))

writer.writeToFile(rows, File("out.csv"), options = CsvWriteIoOptions(prependBom = true))
```

## Recipes

### Exchange files with Excel

Keep the default dialect, `CsvDialect.RFC4180`. If the recipient's Excel
uses `;` as the list separator, use `CsvDialect(delimiter = ';')`. The main
choice is the encoding. Pick it by what the recipient can read:

```kotlin
// UTF-8 with a BOM keeps every character.
writer.writeToFile(rows, "out.csv", options = CsvWriteIoOptions(prependBom = true))

// Windows-31J (JVM only) is for recipients that read only the Japanese Windows code page.
writer.writeToFile(rows, File("out.csv"), charset = "windows-31j")
```

Windows-31J writes characters such as `𠮷` and emoji as `?` without an
error. Replace `〜` (U+301C) and three similar characters first, as
described in [Configuration](#configuration). Do not combine
`charset = "windows-31j"` with `prependBom = true`. Windows-31J cannot
encode the BOM, so the file starts with `?`.

To read a CSV file saved by Excel, keep the default UTF-8 for a UTF-8 file
and pass `charset = "windows-31j"` for a Windows-31J file. The reader drops a
leading BOM by default.

### Exchange files with PostgreSQL `COPY`

With `FORMAT csv` and the default `NULL` option, PostgreSQL writes `NULL` as
an unquoted empty field and an empty string as `""`. To keep that
difference, set `nullFieldIndicator = EMPTY_SEPARATORS` and read with a
nullable API such as `readAllNullable`. Write nullable rows with
`WriteQuoteMode.ALL`. The default `CANONICAL` mode writes both `null` and
`""` as an unquoted empty field, so `COPY FROM` loads the empty strings as
`NULL`.

```kotlin
val pgReader = csvReader {
    nullFieldIndicator = CsvNullFieldIndicator.EMPTY_SEPARATORS
}
val pgWriter = csvWriter {
    dialect = CsvDialect(lineTerminator = "\n") // LF, as COPY TO STDOUT writes
    quoteMode = WriteQuoteMode.ALL
}

val csv = pgWriter.writeAllNullable(listOf(listOf("1", null, "")))
// "1",,""
pgReader.readAllNullable(csv) // [[1, null, ]]
```

## More

- **Migration from kotlin-csv 1.x**:
  see [V2_MIGRATION_GUIDE.md](./V2_MIGRATION_GUIDE.md).
- **API documentation**: hosted on javadoc.io —
  [latest (2.x)](https://javadoc.io/doc/com.jsoizo/kotlin-csv-jvm/latest/index.html) /
  [1.x (1.11.0)](https://javadoc.io/doc/com.jsoizo/kotlin-csv-jvm/1.11.0/index.html).
  Other 2.x versions are available via the version dropdown.
  To build locally, run `./gradlew dokkaGeneratePublicationHtml` — outputs
  HTML to `build/dokka/html/`.
- **Change Logs**: see [GitHub releases](https://github.com/jsoizo/kotlin-csv/releases).

## Miscellaneous

### 🤝 Contributing

Contributions, [issues](https://github.com/jsoizo/kotlin-csv/issues) and feature requests are welcome!
If you have questions, ask away in [Kotlin Slack's](https://kotlinlang.slack.com) `kotlin-csv` room.

### 💻 Development

```sh
git clone git@github.com:jsoizo/kotlin-csv.git
cd kotlin-csv
./gradlew check
```

### 📝 License

Copyright © 2024 [jsoizo](https://github.com/jsoizo).
This project is licensed under [Apache 2.0](LICENSE).

### Acknowledgments

This project was originally created by [@doyaaaaaken](https://github.com/doyaaaaaken). The initial work and contributions are greatly appreciated.
