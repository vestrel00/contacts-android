# Query blocked numbers

This library provides the `BlockedNumbersQuery` API that allows you to get blocked numbers.

An instance of the `BlockedNumbersQuery` API is obtained by,

```kotlin
val query = Contacts(context).blockedNumbers().query()
```

Note that blocked number queries will only work for privileged apps. For more info, read about
[Blocked numbers](/howto/about-blocked-numbers.md).

## A basic query

To get all of the blocked numbers,

```kotlin
val blockedNumbers = Contacts(context)
    .blockedNumbers()
    .query()
    .find()
```

## Ordering

To order resulting BlockedNumbers using one or more fields,

```kotlin
.orderBy(fieldOrder)
```

For example, to order blocked numbers by number,

```kotlin
.orderBy(BlockedNumbersFields.Number.asc())
```

String comparisons ignores case by default. Each orderBys provides `ignoreCase` as an optional
parameter.

Use `BlockedNumbersFields` to construct the orderBys.

## Limiting and offsetting

To limit the amount of blocked numbers returned and/or offset (skip) a specified number of 
blocked numbers,

```kotlin
.limit(limit)
.offset(offset)
```

For example, to only get a maximum 20 blocked numbers, skipping the first 20,

```kotlin
.limit(20)
.offset(20)
```

This is useful for pagination =)

> Note that it is recommended to limit the number of blocked numbers when querying to increase 
> performance and decrease memory cost.

## Executing the query

To execute the query,

```kotlin
.find()
```

## Cancelling the query

To cancel a query amid execution,

```kotlin
.find { returnTrueIfQueryShouldBeCancelled() }
```

The `find` function optionally takes in a function that, if it returns true, will cancel query
processing as soon as possible. The function is called numerous times during query processing to
check if processing should stop or continue. This gives you the option to cancel the query.

This is useful when used in multi-threaded environments. One scenario where this would be frequently
used is when performing queries as the user types a search text. You are able to cancel the current
query when the user enters new text.

For example, to automatically cancel the query inside a Kotlin coroutine when the coroutine is cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val blockedNumbers = query.find { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed when the `find` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](/howto/async-execution.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the query with permission

There are no permissions required for blocked numbers. However, there are privileges that must be 
acquired. For more info, read about [Blocked numbers](/howto/about-blocked-numbers.md).

## Using the `where` function to specify matching criteria

Use the `contacts.core.BlockedNumbersFields` combined with the extensions from `contacts.core.Where` to form
WHERE clauses. 

> This howto page will not provide a tutorial on database where clauses. It assumes that you know the basics. 
> If you don't know the basics, then search for [sqlite where clause](https://www.google.com/search?q=sqlite+where+clause). 

For example, to find blocked numbers that contains "555",

```kotlin
.where { Number contains "555" }
```

To get a list of blocked numbers by IDs,

```kotlin
.where { Id `in` blockedNumberIds }
```