# How do I delete existing sets of data?

This library provides the `DataDelete` API that allows you to delete a list of any data kinds
directly without having to delete them via Contacts/RawContacts.

An instance of the `DataDelete` API is obtained by,

```kotlin
val delete = Contacts(context).data().delete()
```

> To delete all kinds of data via Contacts/RawContacts, you may remove them from the 
> Contact/RawContact and then perform an update.
> For more info, read [How do I update contacts?](/howto/howto-update-contacts.md)

## A basic delete

To delete a set of data,

```kotlin
val deleteResult = Contacts(context)
    .data()
    .delete()
    .data(data)
    .commit()
```

If you want to delete a list of emails and phones,

```kotlin
val deleteResult = Contacts(context)
    .data()
    .delete()
    .data(emails + phones)
    .commit()
```

## Executing the delete

To execute the delete,

```kotlin
.commit()
```

If you want to delete all given data in a single atomic transaction,

```kotlin
.commitInOneTransaction()
```

The call to `commitInOneTransaction` will only succeed if ALL given data are successfully deleted.
If one delete fails, the entire operation will fail and everything will be reverted prior to the 
delete operation. In contrast, `commit` allows for some deletes to succeed and some to fail.

### Handling the delete result

The `commit` and `commitInOneTransaction` functions returns a `Result`,

To check if all deletes succeeded,

```kotlin
val allDeletesSuccessful = deleteResult.isSuccessful
```

To check if a particular delete succeeded,

```kotlin
val firstDeleteSuccessful = deleteResult.isSuccessful(mutableContact1)
```

## Performing the delete and result processing asynchronously

Deletes are executed when the `commit` or `commitInOneTransaction` function is invoked. The work is
done in the same thread as the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the delete with permission

Deletes require the `android.permission.WRITE_CONTACTS` permissions. If not granted, the delete
will do nothing and return a failed result.

To perform the delete with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support

The `DataDelete` API supports custom data. For more info, read [How do I use delete APIs to delete custom data?](/howto/howto-delete-custom-data.md)