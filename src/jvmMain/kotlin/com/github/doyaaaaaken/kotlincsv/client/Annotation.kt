package com.github.doyaaaaaken.kotlincsv.client

import com.github.doyaaaaaken.kotlincsv.util.V2_MIGRATION_GUIDE_URL

@RequiresOptIn(
    message = "This API is experimental. It may be changed in the future without notice.",
    level = RequiresOptIn.Level.WARNING
)
@Deprecated(
    message = "v1 opt-in marker; v2.0 promotes the I/O extensions to stable API and no longer exposes " +
            "this annotation. See the migration guide: " + V2_MIGRATION_GUIDE_URL,
    level = DeprecationLevel.WARNING
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class KotlinCsvExperimental
