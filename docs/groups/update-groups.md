# Update groups

This library provides the `GroupsUpdate` API that allows you to update existing Groups.

An instance of the `GroupsUpdate` API is obtained by,

```kotlin
val update = Contacts(context).groups().update()
```

## A basic update

To update an existing group,

```kotlin
val updateResult = Contacts(context)
    .groups()
    .update()
    .groups(existingGroup?.mutableCopy {
        title = "Best Friends"
    })
    .commit()
```

If you need to update multiple groups,

```kotlin
val mutableGroup1 = group1.mutableCopy { ... }
val mutableGroup2 = group2.mutableCopy { ... }

val updateResult = Contacts(context)
    .groups()
    .update()
    .groups(mutableGroup1, mutableGroup2)
    .commit()
```

## Read-only Groups

Groups created by the system are typically read-only. You cannot modify them, even if you try! The 
Contacts Provider typically have the following system groups (for standard Google Accounts),

- systemId: Contacts, title: My Contacts
- systemId: null, title: Starred in Android
- systemId: Friends, title: Friends
- systemId: Family, title: Family
- systemId: Coworkers, title: Coworkers

The above list may vary per Account and/or flavor of Android.

If you are implementing a sync adapter, you may be able to update read-only groups associated with
the Account that your sync adapter works with. For more info, read
[Contacts API Setup | Sync adapter operations](./../setup/setup-contacts-api.md#sync-adapter-operations).

## Groups and duplicate titles

The Contacts Provider allows multiple groups with the same title (case-sensitive comparison) 
belonging to the same (nullable) account to exist. In older versions of Android, the AOSP Contacts 
app allows the creation of new groups with existing titles. In newer versions, duplicate titles are 
not allowed. Therefore, this library does not allow for duplicate titles.

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
val firstUpdateSuccessful = updateResult.isSuccessful(mutableGroup1)
```

### Handling update failure 

The update may fail for a particular group for various reasons,

```kotlin
updateResult.failureReason(mutableGroup1)?.let {
    when (it) {
        TITLE_ALREADY_EXIST -> promptUserToPickDifferentTitle()
        GROUP_IS_READ_ONLY -> informUserThatReadOnlyGroupsCannotBeModified()
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

Updates require the `android.permission.WRITE_CONTACTS`. If not granted, the update will do nothing 
and return a failed result.

To perform the update with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)