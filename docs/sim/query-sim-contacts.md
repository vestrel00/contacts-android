# Query contacts in SIM card

This library provides the `SimContactsQuery` API that allows you to get contacts stored in the SIM
card.

An instance of the `SimContactsQuery` API is obtained by,

```kotlin
val query = Contacts(context).sim().query()
```

Note that SIM card queries will only work if there is a SIM card in the ready state. For more info, 
read [SIM card state](./../sim/about-sim-contacts.md#sim-card-state).

## A basic query

To get all of the contacts in the SIM card,

```kotlin
val simContacts = Contacts(context).sim().query().find()
```

## Limitations

Projections, selections, and order are not supported by the `IccProvider`. Therefore, we are unable
to provide `include`, `where`, `orderBy`, `limit`, and `offset` functions in our `SimContactsQuery`
API.

Due to all of these limitations, all queries will return all contacts in the SIM card. You may 
perform your own sorting and pagination if you wish.

> ℹ️ Depending on memory size, [SIM cards can hold 200 to 500+ contacts](https://www.quora.com/How-many-contacts-can-I-save-on-my-SIM-card).
> The most common being around 250. Most, if not all, SIM cards have less than 1mb memory (averaging
> 32KB to 64KB). Therefore, memory and speed should not be affected much by not being able to
> sort/order and paginate at the query level. 

For more info, read about [SIM Contacts](./../sim/about-sim-contacts.md)

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
        val simContacts = query.find { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed when the `find` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the project roadmap.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)