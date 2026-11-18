package io.github.rosyidputrar.kembar.core.common.dispatcher

import javax.inject.Qualifier

/** Coroutine dispatchers injected through Hilt so tests can replace them. */
enum class KembarDispatchers {
    Default,
    IO,
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: KembarDispatchers)

/** A process-wide scope for work that must outlive any single screen (e.g. DataStore). */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
