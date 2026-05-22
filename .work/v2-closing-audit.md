# kotlin-csv v2 クロージング監査

作成日: 2026-05-22

## サマリ

`src`、テスト、README / migration guide / Dokka向け `Module.md`、benchmark を対象に、v2 リリース前の一貫性・不要ロジック・性能・スタイル・コメントを確認した。

監査時点でライブラリ本体に P0 の実装破綻は見つからなかった。`./gradlew check` は成功している。監査後、P0 / P1 / P2 のうちリリース前に固定したい仕様とドキュメント差分は対応済み。

優先度の意味:

- P0: コミット / リリース前に必ず対応
- P1: v2 リリース前に判断または修正推奨
- P2: v2.0.x 以降でもよい改善候補
- P3: メモ、軽微な整備、将来検討

## P0

### P0-1: `.env` が追加状態で、Maven Central 認証情報を含む

- 根拠: `git status --short` で `.env` が `A`。`.env` には `ORG_GRADLE_PROJECT_mavenCentralUsername` / `ORG_GRADLE_PROJECT_mavenCentralPassword` が含まれる。`.gitignore` は `.env` を無視していない。
- 影響: 誤ってコミット / push すると公開リポジトリに認証情報が漏れる。これはコード品質以前にリリースブロッカー。
- 推奨対応: `.env` をコミット対象から外し、`.gitignore` に `.env` / `.env.*` を追加する。既にリモートへ push していた場合は Maven Central 側の認証情報をローテーションする。
- 検証: `git status --short -- .env` で表示されないこと、または意図的に untracked であること。`git check-ignore .env` が成功すること。
- 対応メモ: `.env` は index から外し、`.gitignore` に `.env` / `.env.*` を追加した。

## P1

### P1-1: 未終端の quoted field が例外ではなく行ごと黙って捨てられる

- 根拠: `ParseStateMachine.getResult()` は `QUOTED_FIELD` で `null` を返す。`parseRows` / `parseRowsFromChunks` の EOF tail flush は `getResult()?.let { yield(it) }` なので、`"abc` は例外にならず空結果になる。テストも `unterminatedQuote_atEof_yieldsNoFinalRow` としてこの挙動を固定している。
- 関連箇所: `src/commonMain/.../ParseStateMachine.kt:176`, `src/commonMain/.../SequenceParser.kt:46`, `src/commonTest/.../SequenceParserTest.kt:203`, `src/commonTest/.../SequenceParserTest.kt:280`
- 影響: `CsvReader` の KDoc / `Module.md` は format error が `CsvParseFormatException` として表面化すると説明しているため、仕様説明と実挙動がずれる。破損CSVを検知したい利用者にはデータ欠落として見える。
- 推奨対応: v2 での正式仕様を決める。推奨は EOF 時の `QUOTE_START` / `QUOTED_FIELD` を `CsvParseFormatException` にすること。互換性のため維持するなら、README / Module.md / migration guide に「未終端quoteは最終行を捨てる」と明記する。
- 検証: String 経路と chunked I/O 経路の両方に、未終端quoteの期待挙動をテストとして追加 / 更新する。
- 対応メモ: EOF時に `QUOTE_START` / `QUOTED_FIELD` なら `CsvParseFormatException` を投げるようにし、String経路とchunked経路のテストを更新した。

### P1-2: `WriteQuoteMode.NON_NUMERIC` の `.` 判定が説明とずれる

- 根拠: `WriteQuoteMode.NON_NUMERIC` の KDoc は「digits with at most one `.`」を数値と説明している。一方 `isDecimalNumber()` は `.` だけでも `foundDot = true` になり、最後まで通って `true` を返す。
- 関連箇所: `src/commonMain/.../WriteQuoteMode.kt:14`, `src/commonMain/.../SequenceEncoder.kt:170`
- 影響: `.` が数値扱いで quote されない。仕様上「少なくとも1桁の数字が必要」なら実装バグ。逆に `.` を数値扱いするならドキュメントが誤り。
- 推奨対応: 数値の最小仕様を明文化する。推奨は `hasDigit` を追加して `.` 単独を non-numeric として quote すること。合わせて `.5` / `1.` / `.` / `-1` の期待値テストを追加する。
- 検証: `CsvWriterTest` に境界ケースを追加し、`./gradlew check`。
- 対応メモ: 実装は現状維持し、`NON_NUMERIC` は「digits と最大1つの dot だけで構成される token は quote しない」という単純な字句ルールとして明文化した。`.` / `.5` / `1.` / signed / exponent 風 token のテストを追加した。

### P1-3: 公開ドキュメントに存在しないAPI名と古い依存バージョンが残っている

