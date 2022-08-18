# Query device owner Contact profile

This library provides the `ProfileQuery` API that allows you to get the device owner Profile Contact.

> ℹ️ There can be only one device owner Contact, which is either set (not null) or not yet set 
> (null). However, like other regular Contacts, the Profile Contact may have one or more
> RawContacts.

An instance of the `ProfileQuery` API is obtained by,

```kotlin
val query = Contacts(context).profile().query()
```

> ℹ️ If you want to get non-Profile Contacts, read 
> [Query contacts](./../basics/query-contacts.md) and
> [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

## A basic query

To get the profile Contact,

```kotlin
val profileContact = Contacts(context).profile().query().find().contact
```

## Specifying Accounts

To only include RawContacts associated with one of the given accounts,

```kotlin
.accounts(accounts)
```

For example, to include only RawContacts belonging to only one account,

```kotlin
.accounts(Account("john.doe@gmail.com", "com.google"))
```

> ℹ️ For more info, read [Query for Accounts](./../accounts/query-accounts.md).

The RawContacts returned will only belong to the specified accounts.

If no accounts are specified (this function is not called or called with no Accounts), then all
RawContacts are included in the returned Contact.

A null Account may be provided here, which results in RawContacts with no associated Account to be
included. RawContacts without an associated account are considered local contacts or device-only 
contacts, which are not synced.

For more info, read about [Local (device-only) contacts](./../entities/about-local-contacts.md).

> ℹ️ This may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Including only specific data

To include only the given set of fields (data) in each of the Profile Contact,

```kotlin
.include(fields)
```

For example, to only include email and name fields,

```kotlin
.include { Email.all + Name.all }
```

For more info, read [Include only certain fields for read and write operations](./../basics/include-only-desired-data.md).

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
        val profile = query.find { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed when the `find` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for
> listening to Contacts database changes.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return null.

> ℹ️ For API 22 and below, the permission "android.permission.READ_PROFILE" is also required but
> only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
> prior to installation instead of at runtime.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support
 
The `ProfilQuery` API supports custom data. For more info, read [Query custom data](./../customdata/query-custom-data.md).