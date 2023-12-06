# Query contacts by phone or SIP

This library provides the `PhoneLookupQuery` API that performs a highly optimized query using a 
phone number or SIP address. This will only match EXACT phone numbers or SIP addresses of different
formatting and variations. There is no partial matching. This is useful for dialer apps that want 
to implement caller IDs for incoming and outgoing calls.

An instance of the `PhoneLookupQuery` API is obtained by,

```kotlin
val query = Contacts(context).phoneLookupQuery()
```

> ℹ️ For a broader, and more AOSP Contacts app like query that allows partial matching, use the `BroadQuery` API, read [Query contacts](./../basics/query-contacts.md).

> ℹ️ For a more granular, advanced queries, use the `Query` API; [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

> ℹ️ If you want to query RawContacts directly instead of Contacts, read [Query RawContacts](./../basics/query-raw-contacts.md).

> ℹ️ If you want to query Data directly instead of Contacts, read [Query specific data kinds](./../data/query-data-sets.md).

> ℹ️ If you want to get the device owner Contact Profile, read [Query device owner Contact profile](./../profile/query-profile.md).

## A basic query

To get all contacts that have the exact phone number "555-555-5555",

```kotlin
val contacts = Contacts(context)
    .phoneLookupQuery()
    .whereExactlyMatches("555-555-5555")
    .find()
```

The above query will also match contacts that have the following formatting and variations of that
number, such as "5555555555", "(555) 555-5555", or "+1 (555) 555-5555" _regardless of the normalized
number stored in the database_. For more info about matching, read up on the 
[`match` function](./../basics/query-contacts-by-phone-or-sip.md#using-the-match-and-whereexactlymatches-functions-to-specify-matching-criteria)

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

## Custom data support
 
The `PhoneLookupQuery` API does not include custom data in the matching process. However, you may still
use the `include` function with custom data. For more info, read [Query custom data](./../customdata/query-custom-data.md).

##  Using the `match` and `whereExactlyMatches` functions to specify matching criteria

The `PhoneLookupQuery` API lets the Contacts Provider perform the search using its own custom matching
algorithm via the `whereExactlyMatches` function.

This will only match EXACT phone numbers or SIP addresses of different formatting and variations. 
There is no partial matching.

There are several different types of matching algorithms that can be used. The type is set via the
`match` function.

**Custom data are not included in the matching process!** To match custom data, use `Query`.

### Match.PHONE

Match phone numbers. This is useful in cases where you want to implement a caller ID function for 
incoming and outgoing calls. This is the default.

For example, if there are contacts with the following numbers;

- 123
- 1234
- 1234
- 12345

Searching for "123" will only return the one contact with the number "123". Searching for "1234" 
will return the contact(s) with the number "1234".

Additionally, this is able to match phone numbers with or without using country codes. For example, 
the phone number "+923123456789" (country code 92) will be matched using any of the following;
"03123456789", "923123456789", "+923123456789".

The reverse is partially true. For example, the phone number "03123456789" will be matched using
"03123456789" or "+923123456789" BUT will NOT be matched using "923123456789".

If a phone number is saved with AND without a country code, then only the contact with the number 
that matches exactly will be returned. For example, when numbers "+923123456789" and 
"03123456789" are saved, searching for "03123456789" will return only the contact with that exact 
number (NOT including the contact with "+923123456789").

> ℹ️ Matching is not strictly based on the `PhoneEntity.normalizedNumber` (E164 representation) if 
> it is not null. In cases where it is null, matching will be done strictly based on the 
> `PhoneEntity.number`.

> ⚠️ The matching process/results described here may differ across OEMs and/or Android versions.
> For more details, read https://github.com/vestrel00/contacts-android/issues/337#issuecomment-1843672903

### Match.SIP

Same as `Match.PHONE` except this matches SIP addresses instead of phone numbers.

> ⚠️ This is only available for API 21 and above. The `Match.PHONE` will be used for API versions 
> below 21 even if `Match.SIP` is specified.