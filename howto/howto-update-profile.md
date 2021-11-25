# How do I update the device owner Contact profile?

This library provides the `ProfileUpdate` API that allows you to update the device owner Profile Contact.
There can be only one device owner, which is either set (not null) or not yet set (null).

An instance of the `ProfileUpdate` API is obtained by,

```kotlin
val query = Contacts(context).profile().update()
```

> If you want to update non-Profile Contacts, read [How do I update contacts?](/howto/howto-update-contacts.md)

## A basic update

To update the profile Contact and all of its RawContacts,

```kotlin
val updateResult = Contacts(context)
    .profile()
    .update()
    .contact(profile.toMutableContact().apply {
        // make changes
    })
    .commit()
```

To update a profile RawContact directly,

```kotlin
val updateResult = Contacts(context)
    .update()
    .rawContacts(profile.rawContacts.first().toMutableContact().apply {
        // make changes
    })
    .commit()
```

## Deleting blanks

The API allows you to specify if you want the update operation to delete blank RawContacts or not,

```kotlin
.deleteBlanks(true|false)
```

For more info, read [How do I learn more about "blank" contacts?](/howto/howto-learn-more-about-blank-contacts.md)

## Including only specific data

To include only the given set of fields (data) in each of the update operation,

```kotlin
.include(fields)
```

For example, to only include email fields,

```kotlin
.include(Fields.Email.all)
```

For more info, read [How do I include only the data that I want?](/howto/howto-include-only-desired-data.md)

## Executing the update

To execute the update,

```kotlin
.commit()
```

### Handling the update result

The `commit` function returns a `Result`,

```kotlin
val contactsApi =  Contacts(context)
val mutableProfile = profile.toMutableContact().apply { ... }

val updateResult = contactsApi
    .profile()
    .update()
    .contact(mutableProfile)
    .commit()
```

To check if all updates succeeded,

```kotlin
val allUpdatesSuccessful = updateResult.isSuccessful
```

To check if a particular update succeeded,

```kotlin
val firstUpdateSuccessful = updateResult.isSuccessful(mutableProfile.rawContacts.first())
```

Once you have performed the updates, you can retrieve the updated profile Contact reference via the `Query` API,

```kotlin
val updatedProfile = Contacts(context).profile().query().find()
```

> For more info, read [How do I get the device owner Contact profile?](/howto/howto-query-profile.md)

Alternatively, you may use the extensions provided in `ContactRefresh` and `RawContactRefresh`.

To get the updated profile Contact and all of its RawContacts and Data,

```kotlin
val updatedProfile = profile.refresh(contactsApi)
```

To get an updated RawContact and Data,

```kotlin
val updatedProfileRawContact = profile.rawContacts.first().refresh(contactsApi)
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
For more info, read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the update with permission

Updates require the `android.permission.WRITE_CONTACTS` permissions. If not granted, the update 
will do nothing and return a failed result.

> For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but
> only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
> prior to installation instead of at runtime.

To perform the update with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support
 
The `ProfileUpdate` API supports custom data. For more info, read [How do I use update APIs to update custom data?](/howto/howto-update-custom-data.md)