- 根拠: `Module.md` は `reader.read(file)` / `reader.readAll(file)` を例示しているが、実装上は file/path は `readFromFile` / `readAllFromFile`。また JS / Node.js caveat が `kotlinx-io 0.7.0` のままだが、Version Catalog は `0.9.0`。
- 関連箇所: `Module.md:65`, `Module.md:80`, `Module.md:159`, `gradle/libs.versions.toml:4`
- 影響: Dokka 生成物のサンプルがコンパイルできない / 現在の依存前提とずれる。v2 は破壊的変更を伴うため、移行ドキュメントの信頼性に直結する。
- 推奨対応: `Module.md` のサンプルを `readFromFile` / `readAllFromFile` に統一し、kotlinx-io のバージョン表現を現在値または「利用中の kotlinx-io 版」に更新する。README のクイックスタートも extension import が必要なサンプルは import を補う。
- 検証: README / Module.md の主要サンプルを最小プロジェクトでコンパイルするか、少なくとも imports を含む形に揃える。
- 対応メモ: `Module.md` の file API サンプルを `readFromFile` / `readAllFromFile` に修正し、kotlinx-io 表記を `0.9.0` に更新した。README の quick start には reader / writer extension import を追加した。

### P1-4: benchmark の比較条件は概ね妥当だが、最終判断用の結果文書が分散している

- 根拠: v1 / v2 benchmark は同じ `CsvDataGen`、同じ `DatasetSpec`、同じ JMH 設定を使っている。parity test も read / write / header を HARD dataset で比較している。一方、`benchmark/results/phase-a/CANDIDATES.md` は reader fast path 前の「v2悪」評価を含み、`.work/pr-177-overview.md` は fast path 後の改善を説明している。
- 関連箇所: `benchmark/v1/build.gradle.kts:15`, `benchmark/v2/build.gradle.kts:16`, `benchmark/shared/.../DatasetSpec.kt:11`, `benchmark/parity/.../ParityReadTest.kt:31`, `.work/pr-177-overview.md:141`, `benchmark/results/phase-a/CANDIDATES.md:15`
- 影響: 「v1比較として適切か」という点では土台はよい。ただし、クロージング判断時に古い結果と新しい結果が混在し、どの数値が最終評価なのか誤読されやすい。
- 推奨対応: `.work` または `benchmark/results/phase-a` に最終版の benchmark summary を1つ作り、Before / After / v1 比較 / 未達項目を集約する。特に `readAllInputStream(MEDIUM)` は after で throughput は v1 を大きく上回るが、alloc は v2 約456MB/op、v1 約336MB/op でまだ約1.36倍残るため、受け入れ基準を「long-tail解消」か「alloc v1同等」かで明記する。
- 検証: 追加の JMH 実行は不要。既存 JSON と PR overview の数値を1つの表に整理する。
- 対応メモ: `.work/v2-benchmark-summary.md` を追加し、最終判断用の入口を作った。

## P2

### P2-1: `ParseStateMachine.getResult()` が one-shot で副作用を持つ

- 根拠: `getResult()` は `DELIMITER` / `FIELD` / `QUOTE_END` で `fields.add(...)` してから `fields.toList()` を返す。同じ状態で2回呼ぶと結果が変わる。現在の driver は1回だけ呼ぶため壊れてはいない。
- 関連箇所: `src/commonMain/.../ParseStateMachine.kt:176`
- 影響: 今後 driver を増やす場合に、`getResult()` が getter ではなく finalize 操作であることを知らないと重複 field を作る。
- 推奨対応: `finishRowOrNull()` のような名前へ変える、または finalize と snapshot を分ける。少なくとも KDoc に one-shot 契約を書く。
- 対応メモ: `getResult()` / `getFinalResult()` を `finishRow()` / `finishFinalRow()` に改名し、getterではなく行確定操作であることが分かる名前にした。

### P2-2: `skipEmptyLine` 有効時の field-count 例外 `rowNum` が物理行番号ではない

- 根拠: `applyPipeline()` が先に empty row を filter し、その後 `applyFieldCountPolicy()` 内で `rowNum++` する。空行を除いた後の論理行番号になる。
- 関連箇所: `src/commonMain/.../CsvReader.kt:33`, `src/commonMain/.../CsvReader.kt:45`
- 影響: 利用者がファイル上の行番号として `CsvFieldNumDifferentException.rowNum` を使うと、`skipEmptyLine = true` のとき位置がずれる。
- 推奨対応: 物理行番号を維持するか、論理行番号であることを明記する。リリース前に仕様だけでも固定する。
- 外部実装メモ:
  - Python `csvreader.line_num` は source iterator から読んだ行数で、返却レコード数とは別物として説明している: https://docs.python.org/3/library/csv.html#csv.csvreader.line_num
  - Apache Commons CSV は `CSVRecord.getRecordNumber()` と `CSVParser.getCurrentLineNumber()` を分けており、multi-line value では一致しないと明記している: https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVRecord.html
  - Ruby CSV の `lineno` は parsed / generated rows の数として説明され、`skip_blanks` は blank lines を入力から無視する option として扱われる: https://ruby-doc.org/stdlib-3.0.2/libdoc/csv/rdoc/CSV.html
