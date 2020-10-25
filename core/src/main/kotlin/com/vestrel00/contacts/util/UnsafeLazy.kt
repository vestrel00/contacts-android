package com.vestrel00.contacts.util

/**
 * Shorthand for `lazy(LazyThreadSafetyMode.NONE) { ... }`.
 *
 * This is used internally to reduce consumer cold startup times.
 */
internal fun <T> unsafeLazy(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)