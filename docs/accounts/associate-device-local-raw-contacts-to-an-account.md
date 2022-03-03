# Associate a local RawContact to an Account

This library provides the `AccountsLocalRawContactsUpdate` API, which allows you to associate 
**local** RawContacts (those that are not associated with an Account) to an Account in order to 
enable syncing.

An instance of the `AccountsLocalRawContactsUpdate` API is obtained by,

```kotlin
val accountsLocalRawContactsUpdate = Contacts(context).accounts().updateLocalRawContactsAccount()
```

> For more info on local RawContacts, read about [Local (device-only) contacts](./../entities/about-local-contacts.md).

> For more info on syncing, read [Sync contact data across devices](./../entities/sync-contact-data.md).

## Basic usage

To associate/add the given local RawContacts to the given account,

```kotlin
val updateResult = accountsLocalRawContactsUpdate
     .addToAccount(account)
     .localRawContacts(rawContacts)
     .commit()
```

## Executing the update

To execute the update,

```kotlin
.commit()
```

### Handling the update result

The `commit` function returns a `Result`,

To check if all updates succeeded,

```kotlin
val allUpdatesSuccessful = updateResult.isSuccessful
```

To check if a particular update succeeded,

```kotlin
val firstUpdateSuccessful = updateResult.isSuccessful(rawContact1)
```

### Handling update failure

The update may fail for a particular RawContact for various reasons,

```kotlin
updateResult.failureReason(rawContact1)?.let {
    when (it) {
        INVALID_ACCOUNT -> handleInvalidAccount()
        RAW_CONTACT_IS_NOT_LOCAL -> handleRawContactIsNotLocal()
        UNKNOWN -> handleUnknownFailure()
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

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the update with permission

These updates require the `android.permission.GET_ACCOUNTS` and `android.permission.WRITE_CONTACTS`. 
If not granted, the update will do nothing and return a failed result.

To perform the update with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Profile data

The `AccountsLocalRawContactsUpdate` API also supports updating the Profile (device owner) 
RawContacts. To get an instance of this API for Profile data updates,

```kotlin
val profileDataUpdate = Contacts(context).accounts().profile().updateLocalRawContactsAccount()
```

All updates will be limited to the Profile RawContacts, whether it exists or not.

------------------------

## Developer notes (or for advanced users)

> The following section are note from developers of this library for other developers. It is copied
> from the [DEV_NOTES](/DEV_NOTES.md). You may still read the following as a consumer of the library
> in case you need deeper insight.

Due to certain limitations and behaviors imposed by the Contacts Provider, this library only
provides an API to support;

- Associate local RawContacts (those that are not associated with an Account) to an Account,
  allowing syncing between devices.

The library does not provide an API that supports;

- Dissociate RawContacts from their Account such that they remain local to the device and not
  synced between devices.
- Transfer RawContacts from one Account to another.

### SyncColumns modifications

This library supports modifying the `SyncColumns.ACCOUNT_NAME` and `SyncColumns.ACCOUNT_TYPE` of the
RawContacts table in some cases only. In some cases does not work as intended and produces unwanted
side-effects. It probably has something to do with syncing with remote servers and local Account /
sync data not matching up similar to errors on network requests if the system time does not match
network time.

The motivation behind changing the Account columns of the RawContacts table rows is that it would
allow users to;

- Associate local RawContacts (those that are not associated with an Account) to an Account,
  allowing syncing between devices.
- Dissociate RawContacts from their Account such that they remain local to the device and not synced
  between devices.
- Transfer RawContacts from one Account to another.

When modifying the SyncColumns directly, the first works as intended. The second works with some
unwanted side-effects. The third does not work at all and produces unwanted side-effects.

These are the behaviors that I have found;

- Associating local RawContact A to Account X.
  - Works as intended.
  - RawContact A is now associated with Account X and is synced across devices.
- Dissociating RawContact A (setting the SyncColumns' Account name and type to null) from Account X.
  - Partially works with some unwanted-side effects.
  - Dissociates RawContact A from the device but not other devices.
  - RawContact A is no longer visible in the native Contacts app UNLESS it retains the group
    membership to at least the default group from an Account.
  - At this point, RawContact A is a local contact. Changes to this local RawContact A will not be
    synced across devices.
  - If RawContact A is updated in another device and synced up to the server, then a syncing
    side-effect occurs because the RawContact A in the device is different from the RawContact A
    in the server. This causes the Contacts Provider to create another RawContact, resulting in a
    "duplicate". The two RawContact As may get aggregated to the same Contact depending on how
    similar they are.
  - If local RawContact A is re-associated back to Account X, it will still no longer be synced.
- Associating RawContact A from original Account X to Account Y.
  - Does not work and have bad side-effects.
  - No change in other devices.
  - For Lollipop (API 22) and below, RawContact A is no longer visible in the native Contacts app
    and syncing Account Y in system settings fails.
  - For Marshmallow (API 23) and above, RawContact A is no longer visible in the native Contacts
    app. RawContact A is automatically deleted locally at some point by the Contacts Provider.
    Syncing Account Y in system settings succeeds.

Given that associating originally local RawContacts to an Account is the only thing that actually
works, it is the only function that will be exposed to consumers.

If consumers want to transfer RawContacts from one Account to another, they can create a copy of a
RawContact associated with the desired Account and then delete the original RawContact. Same idea
can be used to transform an Account-associated RawContact to a local RawContact. Perhaps we can
implement some functions in this library that does these things? We won't for now because the native
Contacts app does not support these functions anyways. It can always be implemented later if the
community really wants.

Here are some other things to note.

1. The Contacts Provider automatically creates a group membership to the default group of the target
   Account when the account changes. This occurs even if the group membership already exists
   resulting in duplicates.
2. The Contacts Provider DOES NOT delete existing group memberships when the account changes.
   This has to be done manually to prevent duplicates.