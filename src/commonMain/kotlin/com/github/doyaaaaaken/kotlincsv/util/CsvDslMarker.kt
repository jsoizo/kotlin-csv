package com.github.doyaaaaaken.kotlincsv.util

/**
 * @author doyaaaaaken
 */
@Deprecated(
    message = "v1 DSL marker; v2.0 uses untagged DSL builders and no longer ships this annotation. " +
            "See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
@DslMarker
annotation class CsvDslMarker
