# Update contacts in SIM card

This library provides the `SimContactsUpdate` API that allows you to update contacts in the SIM card.

An instance of the `SimContactsUpdate` API is obtained by,

```kotlin
val update = Contacts(context).sim().update()
```

Note that SIM card update will only work if there is a SIM card in the ready state. For more info,
read [SIM card state](./../sim/about-sim-contacts.md#sim-card-state).

## A basic update

To update an existing contact in the SIM card,

```kotlin
var current: SimContact
var modified: MutableSimContact = current.mutableCopy {
    // change the name and/or number
}

val updateResult = Contacts(context)
    .sim()
    .update()
    .simContact(current, modified)
    .commit()
```

### Making further updates

The current entry in the SIM table is not updated based on the ID. Instead, the name AND number are 
used to lookup the entry to update. Continuing the example above, if you need to make another 
update, then you must use the modified copy as the current,

```kotlin
current = modified
modified = current.newCopy {
    // change the name and/or number
}

val result = update
     .simContact(current, modified)
     .commit()
```

> ℹ️ This limitation comes from Android, not this library.

### Updating multiple contacts

If you need to update multiple contacts,

```kotlin
val update1 = SimContactsUpdate.Entry(contact1, contact1.mutableCopy { ... })
val update2 = SimContactsUpdate.Entry(contact2, contact2.mutableCopy { ... })

val updateResult = Contacts(context)
    .sim()
    .update()
    .simContacts(update1, update2)
    .commit()
```

## Blank contacts are not allowed

For more info, read about [SIM Contacts](./../sim/about-sim-contacts.md#blanks-are-not-allowed)

## Character limits

For more info, read about [SIM Contacts](./../sim/about-sim-contacts.md#character-limits)

## Executing the update

To execute the update,

```kotlin
.commit()
```

### Handling the update result

The `commit` function returns a `Result`.

To check if all updates succeeded,

```kotlin
val allUpdatesSuccessful = updateResult.isSuccessful
```

To check if a particular update succeeded,

```kotlin
val firstUpdateSuccessful = updateResult.isSuccessful(simContact)
```

### Handling update failure

The update may fail for a particular SIM contact for various reasons,

```kotlin
updateResult.failureReason(simContact)?.let {
    when (it) {
        NAME_EXCEEDED_MAX_CHAR_LIMIT -> tellUserTheNameIsTooLong()
        NUMBER_EXCEEDED_MAX_CHAR_LIMIT -> tellUserTheNumberIsTooLong()
        NAME_AND_NUMBER_ARE_BLANK -> tellUserTheNameAndNumberCannotBothBeBlank()
        UNKNOWN -> showGenericErrorMessage()
    }
}
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

To perform the work in a different thread, use the Kotlin coroutine extensions provided in
the `async` module. For more info,
read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the project roadmap.

## Performing the update with permission

Updates require the `android.permission.WRITE_CONTACTS` permission. If not granted, the update will
do nothing and return a failed result.

To perform the update with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Known issues

Samsung phones (and perhaps other OEMs) support emails (in addition to name and number) data ahead
of the Android 12 release. Updating and deleting SIM contacts that have email data using the APIs
provided in this library may fail. This issue does not occur when moving the SIM card to a different
phone that does not support emails. 