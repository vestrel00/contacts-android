# Insert contacts from SIM card

This library provides the `SimContactsDelete` API that allows you to delete existing contacts from
the SIM card.

An instance of the `SimContactsDelete` API is obtained by,

```kotlin
val delete = Contacts(context).sim().delete()
```

## A basic delete

To delete a set of existing contacts from the SIM card,

```kotlin
val deleteResult = Contacts(context)
    .sim()
    .delete()
    .simContacts(existingSimContacts)
    .commit()
```

## Executing the delete

To execute the delete,

```kotlin
.commit()
```

### Handling the delete result

The `commit` function returns a `Result`,

To check if all deletes succeeded,

```kotlin
val allDeletesSuccessful = deleteResult.isSuccessful
```

To check if a particular delete succeeded,

```kotlin
val firstDeleteSuccessful = deleteResult.isSuccessful(simContact)
```

## Performing the delete and result processing asynchronously

Deletes are executed when the `commit` or `commitInOneTransaction` function is invoked. The work is
done in the same thread as the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the delete with permission

Deletes require the `android.permission.WRITE_CONTACTS` permissions. If not granted, the delete
will do nothing and return a failed result.

To perform the delete with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Known issues

Samsung phones (and perhaps other OEMs) support emails (in addition to name and number) data ahead
of the Android 12 release. Updating and deleting SIM contacts that have email data using the APIs
provided in this library may fail. This issue does not occur when moving the SIM card to a different
phone that does not support emails. 