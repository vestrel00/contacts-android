# Insert the device owner Contact profile

This library provides the `ProfileInsert` API that allows you to insert one or more RawContacts and 
Data. 

> Note that there can be only one device owner Contact, which is either set (not null) or not yet
> set (null). However, like other regular Contacts, the Profile Contact may have one or more
> RawContacts.

An instance of the `ProfileInsert` API is obtained by,

```kotlin
val insert = Contacts(context).profile().insert()
```

> If you want to create/insert non-Profile Contacts, read [Insert contacts](/docs/insert-contacts.md).

## A basic insert

To create/insert a raw contact with a name of "John Doe" who works at Amazon with a work email of
"john.doe@amazon.com" (in Kotlin),

```kotlin
val insertResult = Contacts(context)
    .profile()
    .insert()
    .rawContacts(NewRawContact().apply {
        name = NewName().apply {
            givenName = "John"
            familyName = "Doe"
        }
        organization = NewOrganization().apply {
            company = "Amazon"
            title = "Superstar"
        }
        emails.add(NewEmail().apply {
            address = "john.doe@amazon.com"
            type = EmailEntity.Type.WORK
        })
    })
    .commit()
```

Or alternatively, in a more Kotlinized style using named arguments,

```kotlin
val insertResult = Contacts(context)
    .profile()
    .insert()
    .rawContacts(NewRawContact(
        name = NewName(
            givenName = "John",
            familyName = "Doe"
        ),
        organization = NewOrganization(
            company = "Amazon",
            title = "Superstar"
        ),
        emails = mutableListOf(NewEmail(
            address = "john.doe@amazon.com",
            type = EmailEntity.Type.WORK
        ))
    ))
    .commit()
```

Or alternatively, using extension functions,

```kotlin
val insertResult = Contacts(context)
    .profile()
    .insert()
    .rawContact {
        setName {
            givenName = "John"
            familyName = "Doe"
        }
        setOrganization {
            company = "Amazon"
            title = "Superstar"
        }
        addEmail {
            address = "john.doe@amazon.com"
            type = EmailEntity.Type.WORK
        }
    }
    .commit()
```

## Allowing blanks

The API allows you to specify if you want to be able to insert blank contacts or not,

```kotlin
.allowBlanks(true|false)
```

For more info, read about [Blank contacts](/docs/entities/about-blank-contacts.md).

## Blank data are not inserted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are 
ignored and are not inserted by insert APIs.

For more info, read about [Blank data](/docs/entities/about-blank-data.md).

## Allowing multiple RawContacts per Account

The API allows you to insert a profile RawContact with an Account that already has a profile 
RawContact,

```kotlin
.allowMultipleRawContactsPerAccount(true|false)
```

According to the `ContactsContract.Profile` documentation; 

> ... each account (including data set, if applicable) on the device may contribute a single raw 
> contact representing the user's personal profile data from that source.

In other words, one account can have one profile RawContact. However, despite the documentation of 
"one profile RawContact per one Account", the Contacts Provider allows for multiple RawContacts per
Account, including multiple local RawContacts (no Account). 

## Associating an Account

New RawContacts can be associated with an Account in order to enable syncing, 

```kotlin
.forAccount(account)
```

For example, to associated the new RawContact to an account,

```kotlin
.forAccount(Account("john.doe@gmail.com", "com.google"))
```

> For more info, read [Query for Accounts](/docs/accounts/query-accounts.md).

### Local RawContacts

If no Account is provided, or null is provided, or if an incorrect account is provided, the
RawContacts inserted will not be associated with an Account. RawContacts inserted without an
associated account are considered local or device-only contacts, which are not synced.

> For more info, read [Sync contact data across devices](/docs/entities/sync-contact-data.md).

There are also certain data kinds that are ignored on insert or update if the RawContact is local.

> For more info, read about [Local (device-only) contacts](/docs/entities/about-local-contacts.md).

## Including only specific data

To include only the given set of fields (data) in each of the insert operation,

```kotlin
.include(fields)
```

For example, to only include email and name fields,

```kotlin
.include { Email.all + Name.all }
```

For more info, read [Include only certain fields for read and write operations](/docs/entities/include-only-desired-data.md).

## Executing the insert

To execute the insert,

```kotlin
.commit()
```

### Handling the insert result

The `commit` function returns a `Result`,

```kotlin
val contactsApi =  Contacts(context)
val newRawContact = NewRawContact(...)

val insertResult = contactsApi
    .profile()
    .insert()
    .rawContact(newRawContact)
    .commit()
```

To check if the insert succeeded,

```kotlin
val insertSucess = insertResult.isSuccessful
```

To get the RawContact IDs of the newly created RawContact,

```kotlin
val rawContactId = insertResult.rawContactId
```

Once you have the RawContact ID, you can retrieve the newly created Contact via the `Query` API,

```kotlin
val contacts = contactsApi
    .query()
    .where { RawContact.Id equalTo rawContactId }
    .find()
```

> For more info, read [Query contacts (advanced)](/docs/query-contacts-advanced.md).

Alternatively, you may use the extensions provided in `ProfileInsertResult`. To get the newly created
Contact,

```kotlin
val contact = insertResult.contact(contactsApi)
```

To instead get the RawContact directly,

```kotlin
val rawContacts = insertResult.rawContact(contactsApi)
```

## Cancelling the insert

To cancel an insert amid execution,

```kotlin
.commit { returnTrueIfInsertShouldBeCancelled() }
```

The `commit` function optionally takes in a function that, if it returns true, will cancel insert
processing as soon as possible. The function is called numerous times during insert processing to
check if processing should stop or continue. This gives you the option to cancel the insert.

For example, to automatically cancel the insert inside a Kotlin coroutine when the coroutine is cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val insertResult = insert.commit { !isActive }
    }
}
```

## Performing the insert and result processing asynchronously

Inserts are executed when the `commit` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](/docs/async/async-execution.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the insert with permission

Inserts require the `android.permission.WRITE_CONTACTS` and `android.permission.GET_ACCOUNTS` 
permissions. If not granted, the insert will do nothing and return a failed result.

> For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but
> only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
> prior to installation instead of at runtime.

To perform the insert with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](/docs/permissions/permissions-handling.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support
 
The `ProfileInsert` API supports custom data. For more info, read [Insert custom data into new or existing contacts](/docs/customdata/insert-custom-data.md).

## RawContact and Contact aggregation

As per documentation in `android.provider.ContactsContract.Profile`,

> The user's profile entry cannot be created explicitly (attempting to do so will throw an
> exception). When a raw contact is inserted into the profile, the provider will check for the
> existence of a profile on the device. If one is found, the raw contact's RawContacts.CONTACT_ID
> column gets the _ID of the profile Contact. If no match is found, the profile Contact is
> created and its _ID is put into the RawContacts.CONTACT_ID column of the newly inserted raw
> contact.