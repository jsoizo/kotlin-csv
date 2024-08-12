package com.jsoizo.kotlincsv.client

@RequiresOptIn(
    message = "This API is experimental. It may be changed in the future without notice.",
    level = RequiresOptIn.Level.WARNING
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class KotlinCsvExperimental
