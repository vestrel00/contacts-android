# How do I associate a local RawContacts to an Account?

This library provides the `AccountsLocalRawContactsUpdate` API, which allows you to associate 
**local** RawContacts (those that are not associated with an Account) to an Account in order to 
enable syncing.

An instance of the `AccountsLocalRawContactsUpdate` API is obtained by,

```kotlin
val accountsLocalRawContactsUpdate = Contacts(context).accounts().updateLocalRawContactsAccount()
```

> For more info on local RawContacts, 
> read [How do I learn more about "local" (device-only) contacts?](/howto/howto-learn-more-about-local-contacts.md)

> For more info on syncing,
> read [How do I sync contact data across devices?](/howto/howto-sync-contact-data.md)

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
read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the update with permission

These updates require the `android.permission.GET_ACCOUNTS` and `android.permission.WRITE_CONTACTS`. 
If not granted, the update will do nothing and return a failed result.

To perform the update with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)

You may, of course, use other permission handling libraries or just do it yourself =)

## Profile data

The `AccountsLocalRawContactsUpdate` API also supports updating the Profile (device owner) 
RawContacts. To get an instance of this API for Profile data updates,

```kotlin
val profileDataUpdate = Contacts(context).accounts().profile().updateLocalRawContactsAccount()
```

All updates will be limited to the Profile RawContacts, whether it exists or not.

## Developer notes

Due to certain limitations and behaviors imposed by the Contacts Provider, this library only
provides an API to support;

- Associate local RawContacts (those that are not associated with an Account) to an Account,
  allowing syncing between devices.

The library does not provide an API that supports;

- Dissociate RawContacts from their Account such that they remain local to the device and not
  synced between devices.
- Transfer RawContacts from one Account to another.

Read the **SyncColumns modifications** section of the DEV_NOTES for more details.