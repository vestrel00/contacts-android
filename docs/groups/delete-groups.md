# Delete groups

This library provides the `GroupsDelete` API that allows you to delete existing Groups.

An instance of the `GroupsDelete` API is obtained by,

```kotlin
val delete = Contacts(context).groups().delete()
```

## A basic delete

To delete a set of existing groups,

```kotlin
val deleteResult = Contacts(context)
    .groups()
    .delete()
    .groups(existingGroups)
    .commit()
```

To delete a set of existing groups using IDs,

```kotlin
val deleteResult = Contacts(context)
    .groups()
    .delete()
    .groupsWithId(1, 2, 3)
    .commit()
```

## An advanced delete

You may specify a matching criteria, like in queries, that will delete all matching groups,

```kotlin
val deleteResult = delete
    .groupsWhere { AccountType equalTo "com.google" }
    .commit()
```

## Executing the delete

To execute the delete,

```kotlin
.commit()
```

To delete all given groups in a single atomic transaction,

```kotlin
.commitInOneTransaction()
```

The call to `commitInOneTransaction` will only succeed if ALL given groups are successfully deleted.
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
val firstDeleteSuccessful = deleteResult.isSuccessful(group1)
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

## Read-only Groups

Groups created by the system are typically read-only. You cannot delete them, even if you try! The
Contacts Provider typically have the following system groups (for standard Google Accounts),

- systemId: Contacts, title: My Contacts
- systemId: null, title: Starred in Android
- systemId: Friends, title: Friends
- systemId: Family, title: Family
- systemId: Coworkers, title: Coworkers

The above list may vary per account.

The `GroupsDelete` API will not attempt to delete a read-only group and will simply result in
failure.

## Group memberships are automatically deleted

When a group is deleted, any memberships to that group are deleted automatically by the
Contacts Provider.

## Deletion is not guaranteed to be immediate

**Groups may not immediately be deleted**. They are marked for deletion and get deleted in the 
background by the Contacts Provider depending on sync settings and network availability.

Group **memberships** to those groups marked for deletion are immediately deleted!

> ℹ️ For more info, read [Sync contact data across devices](./../entities/sync-contact-data.md).