- 判断: kotlin-csv v2 は物理行番号を追跡する公開APIを持っていないため、`CsvFieldNumDifferentException.rowNum` は CSV row number として固定する。`skipEmptyLine = true` の場合は filter 後のCSV行番号であり、物理 source line ではない。
- 対応メモ: README / `Module.md` / migration guide / exception KDoc に `rowNum` の意味を追記した。挙動は変更しない。

### P2-3: `CsvDialect` の validation が line terminator 系の矛盾を許す

- 根拠: validation は `delimiter != quoteChar`、`delimiter != escapeChar`、`lineTerminator.isNotEmpty()` のみ。reader は LF / CR / U+2028 / U+2029 / U+0085 を行終端として扱うが、delimiter にこれらを指定できる。
- 関連箇所: `src/commonMain/.../CsvDialect.kt:26`, `src/commonMain/.../ParseStateMachine.kt:31`
- 影響: `delimiter = '\n'` のような dialect は reader / writer の直感的な round-trip を壊しやすい。現状は delimiter 分岐が行終端分岐より先に評価される。
- 推奨対応: line terminator 系を delimiter / quote / escape に許すのか明文化する。許さないなら `CsvDialect` で reject する。
- 判断: reader が常に行終端として扱う文字は dialect token として許可しない。
- 対応メモ: `CsvDialect` が delimiter / quoteChar / escapeChar に LF / CR / U+2028 / U+2029 / U+0085 を受け取った場合、構築時に `IllegalArgumentException` を投げるようにした。`lineTerminator` 自体は writer 用なので、従来どおり CRLF / LF などを許可する。

### P2-4: I/O fast path 後も ASCII 大規模入力の allocation は v1 同等ではない

- 根拠: `v2-gcprof-read-after.json` では `readAllInputStream(MEDIUM)` が約456MB/op、`v1-gcprof-read.json` では約336MB/op。`.work/pr-177-overview.md` でも alloc は follow-up 対象と記録されている。
- 関連箇所: `.work/pr-177-overview.md:147`
- 影響: throughput / long-tail は大きく改善済みだが、「alloc/op を v1同等まで落とす」が受け入れ条件なら未達。
- 推奨対応: v2.0.0 の受け入れ条件を明文化する。alloc追跡を続けるなら、field list / row list / StringBuilder snapshot 周辺を次の候補にする。
- 対応メモ: `.work/v2-benchmark-summary.md` で「v2.0.0 は long-tail 解消を受け入れ基準にし、alloc の v1同等化は follow-up」と明記した。追加JMHは行わない。

## P3

### P3-1: コメントとスタイルの軽微な不一致

- `ParseStateMachine` に `@author` だけのKDoc、`Read character and change state`、`return parsed CSV Fields` のような説明的 / 旧式コメントが残る。
- `SequenceParser.kt` に `val swap = currentBuffer; currentBuffer = nextBuffer; nextBuffer = swap` の1行複数文がある。
- `SequenceEncoder.kt` に `else                  ->` の桁合わせスタイルが残る。
- `CsvWriterTest` の `// -------- basics --------` などは見出し用途で、プロダクションコードではないがグローバルコメント方針とはやや違う。

推奨対応: v2.0.0 前に大きく触る必要はない。別PRで `ParseStateMachine` のコメントだけ「why」中心に整理し、ついでに1行複数文と桁合わせを通常のKotlin styleへ寄せる。

### P3-2: ブランチ名 / リンク名の古い `master` 表記

- 根拠: CI は `main` を対象にしているが、README badge / POM license URL は `master` を指している。
- 関連箇所: `.github/workflows/build_and_test.yml:6`, `README.md:5`, `README.md:9`, `build.gradle.kts:139`
- 推奨対応: 実リポジトリのデフォルトブランチに合わせて `main` へ統一する。

### P3-3: `.claude/scheduled_tasks.lock` と `.tool-versions` の扱いを決める

- 根拠: `git status --short` で `.claude/scheduled_tasks.lock` と `.tool-versions` も追加状態。
- 影響: `.tool-versions` は開発環境固定として有用な可能性がある。一方 lock file は個人環境由来ならコミット不要。
- 推奨対応: `.tool-versions` は採用するなら意図を確認してコミット、lock file は ignore 対象にする。

## 良かった点

- v1 / v2 benchmark は同じデータ生成器と同じ JMH 基本設定を使っており、比較の土台は妥当。
- chunked reader の境界テストは CRLF、escape、double quote、surrogate pair を押さえている。
- property test が round-trip、quote soundness、field-count policy、skipEmptyLine、header auto rename をカバーしている。
- writer の lazy core と eager fast path の重複は、性能目的が明確で許容できる重複。
- `./gradlew check` は成功しており、現時点でテスト・KMP主要ターゲットのベースラインは安定している。

## 推奨の次アクション

1. P0 / P1 / P2 対応を含む最終 `./gradlew check` を通してからコミットする。
2. P3-1 は別PRでコメントと軽微なスタイルだけを整理する。
3. P3-2 / P3-3 はリポジトリ運用判断を確認してから扱う。
