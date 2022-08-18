# Update existing sets of data

This library provides the `DataUpdate` API that allows you to update a list of any data kinds
in the Contacts Provider database directly without having to update them via Contacts/RawContacts.
This ensures that the Contacts Provider database contains the same data you have in memory.

An instance of the `DataUpdate` API is obtained by,

```kotlin
val update = Contacts(context).data().update()
```

> ℹ️ To update all kinds of data via Contacts/RawContacts, read [Update contacts](./../basics/update-contacts.md).

## A basic update

To update a set of data,

```kotlin
val updateResult = Contacts(context)
    .data()
    .update()
    .data(data)
    .commit()
```

If you want to update a list of mutable emails and phones,

```kotlin
val updateResult = Contacts(context)
    .data()
    .update()
    .data(mutableEmails + mutablePhones)
    .commit()
```

## Blank data are deleted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are 
deleted by update APIs, unless the corresponding fields are not included in the operation.

For more info, read about [Blank data](./../entities/about-blank-data.md).

## Including only specific data

To perform update operations only the given set of fields (data),

```kotlin
.include(fields)
```

For example, to perform updates on only email and name fields,

```kotlin
.include { Email.all + Name.all }
```

For more info, read [Include only certain fields for read and write operations](./../basics/include-only-desired-data.md).

## Executing the update

To execute the update,

```kotlin
.commit()
```

### Handling the update result

The `commit` function returns a `Result`,

```kotlin
val contactsApi =  Contacts(context)
val mutableEmail = email.mutableCopy { ... }
val mutablePhone = phone.mutableCopy { ... }

val updateResult = contactsApi
    .date()
    .update()
    .data(mutableEmail, mutablePhone)
    .commit()
```

To check if all updates succeeded,

```kotlin
val allUpdatesSuccessful = updateResult.isSuccessful
```

To check if a particular update succeeded,

```kotlin
val emailUpdateSuccessful = updateResult.isSuccessful(mutableEmail)
```

Once you have performed the updates, you can retrieve the updated data references via the 
`DataQuery` APIs,

```kotlin
val updatedEmail = contactsApi
    .data()
    .query()
    .emails()
    .where { Email.Id equalTo emailId }
    .find()
```

> ℹ️ For more info, read [Query specific data kinds](./../data/query-data-sets.md).

Alternatively, you may use the extensions provided in `DataRefresh`.

To get the updated phone,

```kotlin
val updatedPhone = phone.refresh(contactsApi)
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
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the update with permission

Updates require the `android.permission.WRITE_CONTACTS` permissions. If not granted, the update 
will do nothing and return a failed result.

To perform the update with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Profile data

The `DataUpdate` API also supports updating the Profile (device owner) contact data. To get an 
instance of this API for Profile data updates,

```kotlin
val profileDataUpdate = Contacts(context).profile().data().update()
```

All updates will be limited to the Profile, whether it exists or not.

## Custom data support
 
The `DataUpdate` API supports custom data. For more info, read [Update custom data](./../customdata/update-custom-data.md).
