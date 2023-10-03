# Query for Accounts

This library provides the `AccountsQuery` API that allows you to retrieve **visible** `Account`s 
from the `AccountManager` whose `Account.type` has a matching 
`android.content.SyncAdapterType.accountType` where the
`android.content.SyncAdapterType.authority` is equal to
`android.provider.ContactsContract.AUTHORITY`.

If you look at all of the Accounts returned by the `AccountManager.getAccounts` in your 3rd party
app, you might see the following [Account.type]s...

- "com.google" if you are signed into a Google account
- "com.google.android.gm.legacyimap" if you are signed into an Personal (IMAP) account
- "com.samsung.android.mobileservice" if the device is from Samsung

When you open the Google Contacts app (assuming that it is a 3rd party app that did not come 
pre-installed in the OS) and select an Account to save a new Contact to, you will notice that it
only allows you to choose between the Google account of local/device account. It does not show
the Personal (IMAP) account or the Samsung Account. The reason is (probably) because there is no 
sync adapter for Contacts for those accounts. This API filters such accounts for you because this 
API is specific to accounts relating to Contacts!

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
read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the project roadmap.

## Performing the query with permission

Queries require the `android.permission.GET_ACCOUNTS` permissions. If not granted, the query will 
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module. For
more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Limitations

Samsung (type "com.osp.app.signin"), Xiaomi (type "com.xiaomi"), and perhaps other OEMs do not allow
3rd party (non-system) apps (those that do not come pre-installed in the OS) to access their 
accounts. Your app does not have visibility on this accounts, unless this API is packaged as part 
of a custom Android OS, which would be super cool. Such accounts are not returned by this query. 
In a Samsung device, the Samsung Contacts app is able to show the Samsung account but the Google 
Contacts app cannot.