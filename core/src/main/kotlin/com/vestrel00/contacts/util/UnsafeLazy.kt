package com.vestrel00.contacts.util

/**
 * Shorthand for `lazy(LazyThreadSafetyMode.NONE) { ... }`.
 *
 * This is used internally to reduce consumer cold startup times. Everything that is more memory or
 * CPU intensive than lazy(LazyThreadSafetyMode.NONE) { ... } should use this.
 */
internal fun <T> unsafeLazy(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)