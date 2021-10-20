# How do I get a list of contacts in a more advanced way?

This library provides the `Query` API, which returns a list of Contacts matching a specific search
criteria. All RawContacts of matching Contacts are included in the resulting Contact instances.

This provides a great deal of granularity and customizations when providing matching criteria via
the `where` function.

> For a broader, and more native Contacts app like query, use the `BroadQuery` API.
> For more info, read [How do I get a list of contacts in the simplest way?](/howto/howto-query-contacts.md).

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
- belongs to the account of "jerry@gmail.com" or "jerry@myspace.com"

```kotlin
val contacts = Contacts(context)
    .query()
    .where(
        (Fields.Name.GivenName startsWith "leo") and
                ((Fields.Email.Address endsWith "gmail.com") or (Fields.Email.Address endsWith "hotmail.com")) and
                (Fields.Address.Country equalToIgnoreCase "us") and
                ((Fields.Event.Date lessThan Date().toWhereString()) and (Fields.Event.Type equalTo Event.Type.BIRTHDAY)) and
                (Fields.Contact.Options.Starred equalTo true) and
                (Fields.Nickname.Name equalTo "DarEdEvil") and
                (Fields.Organization.Company `in` listOf("facebook", "FB")) and
                (Fields.Note.Note.isNotNullOrEmpty())
    )
    .accounts(
        Account("jerry@gmail.com", "com.google"),
        Account("jerry@myspace.com", "com.myspace"),
    )
    .include(Fields.Contact.Id, Fields.Contact.DisplayNamePrimary, Fields.Phone.Number, Fields.Phone.NormalizedNumber)
    .orderBy(ContactsFields.DisplayNamePrimary.desc())
    .offset(0)
    .limit(5)
    .find()
```

## A simple query

This query may also be used to make simpler queries.

To get all contacts with a phone number AND email,

```kotlin
val contacts = Contacts(context)
    .query()
    .where(Fields.Phone.Number.isNotNullOrEmpty() and Fields.Email.Address.isNotNullOrEmpty())
    .find()
```

## Including blank contacts

The API allows you to specify if you want to include blank contacts or not,

```kotlin
.includeBlanks(true|false)
```

For more info, read [How do I learn more about "blank" contacts?](/howto/howto-learn-more-about-blank-contacts.md).

## Specifying Accounts

To limit the search to only those RawContacts associated with one of the given accounts,

```kotlin
.accounts(accounts)
```

For example, to limit the search to contacts belonging to only one account.

```kotlin
.accounts(Account("jerry@gmail.com", "com.google")))
```

> For more info, read [How do I query for Accounts?](/howto/howto-query-accounts.md).

The Contacts returned may still contain RawContacts / data that belongs to other accounts not
specified in the given accounts because Contacts may be made up of more than one RawContact from
different Accounts. This is the same behavior as the native Contacts app.

If no accounts are specified (this function is not called or called with no Accounts), then all
RawContacts of Contacts are included in the search.

A null Account may be provided here, which results in RawContacts with no associated Account to be
included in the search. RawContacts without an associated account are considered local contacts or
device-only contacts, which are not synced.

For more info, read [How do I learn more about "local" (device-only) contacts?](/howto/howto-learn-more-about-local-contacts.md).

> Note that this may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Specifying Groups

To limit the search to only those RawContacts associated with at least one of the given groups,

```kotlin
.where(Fields.GroupMembership.GroupId `in` groups.mapNotNull { it.id })
```

> For more info, read [How do I retrieve groups?](/howto/howto-query-groups.md).

Contacts returned may still contain RawContacts / data that belongs to other groups not specified in
the given groups because Contacts may be made up of more than one RawContact from different Groups.
This is the same behavior as the native Contacts app.

If no groups are specified, then all RawContacts of Contacts are included in the search.

> Note that this may affect performance. This may require one or more additional queries, internally
> performed in this function, which increases the time required for the search. Therefore, you
> should only specify this if you actually need it.

## Including only specific data

To include only the given set of fields (data) in each of the matching contacts,

```kotlin
.include(fields)
```

For more info, read [How do I include only the data that I want?](/howto/howto-include-only-desired-data.md).

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
> For more info, read [How do I use some miscellaneous extension functions to make my life easier?](/howto/howto-use-miscellaneous-extensions.md).

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

This is useful when used in multi-threaded environments. One scenario where this would be commonly
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

To perform the work in a different thread, use the extensions provided in the `async` module.
For more info, read [How do I use the async extensions to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap, which includes APIs for
> listening to Contacts database changes.

## Performing the query with permission

Queries require the `android.permission.READ_CONTACTS` permission. If not granted, the query will 
do nothing and return an empty list.

To perform the query with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions extensions to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)

You may, of course, use other permission handling libraries or just do it yourself =)
     
## Blank Contacts and the `where` function

The `where` function is only used to query the Data table. Some contacts do not have any Data table 
rows. However, this library exposes some fields that belong to other tables, accessible via the 
Data table with joins;

- `Fields.Contact`
- `Fields.RawContact`

Using these fields in the where clause does not have any effect in matching blank Contacts or 
RawContacts simply because they have no Data rows containing these joined fields.

For more info, read [How do I learn more about "blank" contacts?](/howto/howto-learn-more-about-blank-contacts.md).

### Limitations

Blank RawContacts and blank Contacts do not have any rows in the Data table so a `where` clause that 
uses any fields from the Data table `Fields` will **exclude** blanks in the result (even if they are 
OR'ed). There are some joined fields that can be used to match blanks **as long as no other fields 
are in the where clause**;

- `Fields.Contact` enables matching blank Contacts. The result will include all RawContact(s)
  belonging to the Contact(s), including blank(s). Examples;

  - `Fields.Contact.Id equalTo 5`
  - `Fields.Contact.Id in listOf(1,2,3) and Fields.Contact.DisplayNamePrimary contains "a"`
  - `Fields.Contact.Options.Starred equalTo true`

- `Fields.RawContact` enables matching blank RawContacts. The result will include all Contact(s) 
  these belong to, including sibling RawContacts (blank and not blank). Examples;

  - `Fields.RawContact.Id equalTo 5`
  - `Fields.RawContact.Id notIn listOf(1,2,3)`

Blanks will not be included in the results even if they technically should **if** joined fields 
from other tables are in the `where`. In the below example, matching the `Contact.Id` to an 
existing blank Contact with Id of 5 will yield no results because it is joined by `Fields.Email`, 
which is not a part of `Fields.Contact`. It should technically return the blank Contact with Id of 
5 because the OR operator is used. However, because we internally need to query the Contacts table 
to match the blanks, a DB exception will be thrown by the Contacts Provider because 
`Fields.Email.Address` ("data1" and "mimetype") are columns from the Data table that do not exist 
in the Contacts table. The same applies to the `Fields.RawContact`.

- `Fields.Contact.Id equalTo 5 OR (Fields.Email.Address.isNotNull())`
- `Fields.RawContact.Id ... OR (Fields.Phone.Number...)`