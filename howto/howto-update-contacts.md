# How do I update contacts?

This library provides the `Update` API that allows you to update one or more Contacts, RawContacts, 
and Data.

An instance of the `Update` API is obtained by,

```kotlin
val update = Contacts(context).update()
```

> If you want to update the device owner Contact Profile, read [How do I update the device owner Contact profile?](/contacts-android/howto/howto-update-profile.html)

> If you want to update a set of Data, read [How do I update existing sets of data?](/contacts-android/howto/howto-update-data-sets.html)

## A basic update

To update a Contact and all of its RawContacts,

```kotlin
val updateResult = Contacts(context)
    .update()
    .contacts(johnDoe.mutableCopy {
       setOrganization {
           company = "Microsoft"
           title = "Newb"
       }
       emails().first().apply {
           address = "john.doe@microsoft.com"
       }
    })
    .commit()
```

To update a RawContact directly,

```kotlin
val updateResult = Contacts(context)
    .update()
    .rawContacts(johnDoeFromGmail.mutableCopy {
       setOrganization {
           company = "Microsoft"
           title = "Newb"
       }
       emails().first().apply {
           address = "john.doe@microsoft.com"
       }
    })
    .commit()
```

## Deleting blanks

The API allows you to specify if you want the update operation to delete blank contacts or not,

```kotlin
.deleteBlanks(true|false)
```

For more info, read [How do I learn more about "blank" contacts?](/contacts-android/howto/howto-learn-more-about-blank-contacts.html)

## Blank data are deleted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are 
deleted by update APIs.

For more info, read [How do I learn more about "blank" data?](/contacts-android/howto/howto-learn-more-about-blank-data.html)

## Including only specific data

To include only the given set of fields (data) in each of the update operation,

```kotlin
.include(fields)
```

For example, to only include email and name fields,

```kotlin
.include { Email.all + Name.all }
```

For more info, read [How do I include only the data that I want?](/contacts-android/howto/howto-include-only-desired-data.html)

## Executing the update

To execute the update,

```kotlin
.commit()
```

### Handling the update result

The `commit` function returns a `Result`,

```kotlin
val contactsApi =  Contacts(context)
val mutableContact1 = contact1.mutableCopy { ... }
val mutableContact2 = contact2.mutableCopy { ... }

val updateResult = contactsApi
    .update()
    .contacts(mutableContact1, mutableContact2)
    .commit()
```

To check if all updates succeeded,

```kotlin
val allUpdatesSuccessful = updateResult.isSuccessful
```

To check if a particular update succeeded,

```kotlin
val firstUpdateSuccessful = updateResult.isSuccessful(mutableContact1)
```

Once you have performed the updates, you can retrieve the updated Contacts references via the `Query` API,

```kotlin
val updatedContacts = contactsApi
    .query()
    .where { Contact.Id `in` listOf(contact1.id) }
    .find()
```

> For more info, read [How do I get a list of contacts in a more advanced way?](/contacts-android/howto/howto-query-contacts-advanced.html)

Alternatively, you may use the extensions provided in `ContactRefresh` and `RawContactRefresh`.

To get the updated Contact and all of its RawContacts and Data,

```kotlin
val updatedContact1 = contact1.refresh(contactsApi)
```

To get an updated RawContact and Data,

```kotlin
val updatedRawContact1 = contact1.rawContacts.first().refresh(contactsApi)
```

## Cancelling the update

To cancel an update amid execution,

```kotlin
.commit { returnTrueIfUpdateShouldBeCancelled() }
```

The `commit` function optionally takes in a function that, if it returns true, will cancel update
processing as soon as possible. The function is called numerous times during update processing to
check if processing should stop or continue. This gives you the option to cancel the update.

For example, to automatically cancel the update inside a Kotlin coroutine when the coroutine is cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val updateResult = update.commit { !isActive }
    }
}
```

## Performing the update and result processing asynchronously

Updates are executed when the `commit` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/contacts-android/howto/howto-use-api-with-async-execution.html)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the update with permission

Updates require the `android.permission.WRITE_CONTACTS` permissions. If not granted, the update 
will do nothing and return a failed result.

To perform the update with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/contacts-android/howto/howto-use-api-with-permissions-handling.html)

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support
 
The `Update` API supports custom data. For more info, read [How do I use update APIs to update custom data?](/contacts-android/howto/howto-update-custom-data.html)

## Modifiable Contact fields

As per documentation in `android.provider.ContactsContract.Contacts`,

> Only certain columns of Contact are modifiable: STARRED, CUSTOM_RINGTONE, SEND_TO_VOICEMAIL.
> Changing any of these columns on the Contact also changes them on all constituent raw contacts.

The rest of the APIs provided in this library allow you to modify Data fields (e.g. Email, Phone, 
etc). Essentially, anything that the Contacts Provider allows for modification =) 

### Local RawContacts

Updates to local RawContacts are not synced!

> For more info, read [How do I sync contact data across devices?](/contacts-android/howto/howto-sync-contact-data.html).

There are also certain data kinds that are ignored on insert or update if the RawContact is local.

> For more info, read [How do I learn more about "local" (device-only) contacts?](/contacts-android/howto/howto-learn-more-about-local-contacts.html)
