# Query RawContacts

This library provides the `RawContactsQuery` API that allows you to get a list of RawContacts 
matching a specific search criteria. Use this if you want to show RawContacts directly (something
that the Google Contacts app does) instead of Contacts that may consist of several linked RawContacts.

> ⚠️ The APIs for this have changed significantly since [version 0.3.0](https://github.com/vestrel00/contacts-android/discussions/218).
> For documentation for [version 0.2.4](https://github.com/vestrel00/contacts-android/releases/tag/0.2.4)
> and below, [visit this page (click me)](https://github.com/vestrel00/contacts-android/blob/0.2.4/docs/accounts/query-raw-contacts.md).

An instance of the `RawContactsQuery` API is obtained by,

```kotlin
val query = Contacts(context).rawContactsQuery()
```

> ℹ️ For a broader, and more AOSP Contacts app like query, use the `BroadQuery` API, read [Query contacts](./../basics/query-contacts.md).

> ℹ️ For specialized matching of phone numbers and SIP addresses, use the `PhoneLookupQuery` API; [Query contacts by phone or SIP](./../basics/query-contacts-by-phone-or-sip.md).

> ℹ️ If you want to get Contacts instead of RawContacts; [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

> ℹ️ If you want to query Data directly instead of Contacts, read [Query specific data kinds](./../data/query-data-sets.md).

> ℹ️ If you want to get the device owner Contact Profile, read [Query device owner Contact profile](./../profile/query-profile.md).

## A basic query

To get all RawContacts, 

```kotlin
val rawContacts = Contacts(context).rawContactsQuery().find()
```

## An advanced query

To retrieve the first 5 RawContacts in the given account that has at least one email, skipping the 
first 2, where the RawContact's display name starts with "a", ordered by the display name in 
ascending order (ignoring case),

```kotlin
val rawContacts = rawContactsQuery
    .rawContactsWhere(emptySet(), RawContactsFields.DisplayNamePrimary.isNotNullOrEmpty() )
    .where { Email.Address.isNotNullOrEmpty() }
    .orderBy(RawContactsFields.DisplayNamePrimary.asc())
    .limit(5)
    .offset(2)
    .find()
```

## Including only specific data

To include only the given set of fields (data) in each of the matching RawContacts,

```kotlin
.include(fields)
```

For example, to only include the RawContact's primary display name, and email and name fields,

```kotlin
.includeRawContactsFields(RawContactsFields.DisplayNamePrimary)
.include { Email.all + Name.all }
```

For more info, read [Include only certain fields for read and write operations](./../basics/include-only-desired-data.md).

## Specifying Accounts

To limit the search to only those RawContacts associated with one of the given accounts,

```kotlin
.rawContactsWhere(accounts, null)
```

For example, to limit the search to contacts belonging to only one account,

```kotlin
.rawContactsWhere(listOf(Account("john.doe@gmail.com", "com.google")), null)
```

> ℹ️ For more info, read [Query for Accounts](../accounts/query-accounts.md).

If no accounts are specified (this function is not called or called with no Accounts), then all
RawContacts of Contacts are included in the search.

A null Account may be provided here, which results in RawContacts with no associated Account to be
included in the search. RawContacts without an associated account are considered local contacts or
device-only contacts, which are not synced.

For more info, read about [Local (device-only) contacts](../entities/about-local-contacts.md).

> ℹ️ This may affect performance. This may require one or more additional queries, internally 
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
use the `limit` and `offset` functions;

```kotlin
.limit(limit)
.offset(offset)
```

For more info, read [Using limit and offset in queries](limit-and-offset-queries.md).

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
For more info, read [Execute work outside of the UI thread using coroutines](../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for
> listening to Contacts database changes.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support

The `RawContactsQuery` API supports custom data. For more info, read [Query custom data](./../customdata/query-custom-data.md).

## RawContacts from more than one account in the same list

When you perform a query that returns groups from more than one account, you will get everything
in the same list. This list is just like any other `List` except it also provides an extra function
that allows you to get a sublist with RawContacts belonging only to a particular account.

```kotlin
val rawContactsFromAccount = rawContactsList.from(account)
```

## Getting Contacts from RawContacts

If you want to get the Contacts and all associated RawContacts and Data from a set of `RawContact`s,

```kotlin
val contacts = Contacts(context)
    .query()
    .where { RawContact.Id `in` rawContactIds }
    .find()
```

> ℹ️ For more info, read [Query contacts (advanced)](query-contacts-advanced.md).

If you need a more convenient way to convert the `RawContact`s to `Contact`s, use 
`RawContactContact` extensions. For more info, read [Convenience functions](../other/convenience-functions.md).

## Profile RawContacts

The `RawContactsQuery` API also supports querying the Profile (device owner) RawContacts. 
To get an instance of this API for Profile queries,

```kotlin
val query = Contacts(context).profile().rawContactsQuery()
```

All queries will be limited to the Profile, whether it exists or not.

## Using the `where` and `rawContactsWhere` function to specify matching criteria

Use the `contacts.core.Fields` and `contacts.core.RawContactsFields` combined with the extensions 
from `contacts.core.Where` to form WHERE clauses. 

> ℹ️ This docs page will not provide a tutorial on database where clauses. It assumes that you know 
> the basics. If you don't know the basics, then search for [sqlite where clause](https://www.google.com/search?q=sqlite+where+clause). 

For example, to get a list of RawContacts with the given IDs,

```kotlin
val rawContacts = Contacts(context)
    .rawContactsQuery()
    .rawContactsWhere(emptyList(), RawContactsFields.Id `in` rawContactIds)
    // alterAOSPly, .where { RawContact.Id `in` rawContactIds }
    .find()
```

To get all favorited/starred RawContacts,

```kotlin
fun getAllFavoriteRawContacts(): List<RawContact> = Contacts(this)
    .rawContactsQuery()
    .rawContactsWhere(emptyList(), RawContactsFields.Options.Starred equalTo true)
    .find()
```

### Limitations

This library only provides basic WHERE functions. It does not cover the entirety of SQLite, though 
the community may add more over time <3

Furthermore, this library is constrained by rules and limitations set by the Contacts Provider and
the behavior of the AOSP Contacts app. One such rule/limitation has resulted in this library not
providing WHERE functions such as `isNull` or `isNullOrEmpty` to prevent making misleading queries.