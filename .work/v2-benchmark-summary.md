# kotlin-csv v2 benchmark summary

作成日: 2026-05-22

## 目的

v2クロージング判断用に、v1/v2比較の読み方と現時点の最終評価を1箇所にまとめる。詳細な調査ログは `benchmark/results/phase-a/` と `.work/pr-177-overview.md` に残し、このファイルは「今どの数値を見るべきか」を明確にするためのもの。

## 比較条件の妥当性

- v1 benchmark は `com.jsoizo:kotlin-csv-jvm:1.10.0`、v2 benchmark はこのリポジトリの現在実装を参照する。
- v1 / v2 は共通の `CsvDataGen` と `DatasetSpec` を使い、同じ seed で同じCSV本文を生成する。
- JMH設定は v1 / v2 の `build.gradle.kts` で揃っている。
- parity test は HARD dataset で read / write / header の同値性を確認している。
- v2はv1とAPI形状が異なるため、benchmarkは「同じ利用シナリオをv2の推奨APIで実装した比較」として読む。API呼び出し名の完全一致比較ではない。

## 最終評価

Reader I/O fast path 後、問題だった primary profile の long-tail は解消している。`readAllInputStream(MEDIUM)` は `thrpt x avgt` がほぼ1に戻り、平均時間も大きく改善した。

| 指標 | v2 before | v2 after |
| --- | ---: | ---: |
| Throughput | 0.00407 ops/ms | 0.00772 ops/ms |
| Average time | 947.30 ms/op | 129.59 ms/op |
| Error | +/- 457.98 ms | +/- 2.69 ms |
| `thrpt x avgt` | 約3 | 約1.00 |
| alloc/op | 469 MB/op | 435-456 MB/op |

結論:

- throughput / latency / long-tail: v2.0.0 リリース判断として良好。
- alloc/op: v1同等までは未達。`readAllInputStream(MEDIUM)` では v1 約336MB/opに対して v2 after 約456MB/opで、約1.36倍残る。
- v2.0.0 の受け入れ条件を「long-tail解消と実用性能改善」とするならクリア。「alloc/opもv1同等」とするなら follow-up が必要。

## 代表値

### Primary after

`benchmark/results/phase-a/v2-primary-full-after.json` からの代表値。

| workload | dataset | throughput | average time |
| --- | --- | ---: | ---: |
| `readAllString` | MEDIUM | 0.00855 ops/ms | 122.04 ms/op |
| `readAllInputStream` | MEDIUM | 0.00772 ops/ms | 129.59 ms/op |
| `readAllFile` | MEDIUM | 0.00740 ops/ms | 134.31 ms/op |
| `sequenceIterativeFile` | MEDIUM | 0.00756 ops/ms | 143.61 ms/op |
| `readAllWithHeader` | MEDIUM | 0.00708 ops/ms | 145.33 ms/op |

### GC alloc after

`benchmark/results/phase-a/v2-gcprof-read-after.json` と `benchmark/results/phase-a/v1-gcprof-read.json` からの代表値。

| workload | dataset | v1 alloc/op | v2 after alloc/op | v2/v1 |
| --- | --- | ---: | ---: | ---: |
| `readAllString` | MEDIUM | 355 MB | 119 MB | 0.33x |
| `readAllInputStream` | MEDIUM | 336 MB | 457 MB | 1.36x |
| `readAllFile` | MEDIUM | 336 MB | 457 MB | 1.36x |
| `sequenceIterativeFile` | MEDIUM | 335 MB | 455 MB | 1.36x |
| `readAllWithHeader` | MEDIUM | 539 MB | 242 MB | 0.45x |

## 未解決項目

- I/O reader の alloc/op はまだ v1同等ではない。`ParseStateMachine` reuse と per-char yield 排除で long-tailは解消したが、row / field の生成や snapshot の allocation は残っている。
- benchmark result は `benchmark/results/phase-a/CANDIDATES.md` に古い before 評価が残る。参照時はこの summary と `.work/pr-177-overview.md` を最終判断の入口にする。
- kotlinx-io backend は java.io backend よりCPUで約7-13%遅いが、v1/v2比較の主軸ではない。別issueで扱うのが妥当。

## 推奨判断

v2.0.0 は性能クロージング可能。ただし release note / issue comment では「long-tailは解消済み、I/O alloc/opのv1同等化はfollow-up」と明記する。
