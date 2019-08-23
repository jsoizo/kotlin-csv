<h1 align="center">Welcome to kotlin-csv 👋</h1>
<p>
  <img alt="Version" src="https://img.shields.io/badge/version-0.6.0-blue.svg?cacheSeconds=2592000" />
  <a href="https://github.com/doyaaaaaken/kotlin-csv/blob/master/LICENSE">
    <img alt="License: Apache License 2.0" src="https://img.shields.io/badge/License-Apache License 2.0-yellow.svg" target="_blank" />
  </a>
  <img alt="CircleCI" src="https://circleci.com/gh/doyaaaaaken/kotlin-csv/tree/master.svg?style=svg" />
  <a href="https://codecov.io/gh/doyaaaaaken/kotlin-csv">
    <img src="https://codecov.io/gh/doyaaaaaken/kotlin-csv/branch/master/graph/badge.svg" alt="codecov" />
  </a>
  <a href="https://www.codefactor.io/repository/github/doyaaaaaken/kotlin-csv">
    <img src="https://www.codefactor.io/repository/github/doyaaaaaken/kotlin-csv/badge" alt="CodeFactor" />
  </a>
</p>

> Kotlin DSL CSV Reader/Writer

# Usage

## Download

gradle kotlin DSL:
```
implementation("com.github.doyaaaaaken:kotlin-csv:0.6.0")
```

gradle groovy DSL:
```
implementation 'com.github.doyaaaaaken:kotlin-csv:0.6.0'
```

maven:
```
<dependency>
  <groupId>com.github.doyaaaaaken</groupId>
  <artifactId>kotlin-csv</artifactId>
  <version>0.6.0</version>
</dependency>
```

## Examples

### Reading examples

#### Simple case

You can read csv file by both String and java.io.File object.
```kotlin
val csvData: String = "a,b,c"
val rows: List<List<String>> = csvReader().readAll(csvData)

val file: File = File("test.csv")
val rows: List<List<String>> = csvReader().readAll(file)
```
If you want to get ``Sequence<List<String>>``, you can use `readAsSequence` method.

#### Customize

When you create CsvReader, you can choose read options.
```kotlin
// this is tsv reader's option
val tsvReader = csvReader {
    charset = Charsets.ISO_8859_1
    quoteChar = '"'
    delimiter = '\t'
    escapeChar = '\\'
}
```

### Writing examples

#### Simple case

You can write csv simply, only one line.
No need to call other methods. <br />
Also, **You don't have to call `use`, `close` and `flush` method.**
```kotlin
val rows = listOf(listOf("a", "b", "c"), listOf("d", "e", "f"))
csvWriter().open("test.csv") { writeAll(rows) }
```

You can also write csv file per each line.<br />
Also, **You don't have to call `use`, `close` and `flush` method.**
```kotlin
val row1 = listOf("a", "b", "c")
val row2 = listOf("d", "e", "f")
csvWriter().open("test.csv") { 
    writeRow(row1)
    writeRow(row2)
}
```

#### Customize

When you create CsvWriter, you can choose write options.
```kotlin
val writer = csvWriter {
    charset = Charsets.ISO_8859_1
    delimiter = '\t'
    nullCode = "NULL"
    lineTerminator = "\n"
    quote {
        mode = WriteQuoteMode.ALL
        char = '\''
    }
}
```


# Miscellaneous

## 🤝 Contributing

Contributions, issues and feature requests are welcome!<br />Feel free to check [issues page](https://github.com/doyaaaaaken/kotlin-csv/issues).<br />
If you have question, feel free to ask in [Kotlin slack's](https://kotlinlang.slack.com/) `kotlin-csv` room.

## 💻 Development

```
$ git clone git@github.com:doyaaaaaken/kotlin-csv.git
$ cd kotlin-csv
$ ./gradlew test
```

## Show your support

Give a ⭐️ if this project helped you!

## 📝 License

Copyright © 2019 [doyaaaaaken](https://github.com/doyaaaaaken).<br />
This project is [Apache License 2.0](https://github.com/doyaaaaaken/kotlin-csv/blob/master/LICENSE) licensed.

***
_This project is inspired ❤️ by [scala-csv](https://github.com/tototoshi/scala-csv)_

_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_
