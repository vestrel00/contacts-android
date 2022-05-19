# Delete device owner Contact profile

This library provides the `ProfileDelete` API, which allows you to delete the device owner Profile 
Contact or only some of its constituent RawContacts.

> ℹ️ There can be only one device owner Contact, which is either set (not null) or not yet set 
> (null). However, like other regular Contacts, the Profile Contact may have one or more 
> RawContacts.

An instance of the `ProfileDelete` API is obtained by,

```kotlin
val delete = Contacts(context).profile().delete()
```

> ℹ️ If you want to delete non-Profile Contacts, read [Delete Contacts](./../basics/delete-contacts.md)

## A basic delete

To delete a the profile Contact (if it exist) and all of its RawContacts,

```kotlin
val deleteResult = delete
     .contact()
     .commit()
```

If you want to delete a set of RawContacts belonging to the profile Contact,

```kotlin
val deleteResult = delete
     .rawContacts(contactToDelete)
     .commit()
```

Note that the **profile Contact is deleted automatically when all constituent RawContacts are deleted.**

## Executing the delete

To execute the delete,

```kotlin
.commit()
```

If you want to delete all given RawContacts in a single atomic transaction,

```kotlin
.commitInOneTransaction()
```

The call to `commitInOneTransaction` will only succeed if ALL given RawContacts are successfully 
deleted. If one delete fails, the entire operation will fail and everything will be reverted prior 
to the delete operation. In contrast, `commit` allows for some deletes to succeed and some to fail.
This really only applies to when only `rawContacts` are specified.

## Performing the delete and result processing asynchronously

Deletes are executed when the `commit` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in the `async` module.
For more info, read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the delete with permission

Deletes require the `android.permission.WRITE_CONTACTS` permissions. If not granted, the delete
will do nothing and return a failed result.

> ℹ️ For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but
> only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
> prior to installation instead of at runtime.

To perform the delete with permission, use the extensions provided in the `permissions` module.
For more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Custom data support

The `ProfileDelete` API supports custom data. For more info, read [Delete custom data](./../customdata/delete-custom-data.md).
