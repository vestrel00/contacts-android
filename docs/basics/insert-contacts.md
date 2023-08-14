# Insert contacts

This library provides the `Insert` API that allows you to insert one or more RawContacts and Data.

An instance of the `Insert` API is obtained by,

```kotlin
val insert = Contacts(context).insert()
```

> ℹ️ If you want to create/insert the device owner Contact Profile, read [Insert device owner Contact profile](./../profile/insert-profile.md).

> ℹ️ If you want to insert Data into a new or existing contact, read [Insert data into new or existing contacts](./../data/insert-data-sets.md).

## A basic insert

To create/insert a contact with a name of "John Doe" who works at Amazon with a work email of
"john.doe@amazon.com" (in Kotlin),

```kotlin
val insertResult = Contacts(context)
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

For more info, read about [Blank contacts](./../entities/about-blank-contacts.md).

## Blank data are not inserted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are
ignored and are not inserted by insert APIs.

For more info, read about [Blank data](./../entities/about-blank-data.md).

## Associating an Account

New RawContacts can be associated with an Account in order to enable syncing,

```kotlin
newRawContact.account = account
// or newRawContact.setAccount(account)
```

> ℹ️ Prior to [version 0.3.0](https://github.com/vestrel00/contacts-android/releases/tag/0.3.0),
> setting the account is done via the `Insert.forAccount` function.

For example, to associated the new RawContact to an account,

```kotlin
newRawContact.account = Account("john.doe@gmail.com", "com.google")
// or newRawContact.setAccount(Account("john.doe@gmail.com", "com.google"))
```

> ℹ️ For more info, read [Query for Accounts](./../accounts/query-accounts.md).

### Local RawContacts

If no Account is provided, or null is provided, or if an incorrect account is provided, the
RawContacts inserted will not be associated with an Account. RawContacts inserted without an
associated account are considered local or device-only contacts, which are not synced.

> ℹ️ For more info, read [Sync contact data across devices](./../entities/sync-contact-data.md).

## Including only specific data

To include only the given set of fields (data) in each of the insert operation,

```kotlin
.include(fields)
```

For example, to only include email and name fields,

```kotlin
.include { Email.all + Name.all }
```

For more info, read [Include only certain fields for read and write operations](./../basics/include-only-desired-data.md).

## Executing the insert

To execute the insert,

```kotlin
.commit()
```

### Handling the insert result

The `commit` function returns a `Result`,

```kotlin
val contactsApi = Contacts(context)
val newRawContact1 = NewRawContact(...)
val newRawContact2 = NewRawContact(...)

val insertResult = contactsApi
    .insert()
    .rawContacts(newRawContact1, newRawContact2)
    .commit()
```

To check if all inserts succeeded,

```kotlin
val allInsertsSuccessful = insertResult.isSuccessful
```

To check if a particular insert succeeded,

```kotlin
val firstInsertSuccessful = insertResult.isSuccessful(newRawContact1)
```

To get the RawContact IDs of all the newly created RawContacts,

```kotlin
val allRawContactIds = insertResult.rawContactIds
```

To get the RawContact ID of a particular RawContact,

```kotlin
val secondRawContactId = insertResult.rawContactId(newRawContact2)
```

Once you have the RawContact IDs, you can retrieve the newly created Contacts via the `Query` API,

```kotlin
val contacts = contactsApi
    .query()
    .where { RawContact.Id `in` allRawContactIds }
    .find()
```

> ℹ️ For more info, read [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

Alternatively, you may use the extensions provided in `InsertResult`. To get all newly created
Contacts,

```kotlin
val contacts = insertResult.contacts(contactsApi)
```

To get a particular contact,

```kotlin
val contact = insertResult.contact(contactsApi, newRawContact1)
```

To instead get the RawContacts directly,

```kotlin
val rawContacts = insertResult.rawContacts(contactsApi)
```

To get a particular RawContact,

```kotlin
val rawContact = insertResult.rawContact(contactsApi, newRawContact2)
```

## Cancelling the insert

To cancel an insert amid execution,

```kotlin
.commit { returnTrueIfInsertShouldBeCancelled() }
```

The `commit` function optionally takes in a function that, if it returns true, will cancel insert
processing as soon as possible. The function is called numerous times during insert processing to
check if processing should stop or continue. This gives you the option to cancel the insert.

For example, to automatically cancel the insert inside a Kotlin coroutine when the coroutine is
cancelled,

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

To perform the work in a different thread, use the Kotlin coroutine extensions provided in
the `async` module. For more info,
read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the project roadmap.

## Performing the insert with permission

Inserts require the `android.permission.WRITE_CONTACTS` and `android.permission.GET_ACCOUNTS`
permissions. If not granted, the insert will do nothing and return a failed result.

To perform the insert with permission, use the extensions provided in the `permissions` module. For
more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support

The `Insert` API supports custom data. For more info, read [Insert custom data into new or existing contacts](./../customdata/insert-custom-data.md).

## RawContact and Contacts aggregation

As per documentation in `android.provider.ContactsContract.Contacts`,

> A Contact cannot be created explicitly. When a raw contact is inserted, the provider will first
> try to find a Contact representing the same person. If one is found, the raw contact's
> RawContacts#CONTACT_ID column gets the _ID of the aggregate Contact. If no match is found,
> the provider automatically inserts a new Contact and puts its _ID into the
> RawContacts#CONTACT_ID column of the newly inserted raw contact.

## Insert a new RawContact with data of every kind

Unless you are allowing blanks, you only need to provide at least one data kind when inserting a new
contact in order for the operation to succeed.

If you want to provide data of every kind, which is useful when implementing a contact creation
screen,

```kotlin
val accountToAddContactTo = Account("vestrel00@pixar.com", "com.pixar")

val insertResult = Contacts(context)
    .insert()
    .rawContact {
        setAccount(accountToAddContactTo)
        setName {
            givenName = "Buzz"
            familyName = "Lightyear"
        }
        setNickname {
            name = "Buzz"
        }
        setOrganization {
            title = "Space Toy"
            company = "Pixar"
        }
        addPhone {
            number = "(555) 555-5555"
            type = PhoneEntity.Type.CUSTOM
            label = "Fake Number"
        }
        setSipAddress {
            sipAddress = "sip:buzz.lightyear@pixar.com"
        }
        addEmail {
            address = "buzz.lightyear@pixar.com"
            type = EmailEntity.Type.WORK
        }
        addEmail {
            address = "buzz@lightyear.net"
            type = EmailEntity.Type.HOME
        }
        addAddress {
            formattedAddress = "1200 Park Ave"
            type = AddressEntity.Type.WORK
        }
        addIm {
            data = "buzzlightyear@skype.com"
            protocol = ImEntity.Protocol.SKYPE
        }
        addWebsite {
            url = "https://www.pixar.com"
        }
        addWebsite {
            url = "https://www.disney.com"
        }
        addEvent {
            date = EventDate.from(year = 1995, month = 10, dayOfMonth = 22)
            type = EventEntity.Type.BIRTHDAY
        }
        addRelation {
            name = "Childhood friend"
            type = RelationEntity.Type.CUSTOM
            label = "Imaginary Friend"
        }
        groupMemberships.addAll(
            contactsApi
                .groups()
                .query()
                .accounts(accountToAddContactTo)
                .where { Title contains "friend"}
                .find()
                .newMemberships()
        )
        setNote {
            note = "The best toy in the world!"
        }
    }
    .commit()
```

## Inserting photos and thumbnails

To set full-sized photos (and by API design thumbnails), 
read [Get set remove full-sized and thumbnail contact photos](./../other/get-set-remove-contact-raw-contact-photo.md).