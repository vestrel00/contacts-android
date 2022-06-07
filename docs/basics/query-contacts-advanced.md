# Query contacts (advanced)

This library provides the `Query` API that allows you to get a list of Contacts matching a specific 
search criteria. All RawContacts of matching Contacts are included in the resulting Contact 
instances.

This provides a great deal of granularity and customizations when providing matching criteria via
the `where` function.

An instance of the `Query` API is obtained by,

```kotlin
val query = Contacts(context).query()
```

> ℹ️ For a broader, and more native Contacts app like query, use the `BroadQuery` API, read [Query contacts](./../basics/query-contacts.md).

> ℹ️ If you want to query Data directly instead of Contacts, read [Query specific data kinds](./../data/query-data-sets.md).

> ℹ️ If you want to get the device owner Contact Profile, read [Query device owner Contact profile](./../profile/query-profile.md).

## An advanced query

To retrieve the first 5 contacts (including only the contact id, display name, and phone numbers in
the results) ordered by display name in descending order, matching ALL of these rules;

- a first name starting with "leo"
- has emails from gmail or hotmail
- lives in the US
- has been born prior to making this query
- is favorited (starred)
- has a nickname of "DarEdEvil" (case sensitive)
- works for Facebook
- has a note
- belongs to the account of "john.doe@gmail.com" or "john.doe@myspace.com"

```kotlin
val contacts = Contacts(context)
    .query()
    .where {
        (Name.GivenName startsWith "leo") and
        (Email.Address { endsWith("gmail.com") or endsWith("hotmail.com") }) and
        (Address.Country equalToIgnoreCase "us") and
        (Event { (Date lessThan Date().toWhereString()) and (Type equalTo EventEntity.Type.BIRTHDAY) }) and
        (Contact.Options.Starred equalTo true) and
        (Nickname.Name equalTo "DarEdEvil") and
        (Organization.Company `in` listOf("facebook", "FB")) and
        (Note.Note.isNotNullOrEmpty())
    }
    .accounts(
        Account("john.doe@gmail.com", "com.google"),
        Account("john.doe@myspace.com", "com.myspace"),
    )
    .include { setOf(
        Contact.Id,
        Contact.DisplayNamePrimary,
        Phone.Number
    ) }
    .orderBy(ContactsFields.DisplayNamePrimary.desc())
    .offset(0)
    .limit(5)
    .find()
```

## A basic query

This query API may also be used to make basic, simpler queries.

To get all contacts ordered by the primary display name,

```kotlin
val contacts = Contacts(context)
    .query()
    .orderBy(ContactsFields.DisplayNamePrimary.asc())
    .find()
```

To get all contacts with a phone number AND email,

```kotlin
val contacts = Contacts(context)
    .query()
    ...
    .where{ Phone.Number.isNotNullOrEmpty() and Email.Address.isNotNullOrEmpty() }
    .find()
```

> ℹ️ Phone numbers are a special case because the Contacts Provider keeps track of the existence of 
> a phone number for any given contact. Use `Contact.HasPhoneNumber equalTo true` instead for a 
> more optimized query.

To get a list of contacts with the given IDs,

```kotlin
val contacts = Contacts(context)
    .query()
    ...
    .where { Contact.Id `in` contactIds }
    .find()
```

To get a Contact by lookup key, read about [Contact lookup key vs ID](./../entities/about-contact-lookup-key.md).

## Including blank contacts

The API allows you to specify if you want to include blank contacts or not,

```kotlin
.includeBlanks(true|false)
```

For more info, read [Blank contacts](./../entities/about-blank-contacts.md).

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

## Including only specific data

To include only the given set of fields (data) in each of the matching contacts,

```kotlin
.include(fields)
```

For example, to only include email and name fields,

```kotlin
.include { Email.all + Name.all }
```

For more info, read [Include only certain fields for read and write operations](./../entities/include-only-desired-data.md).

### Specifying Groups

To limit the search to only those RawContacts associated with at least one of the given groups,

