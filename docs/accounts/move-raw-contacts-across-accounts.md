# Move RawContacts across Accounts

This library provides the `MoveRawContactsAcrossAccounts` API, which allows you to move
RawContacts from one Account to another, including a null Account.

This API functions identically to the Google Contacts app. Copies of RawContacts are inserted into
the Contacts Provider database under a different account and the original RawContacts are deleted 
afterwards. RawContact and Data values are also copied over.

In other words, this is a copy-insert-delete operation. New rows are created in the RawContact, 
Contact, and Data tables with the same values from the original. Then, the original rows are deleted.

> ⚠️ The APIs for this have changed significantly since [version 0.3.0](https://github.com/vestrel00/contacts-android/releases/tag/0.3.0).
> For documentation for [version 0.2.4](https://github.com/vestrel00/contacts-android/releases/tag/0.2.4)
> and below, only supported moving local RawContacts to an Account,
> [visit this page (click me)](https://github.com/vestrel00/contacts-android/blob/0.2.4/docs/accounts/associate-device-local-raw-contacts-to-an-account.md).

An instance of the `MoveRawContactsAcrossAccounts` API is obtained by,

```kotlin
val move = Contacts(context).accounts().move()
```

> ℹ️ For more info on syncing, read [Sync contact data across devices](./../entities/sync-contact-data.md).

> ℹ️ For more info on local RawContacts, read about [Local (device-only) contacts](./../entities/about-local-contacts.md).

## Basic usage

To move a given RawContacts to another account,

```kotlin
val moveResult = move.rawContactsTo(account, rawContact).commit()
```

If you only have the RawContact's ID,

```kotlin
val moveResult = move.rawContactsWithIdsTo(account, rawContactId).commit()
```

## Executing the move

To execute the move,

```kotlin
.commit()
```

### Handling the move result

The `commit` function returns a `Result`,

To check if all moves succeeded,

```kotlin
val allUMovesSuccessful = moveResult.isSuccessful
```

To check if a particular move succeeded,

```kotlin
val firstMoveSuccessful = moveResult.isSuccessful(rawContact1)
```

To get the RawContact IDs of all the newly created RawContact copies,

```kotlin
val rawContactCopiesIds = moveResult.rawContactIds
```

To get the ID of the RawContact copy of a particular original RawContact,

```kotlin
val firstRawContactCopyId = moveResult.rawContactId(originalRawContact2)
```

Once you have the RawContact IDs, you can retrieve the newly created aggregated Contacts via the 
`Query` API,

```kotlin
val contacts = contactsApi
    .query()
    .where { RawContact.Id `in` rawContactCopiesIds }
    .find()
```

> ℹ️ For more info, read [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

Or, retrieve the RawContacts directly via the `RawContactsQuery` API,

```kotlin
val contacts = contactsApi
    .rawContactsQuery()
    .where { RawContact.Id `in` rawContactCopiesIds }
    .find()
```

> ℹ️ For more info, read [Query RawContacts](./../basics/query-raw-contacts.md).

Alternatively, you may use the extensions provided in `MoveRawContactsAcrossAccountsResult`. 
To get all newly created Contacts,

```kotlin
val contacts = moveResult.contacts(contactsApi)
```

To get a particular contact,

```kotlin
val contact = moveResult.contact(contactsApi, originalRawContactId)
```

To instead get the RawContacts directly,

```kotlin
val rawContacts = moveResult.rawContacts(contactsApi)
```

To get a particular RawContact,

```kotlin
val rawContact = moveResult.rawContact(contactsApi, originalRawContactId)
```

### Handling move failure

The move may fail for a RawContact for various reasons,

```kotlin
moveResult.failureReason(originalRawContactId)?.let {
    when (it) {
        INVALID_ACCOUNT -> showInvalidAccountError()
        ALREADY_IN_ACCOUNT -> promptUserToPickDifferentAccount()
        RAW_CONTACT_NOT_FOUND -> showRawContactNotFoundError()
        INSERT_RAW_CONTACT_COPY_FAILED -> promptUserToTryAgain()
        DELETE_ORIGINAL_RAW_CONTACT_FAILED -> showOriginalRawContactNotDeletedError()
        UNKNOWN -> showGenericErrorMessage()
    }   
}
```

## Cancelling the move

To cancel a move amid execution,

```kotlin
.commit { returnTrueIfMoveShouldBeCancelled() }
```

The `commit` function optionally takes in a function that, if it returns true, will cancel move
processing as soon as possible. The function is called numerous times during move processing to
check if processing should stop or continue. This gives you the option to cancel the move.

For example, to automatically cancel the move inside a Kotlin coroutine when the coroutine is
cancelled,

```kotlin
launch {
    withContext(coroutineContext) {
        val moveResult = move.commit { !isActive }
    }
}
```

## Performing the move and result processing asynchronously

Moves are executed when the `commit` function is invoked. The work is done in the same thread as
the call-site. This may result in a choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in
the `async` module. For more info,
read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> ℹ️ Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing the move with permission

Moves require the `android.permission.READ_CONTACTS`, `android.permission.WRITE_CONTACTS` and 
`android.permission.GET_ACCOUNTS` permissions. If not granted, the move will do nothing and return 
a failed result.

To perform the move with permission, use the extensions provided in the `permissions` module. For
more info, read [Permissions handling using coroutines](./../permissions/permissions-handling-coroutines.md).

You may, of course, use other permission handling libraries or just do it yourself =)

## Profile support

The `MoveRawContactsAcrossAccounts` API does NOT support moving Profile RawContacts.

## Custom data support

The `MoveRawContactsAcrossAccounts` API supports custom data. For more info,
read [Integrate custom data](./../customdata/integrate-custom-data.md).

## Group memberships are copied over on a best-effort basis

- Groups with matching title (case-sensitive)
- Default Group (autoAdd is true)
- Favorites Group (if starred is true)

## Default/primary flags of Data rows are not copied over

For example, if a phone number is set as the default (isPrimary: 1, isSuperPrimary: 1), after this 
move operation it will no longer be a default data (isPrimary: 0,	isSuperPrimary: 0). 
_Yes, like all other behaviors of this API, this is the same as Google Contacts._

## Contact IDs and lookup keys may change

This means that references to Contact IDs and lookup keys may become invalid. For example, shortcuts
may break after performing this operation.

## (Raw)Contact links (AggregationExceptions) are copied over

For example, if there are two or more RawContacts that are linked to the same Contact, moving one
or more of the RawContacts will still result in the RawContacts being linked to the same Contact 
(though the original Contact may have been deleted and replaced with a new copy).

------------------------

## Developer notes (or for advanced users)

Let's take a look at what happens when **Google Contacts** moves RawContacts to different Accounts.
Other apps such as AOSP Contacts and Samsung Contacts may do things a bit differently. We will
focus on modelling API behavior after Google Contacts behavior because... it's Google. For testing,
I used a Pixel 4 API 30 emulator and a Samsung Galaxy A71 API 33.

| **Original Account** | **Target Account** |
|----------------------|--------------------|
| null (local)         | X                  |
| X                    | null (local)       |
| X                    | Y                  |

For all of the above scenarios, the behavior is as follows...

- New row in RawContacts table with same values as original
  - Original row is deleted
- New row in Contacts table as the parent of the new RawContact
  - Original row is deleted
- New rows in the Data table with same values as original, belonging to the new RawContact
  - Original rows are deleted

**Group memberships** (which are Account-based) are "carried over" on a best-effort basis;

- Groups with matching title (case-sensitive)
- Default Group (autoAdd is true)
- Favorites Group (if starred is true)

**Default/primary** flags of Data rows are not copied over. For example, if a phone number is set
as the default (isPrimary: 1, isSuperPrimary: 1), after this move operation it will no longer
be a default data (isPrimary: 0,	isSuperPrimary: 0). _Yes, like all other behaviors of this API,
this is the same as Google Contacts._

Contact **IDs** and **lookup keys** may change. This means that references to Contact IDs and
lookup keys may become invalid. For example, shortcuts may break after performing this
operation.

**(Raw)Contact links** (AggregationExceptions) are also copied over, in some cases. For example,
if there are two RawContacts linked to the same Contact and one RawContact is moved to device 
(no Account), a new RawContact is created, the original RawContact is deleted, and the two 
RawContacts are still linked together. However, in the case where there are two RawContacts (one 
having no Account) linked to the same Contact and the device RawContact is moved to the same Account
as the sibling RawContact, the two RawContacts are no longer linked to the same Contact. This may
or may not be intentional. Regardless, we should fix it =)