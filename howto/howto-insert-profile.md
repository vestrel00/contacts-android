# How do I create/insert the device owner Contact profile?

This library provides the `ProfileInsert` API that allows you to insert one or more RawContacts and 
Data. 

The insertion of a RawContact triggers automatic insertion of a new Contact subject to automatic
aggregation by the Contacts Provider.

An instance of the `ProfileInsert` API is obtained by,

```kotlin
val insert = Contacts(context).profile().insert()
```

> If you want to create/insert non-Profile Contacts, read [How do I create/insert contacts?](/contacts-android/howto/howto-insert-contacts.html)

## A basic insert

To create/insert a contact with a name of "John Doe" who works at Amazon with a work email of
"john.doe@amazon.com" (in Kotlin),

```kotlin
val insertResult =  Contacts(context)
    .profile()
    .insert()
    .rawContact {
        name = MutableName().apply {
            givenName = "john"
            familyName = "doe"
        }
        emails.add(MutableEmail().apply {
            type = Email.Type.HOME
            address = "john@doe.com"
        })
    }
    .commit()
```

Or alternatively, in a more Kotlinized style,

```kotlin
val insertResult =  Contacts(context)
    .profile()
    .insert()
    .rawContact {
        setName {
            givenName = "john"
            familyName = "doe"
        }
        addEmail {
            type = Email.Type.HOME
            address = "john@doe.com"
        }
    }
    .commit()
```

## Allowing blanks

The API allows you to specify if you want to be able to insert blank contacts or not,

```kotlin
.allowBlanks(true|false)
```

For more info, read [How do I learn more about "blank" contacts?](/contacts-android/howto/howto-learn-more-about-blank-contacts.html)

## Blank data are not inserted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are 
ignored and are not inserted by insert APIs.

For more info, read [How do I learn more about "blank" data?](/contacts-android/howto/howto-learn-more-about-blank-data.html)

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

> For more info, read [How do I query for Accounts?](/contacts-android/howto/howto-query-accounts.html)

If no Account is provided, or null is provided, or if an incorrect account is provided, the 
RawContacts inserted will not be associated with an Account. RawContacts inserted without an 
associated account are considered local or device-only contacts, which are not synced.
     
> For more info, read [How do I sync contact data across devices?](/contacts-android/howto/howto-sync-contact-data.html)

**Lollipop (API 22) and below**

When an Account is added, from a state where no accounts have yet been added to the system, the
Contacts Provider automatically sets all of the null `accountName` and `accountType` in the
RawContacts table to that Account's name and type.

RawContacts inserted without an associated account will automatically get assigned to an account if
there are any available. This may take a few seconds, whenever the Contacts Provider decides to do
it. Dissociating RawContacts from Accounts will result in the Contacts Provider associating those
back to an Account.

**Marshmallow (API 23) and above**

The Contacts Provider no longer associates local contacts to an account when an account is or
becomes available. Local contacts remain local.

**Account removal**

Removing the Account will delete all of the associated rows in the Contact, RawContact, Data, and
Groups tables locally. This includes user Profile data in those tables.

## Including only specific data

To include only the given set of fields (data) in each of the insert operation,

```kotlin
.include(fields)
```

For example, to only include email fields,

```kotlin
.include(Fields.Email.all)
```

For more info, read [How do I include only the data that I want?](/contacts-android/howto/howto-include-only-desired-data.html)

## Executing the insert

To execute the insert,

```kotlin
.commit()
```

### Handling the insert result

The `commit` function returns a `Result`,

```kotlin
val contactsApi =  Contacts(context)
val mutableRawContact = MutableRawContact().apply { ... }

val insertResult = contactsApi
    .profile()
    .insert()
    .rawContact(mutableRawContact)
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
    .where(Fields.RawContact.Id equalTo rawContactId)
    .find()
```

> For more info, read [How do I get a list of contacts in a more advanced way?](/contacts-android/howto/howto-query-contacts-advanced.html)

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
For more info, read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/contacts-android/howto/howto-use-api-with-async-execution.html)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the insert with permission

Inserts require the `android.permission.WRITE_CONTACTS` and `android.permission.GET_ACCOUNTS` 
permissions. If not granted, the insert will do nothing and return a failed result.

> For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but
> only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
> prior to installation instead of at runtime.

To perform the insert with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/contacts-android/howto/howto-use-api-with-permissions-handling.html)

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support
 
The `ProfileInsert` API supports custom data. For more info, read [How do I use insert and update APIs to create/insert custom data into new or existing contacts?](/contacts-android/howto/howto-insert-custom-data.html)
