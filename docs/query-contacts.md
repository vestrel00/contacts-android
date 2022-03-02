# Query contacts (broad)

This library provides the `BroadQuery` API that allows you to get the exact same search results
as the native Contacts app! This query lets the Contacts Provider perform the search using its own
custom matching algorithm via the `whereAnyContactDataPartiallyMatches` function. This type of
query is the basis of an app that does a broad search of the Contacts Provider. The technique is
useful for apps that want to implement functionality similar to the People app's contact list screen.

An instance of the `BroadQuery` API is obtained by,

```kotlin
val query = Contacts(context).broadQuery()
```

> For a more granular, advanced queries, use the `Query` API.
> For more info, read [Query contacts (advanced)](/docs/query-contacts-advanced.md).

> If you want to query Data directly instead of Contacts, read [Query specific data kinds](/docs/data/query-data-sets.md).

> If you want to get the device owner Contact Profile, read [Query device owner Contact profile](/docs/profile/query-profile.md).

## A basic query

To get all contacts ordered by the primary display name,

```kotlin
val contacts = Contacts(context)
    .broadQuery()
    .orderBy(ContactsFields.DisplayNamePrimary.asc())
    .find()
```

To get all contacts that have any data that at least partially matches a given `searchText`,

```kotlin
val contacts = Contacts(context)
    .broadQuery()
    ...
    .whereAnyContactDataPartiallyMatches(searchText)
    .find()
```

## Including blank contacts

The API allows you to specify if you want to include blank contacts or not,

```kotlin
.includeBlanks(true|false)
```

For more info, read about [Blank contacts](/docs/entities/about-blank-contacts.md).

## Specifying Accounts

To limit the search to only those contacts associated with one of the given accounts,

```kotlin
.accounts(accounts)
```

For example, to limit the search to contacts belonging to only one account,

```kotlin
.accounts(Account("john.doe@gmail.com", "com.google"))
```

> For more info, read [Query for Accounts](/docs/accounts/query-accounts.md).

The Contacts returned may still contain RawContacts / data that belongs to other accounts not
specified in the given accounts because Contacts may be made up of more than one RawContact from
different Accounts. This is the same behavior as the native Contacts app.

If no accounts are specified (this function is not called or called with no Accounts), then all
RawContacts of Contacts are included in the search.

A null Account may be provided here, which results in RawContacts with no associated Account to be
included in the search. RawContacts without an associated account are considered local contacts or
device-only contacts, which are not synced.

For more info, read about [Local (device-only) contacts](/docs/entities/about-local-contacts.md).

> Note that this may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Specifying Groups

To limit the search to only those RawContacts associated with at least one of the given groups,

```kotlin
.groups(groups)
```

For example, to limit the search to only favorites,

```kotlin
.groups(favoritesGroup)
```

> For more info, read [Query groups](/docs/groups/query-groups.md).

Contacts returned may still contain RawContacts / data that belongs to other groups not specified in
the given groups because Contacts may be made up of more than one RawContact from different Groups.
This is the same behavior as the native Contacts app.

If no groups are specified (this function is not called or called with no Groups), then all
RawContacts of Contacts are included in the search.

> Note that this may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Including only specific data

To include only the given set of fields (data) in each of the matching contacts,

```kotlin
.include(fields)
```

For example, to only include email and name fields,

```kotlin
.include { Email.all + Name.all }
```

For more info, read [Include only certain fields for read and write operations](/docs/entities/include-only-desired-data.md).

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

> If you need to sort a collection of Contacts outside of a database query using any field (in
> addition to `ContactsFields`), use `contacts.core.util.ContactsComparator`.
> For more info, read [Convenience functions](/docs/util/convenience-functions.md).

## Limiting and offsetting

To limit the amount of contacts returned and/or offset (skip) a specified number of contacts,

```kotlin
.limit(limit)
.offset(offset)
```

For example, to only get a maximum 20 contacts, skipping the first 20,

```kotlin
.limit(20)
.offset(20)
```

This is useful for pagination =)

> Note that it is recommended to limit the number of contacts when querying to increase performance
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
        val contacts = query.find { !isActive }
    }
}
```

## Performing the query asynchronously

Queries are executed when the `find` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](/docs/async/async-execution.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for
> listening to Contacts database changes.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](/docs/permissions/permissions-handling.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support
 
The `BroadQuery` API does not include custom data in the matching process. However, you may still
use the `include` function with custom data. For more info, read [Query custom data](/docs/customdata/query-custom-data.md).

##  Using the `whereAnyContactDataPartiallyMatches` function to specify matching criteria

The `BroadQuery` API lets the Contacts Provider perform the search using its own custom matching
algorithm via the `whereAnyContactDataPartiallyMatches` function.

Most, but not all, Contact data are included in the matching process. Some are not probably
because some data may result in unintentional matching.

> See `AbstractDataFieldSet.forMatching` documentation on all the fields that are included in this match.

**Custom data are not included in the matching process!** To match custom data, 
read [Query contacts (advanced)](/docs/query-contacts-advanced.md).

Data matching is more sophisticated under the hood than `Query`. The Contacts Provider matches parts
of several types of data in segments. For example, a Contact having the email "hologram@gram.net"
will be matched with the following texts;

- h
- HOLO
- @g
- @gram.net
- gram@
- net
- holo.net
- hologram.net

But will NOT be matched with the following texts;

- olo
- @
- gram@gram
- am@gram.net

Similarly, a Contact having the name "Zack Air" will be matched with the following texts;

- z
- zack
- zack, air
- air, zack
- za a
- , z
- , a
- ,a

But will NOT be matched with the following texts;

- ack
- ir
- ,

Another example is a Contact having the note "Lots   of   spa        ces." will be matched with
the following texts;

- l
- lots
- lots of
- of lots
- ces spa       lots of.
- lo o sp ce . . . . .

But will NOT be matched with the following texts;

- .
- ots

Several types of data are matched in segments. E.G. A Contact with display name "Bell Zee" and
phone numbers "987", "1 23", and "456" will be matched with "be bell ze 9 123 1 98 456".

Matching is **case-insensitive** (case is ignored)