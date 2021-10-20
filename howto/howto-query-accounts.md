# How do I query for Accounts?

This library provides the `AccountsQuery` API that allows you to retrieve `Account`s from the 
`AccountManager` or from the Contacts Provider RawContacts table.

To get all available accounts in the system,

```kotlin
val accounts = Accounts(context).query().allAccounts()
```

To get all available accounts in the system with an account type of "com.google",

```kotlin
val accounts = Accounts(context).query().accountsWithType("com.google")
```

To get the account for a given RawContact, 

```kotlin
val account = Accounts(context).query().accountFor(rawContact)
```

> The returned account may be null, indicating that the given RawContact is local (device-only) and 
> is not associated with an account. For more info, read 
> [How do I learn more about "local" (device-only) contacts?](/howto/howto-learn-more-about-local-contacts.md)

To get the accounts for more than one RawContact in a single query,

```kotlin
val accounts = Accounts(context).query().accountsFor(rawContacts)
```

> The list of accounts may contain nulls in case of local RawContacts. You are also able to retrieve
> the account for the specific RawContact in the given RawContacts list using the result.

## Cancelling the query

To cancel an `accountsFor` query amid execution,

```kotlin
.accountsFor { returnTrueIfQueryShouldBeCancelled() }
```

The `accountsFor` functions optionally takes in a function that, if it returns true, will cancel 
query processing as soon as possible. The function is called numerous times during query processing 
to check if processing should stop or continue. This gives you the option to cancel the query.

This is useful when used in multi-threaded environments. One scenario where this would be commonly
used is when performing queries as the user types a search text. You are able to cancel the current
query when the user enters new text.

For example, to automatically cancel the query inside a Kotlin coroutine when the coroutine is cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val accounts = query.accountsFor(rawContacts) { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed in the same thread as the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the extensions provided in the `async` module.
For more info, read [How do I use the async extensions to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for
> listening to Contacts database changes.

## Performing the query with permission

Queries using `allAccounts` or `accountsWithType` require the `android.permission.GET_ACCOUNTS`. If 
not granted, the query will do nothing and return an empty list.

Queries using `accountFor` or `accountsFor` require the `android.permission.READ_CONTACTS`. If 
not granted, the query will do nothing and return null or an empty list respectively.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions extensions to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)

You may, of course, use other permission handling libraries or just do it yourself =)