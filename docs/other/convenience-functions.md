# Convenience functions

This library provides some nice-to-have extensions in the `contacts.core.utils` package. I will be 
going over some of them in this page.

> Note that functions in the util package that are used directly by other APIs such as result APIs
> are not discussed here.

## Contact data getter and setters

Contacts can be made up of one or more RawContacts. In the case that a Contact has two or more
RawContacts, getting/setting RawContact data may be a bit of a hassle, requiring loops or iterators,

```kotlin
// get all emails from all RawContacts belonging to the Contact
val contactEmails = contact.rawContacts.flatMap { it.emails }
// add an email to the first RawContact belonging to the Contact
contact.mutableCopy().rawContacts.first().emails.add(NewEmail())
```

> For more info, read about [API Entities](./../entities/about-api-entities.md).

To simplify things, getter/setter extensions are provided in the **`ContactData.kt`** file,

```kotlin
// get all emails from all RawContacts belonging to the Contact
val contactEmailSequence = contact.emails()
val contactEmailList = contact.emailList()
// add an email to the first RawContact belonging to the Contact
contact.mutableCopy().addEmail(NewEmail())
```

> Newer versions of the Android Open Source Project Contacts app and the Google Contacts app shows
> data coming from all RawContacts in a Contact details screen. However, they only allow editing
> a single RawContact and not the aggregate Contact in a single screen to avoid confusion. 
> With this in mind, feel free to use the getter extensions but be very careful with using the
> setters!

## Mutable and New RawContact data setters

Getting data from RawContacts is straightforward. You have direct access to their properties. The
same goes for setting data. 

```kotlin
val rawContactEmails = rawContact.emails
rawContact.mutableCopy().addEmail(
    NewEmail().apply{
        address = "abc@alphabet.com"
        type = EmailEntity.Type.WORK
    }
)
```

Still, there are some setter extensions provided in `MutableRawContactData.kt` and 
`NewRawContactData.kt` that can add some sugar to your syntax.

```kotlin
rawContact.mutableCopy().addEmail {
    address = "abc@alphabet.com"
    type = EmailEntity.Type.WORK
}
```

The setter functions in this section and in the "Contact data getter and setters" section also 
uphold the redacted state of the mutable Contact/RawContact. We setting or adding a property using
these extensions, the property being passed will be redacted if the Contact/RawContact it is being
added to is redacted.

> For more info, read [Redact entities and API input and output in production](./../entities/redact-apis-and-entities.md).

## Getting the parent Contact of a RawContact or Data

Using the `Query` API, it is easy to get the parent Contact of a RawContact or Data,

```kotlin
val contactOfRawContact = contactsApi.query().where { Contact.Id equalTo rawContact.contactId }.find().firstOrNull()
val contactOfData = contactsApi.query().where { Contact.Id equalTo data.contactId }.find().firstOrNull()
```

> For more info, read [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

To shorten things, you can use the extensions in `RawContactContact.kt` and `DataContact.kt`,

```kotlin
val contactOfRawContact = rawContact.contact(contactsApi)
val contactOfData = data.contact(contactsApi)
```

On a similar note, to get the parent RawContact of a Data using the extensions in `DataRawContact.kt`,

```kotlin
val rawContactOfData = data.rawContact(contactsApi)
```

These are blocking calls so you might want to do them outside the UI thread.

> For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

## Refresh Contact, RawContact, and Data references

In-memory references to these entities could become inaccurate due to changes in the database that
could occur in your app, other apps, or by the Contacts Provider. If you need to get the most
up-to-date reference of an entity from the database, you could do it using the `Query` and 
`DataQuery` APIs,

```kotlin
val contactFromDb = contactsApi.query().where { Contact.Id equalTo contactInMemory.id }.find().firstOrNull()
val rawContactFromDb = contactsApi.query().where { RawContact.Id equalTo rawContactInMemory.id }.find()
    .firstOrNull()
    ?.rawContacts
    ?.find { it.id == rawContactInMemory.id }
val dataFromDb = contactsApi.data().query().where { DataId equalTo dataInMemory.id }.find().firstOrNull()
```

To shorten things, you can use extensions in `ContactRefresh.kt`, `RawContactRefresh.kt`, and
`DataRefresh.kt`,

```kotlin
val contactFromDb = contactInMemory.refresh(contactsApi)
val rawContactFromDb = rawContactInMemory.refresh(contactsApi)
val dataFromDb = dataInMemory.refresh(contactsApi)
```

These are blocking calls so you might want to do them outside the UI thread.

> For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

## Sort Contacts by data fields

The `Query` and `BroadQuery` APIs allows you to sort Contacts based on fields in the Contacts table
such as `Id` and `DisplayNamePrimary`, 

```kotlin
val sortedContacts = query.orderBy(ContactsFields.DisplayNamePrimary.desc(ignoreCase = true))
```

If you want to sort Contacts based on data fields (e.g. email), you are unable to use the query 
APIs provided in this library to do so. However, if you have a list of Contacts in memory, you can
use the extensions in `ContactsComparator.kt` to build a `Comparator` to use for sorting,

```kotlin
val sortedContacts = unsortedContacts.sortedWith(
    Fields.Email.Address.desc(ignoreCase = true).contactsComparator()
)
```

You can also specify multiple fields for sorting,

```kotlin
val sortedContacts = unsortedContacts.sortedWith(
    setOf(
        Fields.Contact.Options.Starred.desc(),
        Fields.Contact.DisplayNamePrimary.asc(ignoreCase = false),
        Fields.Email.Address.asc()
    ).contactsComparator()
)
```

## Get the Group of a GroupMembership

The `GroupsQuery` allows you to get groups from a set of group Ids,

```kotlin
val group = contactsApi.groups().query().where { Id equalTo groupMembership.groupId }.find().firstOrNull()
val groups = contactsApi.groups().query().where { Id `in` groupMemberships.map { it.groupId } }.find()
```

To shorten things, you can use the extensions in `GroupMembershipGroup.kt`,

```kotlin
val group = groupMembership.group(contactsApi)
val groups = groupMemberships.groups(contactsApi)
```

These are blocking calls so you might want to do them outside the UI thread.

> For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

## Get the RawContact of a BlankRawContact

The `Query` API allows you to get the `RawContact` version of a `BlankRawContact`,

```kotlin
val rawContact = contactsApi.query().where { RawContact.Id equalTo blankRawContact.id }.find()
    .firstOrNull()
    ?.rawContacts
    ?.find { it.id == blankRawContact.id }
```

To shorten things, you can use the extensions in `BlankRawContactToRawContact.kt`,

```kotlin
val rawContact = blankRawContact.toRawContact(contactsApi)
```

These are blocking calls so you might want to do them outside the UI thread.

> For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).
