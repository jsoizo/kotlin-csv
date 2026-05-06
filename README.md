<h1 align="center">kotlin-csv</h1>

<p>
  <img alt="Version" src="https://img.shields.io/badge/version-1.10.0-blue.svg?cacheSeconds=2592000" />
  <a href="https://github.com/jsoizo/kotlin-csv/blob/master/LICENSE">
    <img alt="License: Apache License 2.0" src="https://img.shields.io/badge/License-Apache License 2.0-yellow.svg" target="_blank" />
  </a>
  <a href="https://codecov.io/gh/jsoizo/kotlin-csv">
    <img src="https://codecov.io/gh/jsoizo/kotlin-csv/branch/master/graph/badge.svg" alt="codecov" />
  </a>
  <a href="https://www.codefactor.io/repository/github/jsoizo/kotlin-csv">
    <img src="https://www.codefactor.io/repository/github/jsoizo/kotlin-csv/badge" alt="CodeFactor" />
  </a>
</p>

Pure Kotlin Multiplatform CSV reader and writer.

# Setup

### Gradle (Kotlin DSL)

```kotlin
implementation("com.jsoizo:kotlin-csv-jvm:1.10.0") // JVM
implementation("com.jsoizo:kotlin-csv-js:1.10.0")  // Kotlin/JS (Node.js)
```

### Gradle (Groovy DSL)

```groovy
implementation 'com.jsoizo:kotlin-csv-jvm:1.10.0' // JVM
implementation 'com.jsoizo:kotlin-csv-js:1.10.0'  // Kotlin/JS (Node.js)
```

### Maven

```xml
<dependency>
  <groupId>com.jsoizo</groupId>
  <artifactId>kotlin-csv-jvm</artifactId>
  <version>1.10.0</version>
</dependency>
<dependency>
  <groupId>com.jsoizo</groupId>
  <artifactId>kotlin-csv-js</artifactId>
  <version>1.10.0</version>
</dependency>
```

### [kscript](https://github.com/holgerbrandl/kscript)

```kotlin
@file:DependsOn("com.jsoizo:kotlin-csv-jvm:1.10.0")
@file:DependsOn("com.jsoizo:kotlin-csv-js:1.10.0")
```

# Quick start

The DSL builders return reusable, stateless instances; create them once and
share them across calls.

```kotlin
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.csvWriter

val reader = csvReader()
val writer = csvWriter()
```

## Read

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
```

`reader.readFromFile(...)` accepts `String` paths, `kotlinx.io.files.Path`,
and (on JVM) `java.io.File`. The block receives a cold
`Sequence<List<String>>`; `take(n)` and friends short-circuit cleanly
without parsing the rest of the file. For in-memory streams use
`reader.read(source)` (commonMain `kotlinx.io.Source`) or
`reader.read(stream)` (JVM `java.io.InputStream`).

## Write

```kotlin
val rows = listOf(
    listOf("a", "b", "c"),
    listOf("d", "e", "f"),
)

// To a String — eager
val csv: String = writer.writeAll(rows)

// To a File
writer.writeToFile(rows, File("out.csv"))
```

`writer.writeToFile(...)` accepts `String` paths, `kotlinx.io.files.Path`,
and (on JVM) `java.io.File`. For in-memory streams use
`writer.write(rows, sink)` (commonMain `kotlinx.io.Sink`) or
`writer.write(rows, stream)` (JVM `java.io.OutputStream`). Both
`Sequence<List<String>>` and `List<List<String>>` are accepted as the row
source.

# Configuration

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

| Writer option | Default | Description |
| --- | --- | --- |
| `dialect` | `CsvDialect.RFC4180` | Shared CSV format (delimiter / quote / escape / line terminator). |
| `outputLastLineTerminator` | `true` | Emit a trailing line terminator after the final row. |
| `quoteMode` | `CANONICAL` | When to wrap fields in `quoteChar`: `CANONICAL` (only when needed), `ALL`, or `NON_NUMERIC`. |

Charset is JVM-only and is passed as an argument on the I/O call:

```kotlin
reader.readFromFile(File("data.csv"), charset = "Shift_JIS") { it.toList() }
writer.writeToFile(rows, File("out.csv"), charset = "UTF-16LE")
```

`commonMain` and JS overloads are UTF-8 only.

BOM stripping on read defaults to ON (matches Excel-produced files):

```kotlin
reader.readFromFile(File("data.csv"), options = CsvReadIoOptions(stripBom = false))

writer.writeToFile(rows, File("out.csv"), options = CsvWriteIoOptions(prependBom = true))
```

# More

- **Migration from kotlin-csv 1.x**:
  see [V2_MIGRATION_GUIDE.md](./V2_MIGRATION_GUIDE.md).
- **API documentation**: generated by Dokka — `./gradlew dokkaHtml` outputs
  HTML to `build/dokka/html/`.
- **Change Logs**: see [GitHub releases](https://github.com/jsoizo/kotlin-csv/releases).

# Miscellaneous

## 🤝 Contributing

Contributions, [issues](https://github.com/jsoizo/kotlin-csv/issues) and feature requests are welcome!
If you have questions, ask away in [Kotlin Slack's](https://kotlinlang.slack.com) `kotlin-csv` room.

## 💻 Development

```sh
git clone git@github.com:jsoizo/kotlin-csv.git
cd kotlin-csv
./gradlew check
```

## Show your support

Give a ⭐️ if this project helped you!

## 📝 License

Copyright © 2024 [jsoizo](https://github.com/jsoizo).
This project is licensed under [Apache 2.0](LICENSE).

***
_This project is inspired ❤️ by [scala-csv](https://github.com/tototoshi/scala-csv)_

## Acknowledgments

This project was originally created by [@doyaaaaaken](https://github.com/doyaaaaaken). The initial work and contributions are greatly appreciated.
