# How do I delete Contacts/RawContacts?

This library provides the `Delete` API, which allows you to delete one or more Contacts or 
RawContacts.

An instance of the `Delete` API is obtained by,

```kotlin
val delete = Contacts(context).delete()
```

> If you want to delete the device owner Contact Profile, 
> read [How do I delete the device owner Contact profile?](/howto/howto-delete-profile.md)

> If you want to delete a set of Data, 
> read [How do I delete existing sets of data?](/howto/howto-delete-data-sets.md)

## A basic delete

To delete a set of Contact and all of its RawContacts,

```kotlin
val deleteResult = delete
     .contacts(contactToDelete)
     .commit()
```

If you want to delete a set of RawContacts, 

```kotlin
val deleteResult = delete
     .rawContacts(contactToDelete)
     .commit()
```

You may specify `contacts` and `rawContacts` in the same delete operation.

Note that **Contacts are deleted automatically when all constituent RawContacts are deleted.**

## Executing the delete

To execute the delete,

```kotlin
.commit()
```

If you want to delete all given Contacts and RawContacts in a single atomic transaction,

```kotlin
.commitInOneTransaction()
```

The call to `commitInOneTransaction` will only succeed if ALL given Contacts and RawContacts are
successfully deleted. If one delete fails, the entire operation will fail and everything will be
reverted prior to the delete operation. In contrast, `commit` allows for some deletes to succeed
and some to fail.

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

The `Delete` API supports custom data. For more info, read [How do I use delete APIs to delete custom data?](/howto/howto-delete-custom-data.md)

## Data belonging to RawContacts/Contact are deleted

When a RawContact is deleted, all of its data are also deleted.

## Contacts are deleted automatically when all constituent RawContacts are deleted

Deleting a contact's Contacts row, RawContacts row(s), and associated Data row(s) are best explained
in the documentation in `ContactsContract.RawContacts`;

> When a raw contact is deleted, all of its Data rows as well as StatusUpdates,
> AggregationExceptions, PhoneLookup rows are deleted automatically.
>
> When all raw contacts associated with a Contacts row are deleted, the Contacts row itself is also
> deleted automatically.
>
> The invocation of resolver.delete(...), does not immediately delete a raw contacts row. Instead,
> it sets the ContactsContract.RawContactsColumns.DELETED flag on the raw contact and removes the
> raw contact from its aggregate contact. The sync adapter then deletes the raw contact from the
> server and finalizes phone-side deletion by calling resolver.delete(...) again and passing the
> ContactsContract#CALLER_IS_SYNCADAPTER  query parameter.
>
> Some sync adapters are read-only, meaning that they only sync server-side changes to the phone,
> but not the reverse. If one of those raw contacts is marked for deletion, it will remain on the
> phone. However it will be effectively invisible, because it will not be part of any aggregate
> contact.

**TLDR**

To delete a contacts and all associated rows, simply delete all RawContact rows with the desired
Contacts id. Deletion of the Contacts row and associated Data row(s) will be done automatically by
the Contacts Provider.

Note that deleting a RawContacts row may not immediately (or at all) actually delete the RawContacts
row. In this case, it is marked as deleted and its reference to a contact id is null.