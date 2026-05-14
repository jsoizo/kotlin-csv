package com.github.doyaaaaaken.kotlincsv.client

@RequiresOptIn(
    message = "This API is experimental. It may be changed in the future without notice.",
    level = RequiresOptIn.Level.WARNING
)
@Deprecated(
    message = "v1 opt-in marker; v2.0 promotes the I/O extensions to stable API and no longer exposes this annotation.",
    level = DeprecationLevel.WARNING
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class KotlinCsvExperimental
