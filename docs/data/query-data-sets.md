# Query specific data kinds

This library provides the `DataQueryFactory` API that allows you to get a list of specific data kinds 
directly without having to get them from Contacts/RawContacts.

An instance of the `DataQueryFactory` API is obtained by,

```kotlin
val query = Contacts(context).data().query()
```

> ℹ️ To retrieve all kinds of data via Contacts/RawContacts, read 
> [Query contacts](./../basics/query-contacts.md),
> [Query contacts (advanced)](./../basics/query-contacts-advanced.md),
> [Query contacts by phone or SIP](./../basics/query-contacts-by-phone-or-sip.md),
> [Query RawContacts](./../basics/query-raw-contacts.md).

## Data queries

The `DataQueryFactory` API provides instances of `DataQuery` for every data kind in the library.

The full list of queries are defined in the `DataQueryFactory` interface. Here it is for reference,

```kotlin
val dataQueryFactory = Contacts(context).data().query()

val addressesQuery = dataQueryFactory.addresses()
val emailsQuery = dataQueryFactory.emails()
val eventsQuery = dataQueryFactory.events()
val groupMembershipsQuery = dataQueryFactory.groupMemberships()
val imsQuery = dataQueryFactory.ims()
val namesQuery = dataQueryFactory.names()
val nicknamesQuery = dataQueryFactory.nicknames()
val notesQuery = dataQueryFactory.notes()
val organizationsQuery = dataQueryFactory.organizations()
val phonesQuery = dataQueryFactory.phones()
val relationsQuery = dataQueryFactory.relations()
val sipAddressesQuery = dataQueryFactory.sipAddresses()
val websitesQuery = dataQueryFactory.websites()

// Photos are intentionally left out as it is internal to the library.
```

These query instances will allow you to query only specific data kinds from all contacts.

For example, to get all emails from all contacts,

```kotlin
val emails = Contacts(context).data().query().emails().find()
```

To get all websites with a ".net" extension from contacts with the given IDs,

```kotlin
val websites = Contacts(this)
    .data()
    .query()
    .websites()
    .where { (Website.Url endsWith ".net") and (Contact.Id `in` contactIds) }
    .find()
```

## Specifying Accounts

To limit the search to only those data associated with one of the given accounts,

```kotlin
.accounts(accounts)
```

For example, to limit the search to data belonging to only one account,

```kotlin
.accounts(Account("john.doe@gmail.com", "com.google"))
```

> ℹ️ For more info, read [Query for Accounts](./../accounts/query-accounts.md).

If no accounts are specified (this function is not called or called with no Accounts), then all
data are included in the search.

A null Account may be provided here, which results in RawContacts with no associated Account to be
included in the search. RawContacts without an associated account are considered local contacts or
device-only contacts, which are not synced.

For more info, read about [Local (device-only) contacts](./../entities/about-local-contacts.md).

> ℹ️ This may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Including only specific data

To include only the given set of fields in each of the matching data ,

```kotlin
.include(fields)
```

For example, to only include the given name and family name in a names query,

```kotlin
.include(Fields.Name.GivenName, Fields.Name.FamilyName)
```

For more info, read [Include only certain fields for read and write operations](./../basics/include-only-desired-data.md).

## Ordering

To order resulting data using one or more fields,

```kotlin
.orderBy(fieldOrder)
```

For example, to order emails by type first and then email address,

```kotlin
.orderBy(Fields.Email.Type.asc(), Fields.Email.Address.asc())
```

String comparisons ignores case by default. Each orderBys provides `ignoreCase` as an optional
parameter.

Use the corresponding fields in `Fields` to construct the orderBys.

## Limiting and offsetting

To limit the amount of data returned and/or offset (skip) a specified number of data, use the 
`limit` and `offset` functions;

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
        val data = query.find { !isActive }
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

## Profile data

The `DataQueryFactory` API (and its `DataQuery` instances) also supports querying the Profile 
(device owner) contact data. To get an instance of this API for Profile data queries,

```kotlin
val profileDataQueryFactory = Contacts(context).profile().data().query()
```

All queries will be limited to the Profile, whether it exists or not.

## Custom data support
 
The `DataQueryFactory` API (and its `DataQuery` instances) supports custom data. For more info, 
read [Query custom data](./../customdata/query-custom-data.md).

## Using the `where` function to specify matching criteria

Use the corresponding `contacts.core.Fields` combined with the extensions from `contacts.core.Where` 
to form WHERE clauses. 

> ℹ️ This docs page will not provide a tutorial on database where clauses. It assumes that you know 
> the basics. If you don't know the basics, then search for [sqlite where clause](https://www.google.com/search?q=sqlite+where+clause). 

For example, to get all nicknames from all contacts,

```kotlin
val nicknames = Contacts(context).data().query().nicknames().find()
```

To get all birthday events from all contacts,

```kotlin
val birthdayEvents = Contacts(this)
    .data()
    .query()
    .events()
    .where { Event.Type equalTo EventEntity.Type.BIRTHDAY }
    .find()
```

### Limitations

This library only provides basic WHERE functions. It does not cover the entirety of SQLite, though 
the community may add more over time <3

Furthermore, this library is constrained by rules and limitations set by the Contacts Provider and
the behavior of the AOSP Contacts app. One such rule/limitation has resulted in this library not
providing WHERE functions such as `isNull` or `isNullOrEmpty` to prevent making misleading queries.

Removing a piece of existing data results in the deletion of the row in the Data table if that row 
no longer contains any meaningful data. This is the behavior of the AOSP Contacts app. Therefore, 
querying for null fields is not possible. For example, there may be no Data rows that exist where 
the email address is null. Thus, a query to search for all emails where the address is null may
always return no results.