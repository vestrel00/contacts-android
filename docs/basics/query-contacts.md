# Query contacts

This library provides the `BroadQuery` API that allows you to get the exact same search results
as the native Contacts app! This query lets the Contacts Provider perform the search using its own
custom matching algorithm via the `wherePartiallyMatches` function. This type of query is the basis
of an app that does a broad search of the Contacts Provider. The technique is useful for apps that
want to implement functionality similar to the People app's contact list screen.

An instance of the `BroadQuery` API is obtained by,

```kotlin
val query = Contacts(context).broadQuery()
```

> ℹ️ For a more granular, advanced queries, use the `Query` API; [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

> ℹ️ If you want to query Data directly instead of Contacts, read [Query specific data kinds](./../data/query-data-sets.md).

> ℹ️ If you want to get the device owner Contact Profile, read [Query device owner Contact profile](./../profile/query-profile.md).

## A basic query

To get all contacts ordered by the primary display name,

```kotlin
val contacts = Contacts(context)
    .broadQuery()
    .orderBy(ContactsFields.DisplayNamePrimary.asc())
    .find()
```

To get all contacts that have any data (e.g. name, email, phone, address, organization, note, etc) 
that at least partially matches a given `searchText`,

```kotlin
val contacts = Contacts(context)
    .broadQuery()
    .wherePartiallyMatches(searchText)
    .find()
```

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
different Accounts. This is the same behavior as the native Contacts app.

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

For example, to limit the search to only favorites,

```kotlin
.groups(favoritesGroup)
```

> ℹ️ For more info, read [Query groups](./../groups/query-groups.md).

Contacts returned may still contain RawContacts / data that belongs to other groups not specified in
the given groups because Contacts may be made up of more than one RawContact from different Groups.
This is the same behavior as the native Contacts app.

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

For example, to only include email and name fields,

```kotlin
.include { Email.all + Name.all }
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

> ℹ️ It is recommended to limit the number of contacts when querying to increase performance and 
> decrease memory cost.

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

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for
> listening to Contacts database changes.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support
 
The `BroadQuery` API does not include custom data in the matching process. However, you may still
use the `include` function with custom data. For more info, read [Query custom data](./../customdata/query-custom-data.md).

##  Using the `match` and `wherePartiallyMatches` functions to specify matching criteria

The `BroadQuery` API lets the Contacts Provider perform the search using its own custom matching
algorithm via the `wherePartiallyMatches` function.

There are several different types of matching algorithms that can be used. The type is set via the
`match` function.

Matching is **case-insensitive** (case is ignored).

**Custom data are not included in the matching process!** To match custom data, use `Query`.

### Match.ANY

Most, but not all, Contact data are included in the matching process.
E.G. name, email, phone, address, organization, note, etc.

Use this if you want to get the same results when searching contacts using the AOSP Contacts app 
and the Google Contacts app.

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

Another example is a Contact having the note "Lots   of   spa        ces." will be
matched with the following texts;

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

### Match.PHONE

Only phones or (contact display name + any phones) are included in the matching process.

Use this if you want to get contacts that have a matching phone number or matching
(`Contact.displayNamePrimary` + any phone number).

If you are attempting to matching contacts with phone numbers using `Query`, then you will most
likely find it to difficult and tricky because the normalizedNumber could be null and matching
formatted numbers (e.g. (718) 737-1991) would require some special regular expressions. This match
might just be what you need =)

Only the `Contact.displayNamePrimary` and the phone number/normalizedNumber are included in the
matching process.

For example, a contact with `Contact.displayNamePrimary` of "Bob Dole" and phone number
"(718) 737-1991" (regardless of the value of normalizedNumber) will be matched with the following
texts;

- 718
- 7187371991
- 7.1-8.7-3.7-19(91)
- bob
- dole

Notice that "bob" and "dole" will trigger a match because the display name matches and the contact
has a phone number.

The following texts will NOT trigger a match because the comparison begins at the beginning of the
string and not in the middle or end;

- 737
- 1991

### Match.EMAIL

Only emails or (contact display name + any emails) are included in the matching process.

Only the `Contact.displayNamePrimary` and the email address are included in the matching process.

For example, the search text "bob" will match the following contacts;

- Robert Parr (bob@incredibles.com)
- Bob Parr (incredible@android.com)

Notice that the contact Bob Parr is also matched because the display name matches and an email
exist (even though it does not match).

The following search texts will NOT trigger a match because the comparison begins at the beginning
of the string and not in the middle or end;

- android
- gmail
- @
- .com

------------------------

## Developer notes (or for advanced users)

Matching only by phone number or email address is possible thanks to the following filter Uris
defined in `ContactsContract`, which exist for this specific purpose.

```kotlin
ContactsContract {
    Contacts { CONTENT_FILTER_URI } // Default used by BroadQuery
    CommonDataKinds {
        Phone { CONTENT_FILTER_URI }
        Email { CONTENT_FILTER_URI }
    }
} 
```

**These special filter URIs are only available for the phone and email common data kinds.**

Note that the `EMAIL` and `PHONE` additionally matches the contact display name.

### Comparison table

I've done some preliminary testing on the differences between the different matching/filter 
algorithms. So, given the following contacts...

1. Display name: Robert Parr
    - Email: bob@incredibles.com
2. Display name: Bob Parr
    - Email: incredible@android.com
3. Display name: Bob Dole
    - Phone: (718) 737-1991
4. Display name: vestrel00@gmail.com
    - Email: vestrel00@gmail.com
5. Display name: 646-123-4567
    - Phone: 646-123-4567
6. Display name: Secret agent.
    - Address: Dole street
    - Company: 718
    - Note: Agent code is 646000. His skills are incredible!

Here are some search terms followed by matching contacts based on the type of `Match` used.

| **Search term**    | **ANY** | **PHONE** | **EMAIL** |
|--------------------|---------|-----------|-----------|
| bob                | 1, 2, 3 | 3         | 1, 2      |
| incredible         | 1, 2, 6 |           | 2         |
| android            | 2       |           |           |
| gmail              | 4       |           |           |
| .com               | 1, 2, 4 |           |           |
| @                  |         |           |           |
| 7187371991         | 3       | 3         |           |
| 7.1-8.7-3.7-19(91) | 3       | 3         |           |
| 646                | 5, 6    | 5         |           |
| 646-646            | 6       |           |           |
| 718                | 3, 6    | 3         |           |
| 1991               |         |           |           |
| 4567               |         |           |           |
| 000                |         |           |           |
| dole               | 3, 6    | 3         |           |

The above table gives us some insight on how sophisticated the matching (or search / indexing) 
algorithm is.

For the search term "bob",

- PHONE matches contact 3.
    - Display name matches and contact has a phone even though it does match.
- EMAIL matches contact 1, 2.
    - 1 has a matching email "bob". 2 is also matched because the name matches even though the email
      does not. On the other hand, 3 is NOT matched even though the name matches because 3 has no
      email. Adding an email to 3 will cause 3 to be matched.

For the search term "incredible",

- EMAIL matches 2 (incredible@android.com) but NOT 1 (bob@incredibles.com).
    - This means that email matching does not use `contains` but rather a form of `startsWith`.

**TLDR**

- ANY matches any contact data; name, email, phone, address, organization, note, etc.
- EMAIL matches emails or (display name + any email)
- PHONE matches phones or (display name + any phone)
- EMAIL and PHONE matching is NOT as simple as using the `Query` API
  with `.where { [Email|Phone] contains searchTerm }`