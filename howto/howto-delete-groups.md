# How do I delete groups?

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
    ?.groups(existingGroups)
    ?.commit()
```

## Executing the delete

To execute the delete,

```kotlin
.commit()
```

If you want to delete all given groups in a single atomic transaction,

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
For more info, read [How do I use the async module to simplify executing work outside of the UI thread using coroutines?](/howto/howto-use-api-with-async-execution.md)

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the delete with permission

Deletes require the `android.permission.WRITE_CONTACTS` permissions. If not granted, the delete
will do nothing and return a failed result.

To perform the delete with permission, use the extensions provided in the `permissions` module.
For more info, read [How do I use the permissions module to simplify permission handling using coroutines?](/howto/howto-use-api-with-permissions-handling.md)

You may, of course, use other permission handling libraries or just do it yourself =)

## Groups deletion is not supported prior to API 26

Prior to Android 8.0 (Oreo, API 26), group deletion is unpredictable. Groups that are marked for
deletion remain in the DB and is still shown in the native Contacts app. Sometimes they do get
deleted at some point but the trigger for the actual deletion eludes me.

The native Contacts app (prior to API 26) does NOT support group deletion perhaps because groups
syncing isn't implemented or at least not to the same extent as contacts syncing. Therefore, this
library will also not support group deletion for API versions lower than 26.

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

When a group is deleted, any membership to that group is deleted automatically by the
Contacts Provider.

## Deletion is not immediate

**Groups are not immediately deleted**. However, they are marked for deletion and they do get
deleted in the background by the Contacts Provider depending on sync settings.

However, group memberships to those groups marked for deletion are immediately deleted!

### Starred in Android (Favorites)

When a Contact is starred, the Contacts Provider automatically adds a group membership to the
favorites group for all RawContacts linked to the Contact. Setting the Contact starred to false
removes all group memberships to the favorites group.

The Contact's "starred" value is interdependent with group memberships to the favorites group.
Adding a group membership to the favorites group results in starred being set to true. Removing
the membership sets it to false.

Raw contacts that are not associated with an account do not have any group memberships. Even
though these RawContacts may not have a membership to the favorites group, they may still be
"starred" (favorited), which is not dependent on the existence of a favorites group membership.

**Refresh RawContact instances after changing the starred value.** Otherwise, performing an
update on the RawContact with a stale set of group memberships may revert the star/unstar
operation. For example,

-> query returns a starred RawContact
-> set starred to false
-> update RawContact (still containing a group membership to the favorites group)
-> starred will be set back to true.