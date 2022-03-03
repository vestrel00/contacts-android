# Query for Accounts

This library provides the `AccountsQuery` API that allows you to retrieve `Account`s from the
`AccountManager`.

An instance of the `AccountsQuery` API is obtained by,

```kotlin
val query = Contacts(context).accounts().query()
```

## A basic query

To get all available accounts in the system,

```kotlin
val accounts = Contacts(context).accounts().query()
    .find()
```

To get all available accounts in the system with an account type of "com.google" or "com.yahoo",

```kotlin
val accounts = Contacts(context).accounts().query()
    .withTypes("com.google", "com.yahoo")
    .find()
```

To get the account for a set of RawContacts,

```kotlin
val account = Contacts(context).accounts().query()
    .associatedWith(rawContacts)
    .find()
```

To get all available accounts in the system with an account type of "com.google" or "com.yahoo"
AND is associated with at least one of the given RawContacts,

```kotlin
val accounts = Contacts(context).accounts().query()
    .withTypes("com.google", "com.yahoo")
    .associatedWith(rawContacts)
    .find()
```

> RawContacts that are not associated with an Account are local to the device. For more info, read
> about [Local (device-only) contacts](/docs/entities/about-local-contacts.md).

## Account for each specified RawContact

When you perform a query that uses `associatedWith` without using `withTypes`, you are able to get
the `Account` for each of the `RawContact` specified.

```kotlin
val rawContactAccount = accounts.accountFor(rawContact)
```

This allows you to get the accounts for multiple RawContacts in one API call =)

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

For example, to automatically cancel the query inside a Kotlin coroutine when the coroutine is
cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val contacts = query.find { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed when the `find` function is invoked. The work is done in the same thread as the
call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in
the `async` module. For more info,
read [Execute work outside of the UI thread using coroutines](/docs/async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` and `android.permission.GET_ACCOUNTS`
permissions. If not granted, the query will do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module. For
more info, read [Permissions handling using coroutines](/docs/permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Profile accounts

The `AccountsQuery` API also supports querying the Profile (device owner) contact data. To get an
instance of this API for Profile queries,

```kotlin
val query = Contacts(context).accounts().profile().query()
```

All queries will be limited to the Profile, whether it exists or not.