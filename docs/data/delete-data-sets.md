# Delete existing sets of data

This library provides the `DataDelete` API that allows you to delete a list of any data kinds
directly without having to delete them via Contacts/RawContacts.

An instance of the `DataDelete` API is obtained by,

```kotlin
val delete = Contacts(context).data().delete()
```

> ℹ️ To delete all kinds of data via Contacts/RawContacts, you may remove them from the 
> Contact/RawContact and then perform an update. For more info, read [Update contacts](./../basics/update-contacts.md).

## A basic delete

To delete a set of data,

```kotlin
val deleteResult = delete
    .data()
    .delete()
    .data(data)
    .commit()
```

To delete a list of emails and phones,

```kotlin
val deleteResult = delete
    .data()
    .delete()
    .data(emails + phones)
    .commit()
```

To delete a set of data using data IDs,

```kotlin
val deleteResult = delete
    .data()
    .delete()
    .dataWithId(1, 2, 3)
    .commit()
```

## An advanced delete

You may specify a matching criteria, like in queries, that will delete all matching data,

```kotlin
val deleteResult = delete
    .dataWhere { Email.Address endsWith "@yahoo.com" }
    .commit()
```

## Executing the delete

To execute the delete,

```kotlin
.commit()
```

If you want to delete all specified data in a single atomic transaction,

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
val dataDeleteSuccessful = deleteResult.isSuccessful(data)
val dataDeleteSuccessful = deleteResult.isSuccessful(data.id)
```

To check if a particular advanced delete managed to delete at least one matching data,

```kotlin
val where = Fields.Email.Address endsWith "@yahoo.com"
val deleteResult = delete.dataWhere(where).commit()
val advancedDeleteSuccessful = deleteResult.isSuccessful(where)
```

## Performing the delete and result processing asynchronously

Deletes are executed when the `commit` or `commitInOneTransaction` function is invoked. The work is
done in the same thread as the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the delete with permission

Deletes require the `android.permission.WRITE_CONTACTS` permissions. If not granted, the delete
will do nothing and return a failed result.

To perform the delete with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Profile data

The `DataDelete` API also supports deleting the Profile (device owner) contact data. To get an
instance of this API for Profile data deletes,

```kotlin
val profileDataDelete = Contacts(context).profile().data().delete()
```

All deletes will be limited to the Profile, whether it exists or not.

## Custom data support

The `DataDelete` API supports custom data. For more info, read [Delete custom data](./../customdata/delete-custom-data.md).