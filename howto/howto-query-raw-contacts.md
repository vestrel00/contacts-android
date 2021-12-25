# How do I get a list of RawContacts?

This library provides the `AccountsRawContactsQuery` API that allows you to get a list of 
RawContacts matching a specific search criteria.

More specifically, this query returns `BlankRawContact`s, which are RawContacts that contains no 
data (e.g. email, phone). It only contains critical information required for performing RawContact 
operations such as associating local RawContacts to an Account.

> For more info, read [How do I associate a local RawContacts to an Account?](/howto/howto-associate-device-local-raw-contacts-to-an-account.md)

An instance of the `AccountsRawContactsQuery` API is obtained by,

```kotlin
val query = Contacts(context).accounts().queryRawContacts()
```

## A basic query

To get all RawContacts as blanks, 

```kotlin
val rawContacts = Contacts(context).accounts().queryRawContacts().find()
```

## Specifying Accounts

To limit the search to only those RawContacts associated with one of the given accounts,

```kotlin
.accounts(accounts)
```

For example, to limit the search to contacts belonging to only one account,

```kotlin
.accounts(Account("john.doe@gmail.com", "com.google"))
```

> For more info, read [How do I query for Accounts?](/howto/howto-query-accounts.md)

If no accounts are specified (this function is not called or called with no Accounts), then all
RawContacts of Contacts are included in the search.

A null Account may be provided here, which results in RawContacts with no associated Account to be
included in the search. RawContacts without an associated account are considered local contacts or
device-only contacts, which are not synced.

For more info, read [How do I learn more about "local" (device-only) contacts?](/howto/howto-learn-more-about-local-contacts.md)

> Note that this may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Ordering

To order resulting RawContacts using one or more fields,

```kotlin
.orderBy(fieldOrder)
```

For example, to order RawContacts by account type,

```kotlin
.orderBy(RawContactsFields.AccountType.asc())
```

String comparisons ignores case by default. Each orderBys provides `ignoreCase` as an optional
parameter.

Use `RawContactsFields` to construct the orderBys.

## Limiting and offsetting

To limit the amount of RawContacts returned and/or offset (skip) a specified number of RawContacts,

```kotlin
.limit(limit)
.offset(offset)
```

For example, to only get a maximum 20 RawContacts, skipping the first 20,

```kotlin
.limit(20)
.offset(20)
```

This is useful for pagination =)

> Note that it is recommended to limit the number of RawContacts when querying to increase performance
> and decrease memory cost.

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
        val rawContacts = query.find { !isActive }
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
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)

You may, of course, use other permission handling libraries or just do it yourself =)

## RawContacts from more than one account in the same list

When you perform a query that returns groups from more than one account, you will get everything
in the same `BlankRawContactsList`. This list is just like any other `List` except it also provides 
an extra function that allows you to get a sublist with RawContacts belonging only to a particular 
account.

```kotlin
val rawContactsFromAccount = blankRawContactsList.from(account)
```

## Getting Contacts and RawContacts from BlankRawContacts

If you want to get the Contacts and all associated RawContacts and Data from a set of `BlankRawContact`s,

```kotlin
val contacts = Contacts(context)
    .query()
    .where { RawContact.Id `in` blankRawContactIds }
    .find()
```

> For more info, read [How do I get a list of contacts in a more advanced way?](/howto/howto-query-contacts-advanced.md)

If you need a more convenient way to convert the `BlankRawContact`s to `RawContacts`, use 
`BlankRawContactToRawContact` extensions. For more info, read [How do I use some miscellaneous extension functions to make my life easier?](/howto/howto-use-miscellaneous-extensions.md)

## Profile RawContacts

The `AccountsRawContactsQuery` API also supports querying the Profile (device owner) RawContacts. 
To get an instance of this API for Profile queries,

```kotlin
val query = Contacts(context).accounts().profile().queryRawContacts()
```

All queries will be limited to the Profile, whether it exists or not.

## Using the `where` function to specify matching criteria

Use the `contacts.core.RawContactsField` combined with the extensions from `contacts.core.Where` to 
form WHERE clauses. 

> This howto page will not provide a tutorial on database where clauses. It assumes that you know the basics. 
> If you don't know the basics, then search for [sqlite where clause](https://www.google.com/search?q=sqlite+where+clause). 

For example, to get all favorite RawContacts,

```kotlin
val favoriteRawContacts = Contacts(context)
    .accounts()
    .queryRawContacts()
    .where { Options.Starred equalTo true }
    .find()
```

To get a list of RawContacts with the given IDs,

```kotlin
val favoriteRawContacts = Contacts(context)
    .accounts()
    .queryRawContacts()
    .where { Id `in` rawContactIds }
    .find()
```

### Limitations

This library only provides basic WHERE functions. It does not cover the entirety of SQLite, though 
the community may add more over time <3

Furthermore, this library is constrained by rules and limitations set by the Contacts Provider and
the behavior of the native Contacts app. One such rule/limitation has resulted in this library not
providing WHERE functions such as `isNull` or `isNullOrEmpty` to prevent making misleading queries.