```kotlin
.where { GroupMembership.GroupId `in` groups.mapNotNull { it.id } }
```

> ℹ️ For more info, read [Query groups](./../groups/query-groups.md).

Contacts returned may still contain RawContacts / data that belongs to other groups not specified in
the given groups because Contacts may be made up of more than one RawContact from different Groups.
This is the same behavior as the native Contacts app.

If no groups are specified, then all RawContacts of Contacts are included in the search.

> ℹ️ This may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

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
 
The `Query` API supports custom data. For more info, read [Query custom data](./../customdata/query-custom-data.md).
     
## Using the `where` function to specify matching criteria

Use the `contacts.core.Fields` combined with the extensions from `contacts.core.Where` to form WHERE
clauses. 

> ℹ️ This docs page will not provide a tutorial on database where clauses. It assumes that you know the basics. 
> If you don't know the basics, then search for [sqlite where clause](https://www.google.com/search?q=sqlite+where+clause). 

For example, to get all contacts with a phone number AND email,

```kotlin
val contacts = Contacts(context)
    .query()
    ...
    .where { Phone.Number.isNotNullOrEmpty() and Email.Address.isNotNullOrEmpty() }
    .find()
```

To get a list of contacts with the given IDs,

```kotlin
val contacts = Contacts(context)
    .query()
    ...
    .where { Contact.Id `in` contactIds }
    .find()
```

### Performance

Using `where` may require one or more additional queries, internally performed by the API, which
increases the time it takes for the query to complete. Therefore, you should only use `where` if 
you actually need it.

For every usage of the `and` operator where the left-hand-side and right-hand-side are different 
data kinds, an internal database query is performed. This is due to the way the Data table is 
structured in relation to Contacts. For example,

```kotlin
Email.Address.isNotNull() and Phone.Number.isNotNull() and Address.FormattedAddress.isNotNull()
```

The above will require two additional internal database queries in order to simplify the query such 
that it can actually provide matching Contacts.

Using the `or` operator does not have this performance hit.

### Limitations

This library only provides basic WHERE functions. It does not cover the entirety of SQLite, though 
the community may add more over time <3

Furthermore, this library is constrained by rules and limitations set by the Contacts Provider and
the behavior of the native Contacts app. One such rule/limitation has resulted in this library not
providing WHERE functions such as `isNull` or `isNullOrEmpty` to prevent making misleading queries.

Removing a piece of existing data results in the deletion of the row in the Data table if that row 
no longer contains any meaningful data. This is the behavior of the native Contacts app. Therefore, 
querying for null fields is not possible. For example, there may be no Data rows that exist where 
the email address is null. Thus, a query to search for all contacts with no email addresses may 
return 0 contacts even if there are some contacts that do not have at least one email address.

If you want to match contacts that has no particular type of data, you will have to make two 
queries. One to get contacts that have that particular type of data and another to get contacts
that were not part of the first query results. For example,

```kotlin
val contactsWithEmails = query
    .include(Fields.Contact.Id)
    .where { Email.Address.isNotNullOrEmpty() }
    .find()

val contactIdsWithEmails = contactsWithEmails.mapNotNull { it.id }
val contactsWithoutEmails = query
    .where { Contact.Id notIn contactIdsWithEmails }
    .find()
```

There is a special case with phone numbers. The ContactsContract provides a field that is true if
the contact has at least one phone number; `Fields.Contact.HasPhoneNumber`. The phone number is the 
only kind of data that the ContactsContract provides with an indexed value such as this. The 
ContactsContract does NOT provide things like "hasEmail", "hasWebsite", etc. Regardless, this 
library provide functions to match contacts that "has at least one instance of a kind of data". 
The `HasPhoneNumber` field is not necessary to get contacts that have a phone number. However, this 
does provide an easy way to get contacts that have no phone numbers without having to make two 
queries. For example,

```kotlin
val contactsWithNoPhoneNumbers = query
    .where { Contact.HasPhoneNumber notEqualTo true }
    .find()
```