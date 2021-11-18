# How do I get the device owner Contact profile?

This library provides the `ProfileQuery` API that allows you to get the device own Contact profile.
There can be only one device owner, which is either set (not null) or not yet set (null).

An instance of the `ProfileQuery` API is obtained by,

```kotlin
val query = Contacts(context).profile().query()
```

To get the profile Contact,

```kotlin
val profile = contacts.profile().query().find()
```

## Including blank (raw) contacts

The API allows you to specify if you want to include blank (raw) contacts or not,

```kotlin
.includeBlanks(true|false)
```

For more info, read [How do I learn more about "blank" contacts?](/howto/howto-learn-more-about-blank-contacts.md).

## Specifying Accounts

To only include RawContacts associated with one of the given accounts,

```kotlin
.accounts(accounts)
```

For example, to include only RawContacts belonging to only one account,

```kotlin
.accounts(Account("john.doe@gmail.com", "com.google"))
```

> For more info, read [How do I query for Accounts?](/howto/howto-query-accounts.md).

The RawContacts returned will only belong to the specified accounts.

If no accounts are specified (this function is not called or called with no Accounts), then all
RawContacts are included in the returned Contact.

A null Account may be provided here, which results in RawContacts with no associated Account to be
included. RawContacts without an associated account are considered local contacts or device-only 
contacts, which are not synced.

For more info, read [How do I learn more about "local" (device-only) contacts?](/howto/howto-learn-more-about-local-contacts.md).

> Note that this may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Including only specific data

To include only the given set of fields (data) in each of the Profile Contact,

```kotlin
.include(fields)
```

For example, to only include email fields,

```kotlin
.include(Fields.Email.all)
```

For more info, read [How do I include only the data that I want?](/howto/howto-include-only-desired-data.md)

## Cancelling the query

To cancel a query amid execution,

```kotlin
.find { returnTrueIfQueryShouldBeCancelled() }
```

The `find` function optionally takes in a function that, if it returns true, will cancel query
processing as soon as possible. The function is called numerous times during query processing to
check if processing should stop or continue. This gives you the option to cancel the query.

For example, to automatically cancel the query inside a Kotlin coroutine when the coroutine is cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val contacts = query.find { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed when the `find` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for
> listening to Contacts database changes.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return null.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support
 
The `ProfilQuery` API supports custom data. For more info, read [How do I use query APIs with custom data?](/howto/howto-query-custom-data.md)