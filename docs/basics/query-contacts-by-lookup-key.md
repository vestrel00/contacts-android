# Query contacts by lookup key

This library provides the `LookupQuery` API that uses the `ContactsContract.Contacts.CONTENT_LOOKUP_URI`
to get contacts using lookup keys, which are typically used in shortcuts or other long-term links 
to contacts.

> ℹ️ For more info about lookup keys, read about [Contact lookup key vs ID](./../entities/about-contact-lookup-key.md)

An instance of the `LookupQuery` API is obtained by,

```kotlin
val query = Contacts(context).lookupQuery()
```

> ℹ️ For a broader, and more AOSP Contacts app like query that allows partial matching, use the `BroadQuery` API, read [Query contacts](./../basics/query-contacts.md).

> ℹ️ For a more granular, advanced queries, use the `Query` API; [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

> ℹ️ For specialized matching of phone numbers and SIP addresses, use the `PhoneLookupQuery` API; [Query contacts by phone or SIP](./../basics/query-contacts-by-phone-or-sip.md).

> ℹ️ If you want to query RawContacts directly instead of Contacts, read [Query RawContacts](./../basics/query-raw-contacts.md).

> ℹ️ If you want to query Data directly instead of Contacts, read [Query specific data kinds](./../data/query-data-sets.md).

> ℹ️ If you want to get the device owner Contact Profile, read [Query device owner Contact profile](./../profile/query-profile.md).

## A basic query

To get the contact with the given lookup key,

```kotlin
val contacts = Contacts(context)
    .lookupQuery()
    .whereLookupKeyMatches(lookupKey)
    .find()
    .firstOrNull()
```

For optimization purposes, include the last known ID,

```kotlin
val contact = Contacts(context)
    .lookupQuery()
    .whereLookupKeyWithIdMatches(LookupQuery.LookupKeyWithId(lookupKey, contactId))
    .find()
    .firstOrNull()
```

> ℹ️ Note that if the lookup key or id is a reference to a linked Contact (a Contact with two or more
> constituent RawContacts), and the linked Contact is unlinked, then the query will return
> multiple Contacts. If you want to handle this scenario, do not use functions like `firstOrNull`.

## Specifying Accounts

To limit the search to only those contacts associated with one of the given accounts,

```kotlin
.accounts(accounts)
```

For example, to limit the search to contacts belonging to only one account,

```kotlin
.accounts(Account("john.doe@gmail.com", "com.google"))
```

> ℹ️ For more info, read [Query for Accounts](./../accounts/query-accounts.md).

The Contacts returned may still contain RawContacts / data that belongs to other accounts not
specified in the given accounts because Contacts may be made up of more than one RawContact from
different Accounts. This is the same behavior as the AOSP Contacts app.

If no accounts are specified (this function is not called or called with no Accounts), then all
RawContacts of Contacts are included in the search.

A null Account may be provided here, which results in RawContacts with no associated Account to be
included in the search. RawContacts without an associated account are considered local contacts or
device-only contacts, which are not synced.

For more info, read about [Local (device-only) contacts](./../entities/about-local-contacts.md).

> ℹ️ This may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Specifying Groups

To limit the search to only those RawContacts associated with at least one of the given groups,

```kotlin
.groups(groups)
```

For example, to limit the search to only friends,

```kotlin
.groups(friendsGroup)
```

> ℹ️ For more info, read [Query groups](./../groups/query-groups.md).

Contacts returned may still contain RawContacts / data that belongs to other groups not specified in
the given groups because Contacts may be made up of more than one RawContact from different Groups.
This is the same behavior as the AOSP Contacts app.

If no groups are specified (this function is not called or called with no Groups), then all
RawContacts of Contacts are included in the search.

> ℹ️ This may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Including only specific data

To include only the given set of fields (data) in each of the matching contacts,

```kotlin
.include(fields)
```

For example, to only include phone fields,

```kotlin
.include { Phone.all }
```

For more info, read [Include only certain fields for read and write operations](./../basics/include-only-desired-data.md).

### Optimize your queries

To optimize speed and minimize CPU and memory consumption, it is highly recommended that you only 
include fields you need.

For more info, read [Optimizing queries](./../basics/include-only-desired-data.md#optimizing-queries).

## Ordering

To order resulting Contacts using one or more fields,

```kotlin
.orderBy(fieldOrder)
```

For example, to order contacts by favorite/starred status such that favorite/starred contacts
appear first in the list AND order by display name primary in ascending order (from a to z ignoring
case),

```kotlin
.orderBy(
    ContactsFields.Options.Starred.desc(),
    ContactsFields.DisplayNamePrimary.asc()
)
```

String comparisons ignores case by default. Each orderBys provides `ignoreCase` as an optional
parameter.

Use `ContactsFields` to construct the orderBys.

> ℹ️ If you need to sort a collection of Contacts outside of a database query using any field (in
> addition to `ContactsFields`), use `contacts.core.util.ContactsComparator`.
> For more info, read [Convenience functions](./../other/convenience-functions.md).

## Limiting and offsetting

To limit the amount of contacts returned and/or offset (skip) a specified number of contacts, use 
the `limit` and `offset` functions;

```kotlin
.limit(limit)
.offset(offset)
```

For more info, read [Using limit and offset in queries](./../basics/limit-and-offset-queries.md).

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
        val contacts = query.find { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed when the `find` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the project roadmap, which includes APIs for
> listening to Contacts database changes.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)