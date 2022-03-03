# Execute work outside of the UI thread using coroutines

This library provides extensions in the `async` module that allow you to execute all core API 
functions outside of the main, UI thread. These extensions use [Kotlin Coroutines][coroutines].

The extension functions are lightweight and mostly exist for Coroutine user's convenience. The 
extensions can be generalized in two categories; `withContext` and `async`. These use, you guessed
it, Kotlin Coroutine's [withContext][with-context] and [async][async] functions respectively.

For all core API functions that does blocking work in the call-site thread (e.g. query, insert,
update, and deletes), there is a corresponding `xxxWithContext` and `xxxAsync` extension function.

## Using `withContext` extensions

To perform an query, insert, update, and delete **in order (sequential)** outside the main UI thread,

```kotlin
launch {
    val queryResult = query.findWithContext()
    val insertResult = insert.commitWithContext()
    val updateResult = update.commitWithContext()
    val deleteResult = delete.commitWithContext()
}
```

For each invocation of `xxxWithContext`, the current coroutine suspends, performs the operation in 
the given `CoroutineContext` (default is `Dispatchers.IO` if not specified), then returns the
result.

Computations automatically stops if the parent coroutine scope / job is cancelled.

## Using `async` extensions

To perform an query, insert, update, and delete **in parallel** outside the main UI thread,

```kotlin
launch {
    val deferredQueryResult = query.findAsynct()
    val deferredInsertResult = insert.commitAsync()
    val deferredUpdateResult = update.commitAsync()
    val deferredDeleteResult = delete.commitAsync()
    awaitAll(deferredQueryResult, deferredInsertResult, deferredUpdateResult, deferredDeleteResult)
}
```

For each invocation of `xxxAsync`, a `CoroutineScope`is created  with the given `CoroutineContext`
(default is `Dispatchers.IO` if not specified), performs the operation in that scope, then returns 
the `Deferred` result.

Computations automatically stops if the parent coroutine scope / job is cancelled.

## Cancellations are supported

To cancel a query amid execution,

```kotlin
query.find { returnTrueIfQueryShouldBeCancelled() }
```

The find function optionally takes in a function that, if it returns true, will cancel query
processing as soon as possible. The function is called numerous times during query processing to
check if processing should stop or continue. This gives you the option to cancel the query.

One scenario where this would be frequently used is when performing queries as the user types a 
search text. You are able to cancel the current query when the user enters new text.

For example, to automatically cancel the query inside a Kotlin coroutine when the coroutine is
cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val contacts = query.find { !isActive }
    }
}
```

Or, using the coroutine extensions in the `async` module,

```kotlin
launch {
    val contacts = query.findWithContext()
}
```

> Most core API functions support cancellations, not just queries!

## Not compatible with Java

Unlike the `core` module, the `async` module is not compatible with Java because it requires Kotlin
Coroutines.

## These extensions are optional

You are free to use the core APIs however you want with whatever libraries or frameworks you want 
that works with Java such as Reactive, AsyncTask (hope not), WorkManager, or your own DIY solution.

## Extensions for RxJava and Flow are in the roadmap

If you prefer not to use Kotlin Coroutines and would rather use your own multi-threading mechanism, 
then you are free to use the `core` module without using the `async` module functions.

However, if you prefer to use something that comes with the library to ensure first-class support,
then you might be interested in waiting for extensions for [RxJava][rx] and [Kotlin Flow][flow]!

[coroutines]: https://kotlinlang.org./../coroutines-overview.html
[with-context]: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-context.html
[async]: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/async.html
[flow]: https://github.com/vestrel00/contacts-android/milestone/8
[rx]: https://github.com/vestrel00/contacts-android/milestone/9