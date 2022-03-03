# Link unlink Contacts

The Contacts Provider automatically aggregates similar RawContacts into a single Contact when it
determines that they reference the same person. However, the Contacts Provider's aggregation
algorithms are only as accurate as the Data belonging to these RawContacts. Sometimes, they are not
enough to determine if they indeed are the same person. With this in mind, the Contacts Provider
allows us to explicitly and forcefully specify whether two or more RawContacts reference the same
person or not.

Hence, this library provides extensions in `contacts.core.util.ContactLinks.kt` to allow for linking
and unlinking two or more Contacts (and their constituent RawContacts).

-------------------

## Linking

To link three Contacts and all of their constituent RawContacts into a single Contact,

```kotlin
val linkResult = contact1.link(contactsApi, contact2, contact3)
```

The above links (keep together) all RawContacts belonging to `contact1`, `contact2`, and `contact3`
into a single Contact.

Aggregation is done by the Contacts Provider. For example,

- Contact (id: 1, display name: A)
    - RawContact A
- Contact (id: 2, display name: B)
    - RawContact B
    - RawContact C

Linking Contact 1 with Contact 2 results in;

- Contact (id: 1, display name: A)
    - RawContact A
    - RawContact B
    - RawContact C

Contact 2 no longer exists and all of the Data belonging to RawContact B and C are now associated
with Contact 1.

If instead Contact 2 is linked with Contact 1;

- Contact (id: 1, display name: B)
    - RawContact A
    - RawContact B
    - RawContact C

The same thing occurs except the display name has been set to the display name of RawContact B.

This function only instructs the Contacts Provider which RawContacts should be aggregated to a
single Contact. Details on how RawContacts are aggregated into a single Contact are left to the
Contacts Provider.

> Profile Contact/RawContacts are not supported! This operation will fail if given any profile
> Contact/RawContacts .

### Handling the link result

To check if the link succeeded,

```kotlin
val linkSuccessful = linkResult.isSuccessful
```

To get the ID of the parent Contact of all linked RawContacts,

```kotlin
val contactId: Long? = linkResult.contactId
```

> Note that the `contactId` will belong to one of the linked Contacts.

Once you have the Contact ID, you can retrieve the Contact via the `Query` API,

```kotlin
val contact = contactsApi
    .query()
    .where { Contact.Id equalTo contactId }
    .find()
```

> For more info, read [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

Alternatively, you may use the extensions provided in `ContactLikResult`. To get the parent Contact 
of all linked RawContacts,

```kotlin
val contact = linkResult.contact(contactsApi)
```

-------------------

## Unlinking

To unlink a Contacts with more than one RawContact into a separate Contacts,

```kotlin
val unlinkResult = contact.unlink(contactsApi)
```

The above unlinks (keep separate) all RawContacts belonging to the `contact` into separate
Contacts.

The above does nothing and will fail if the Contact only has one constituent RawContact.

> Profile Contact/RawContacts are not supported! This operation will fail if given any profile 
> Contact/RawContacts .

### Handling the unlink result

To check if the unlink succeeded,

```kotlin
val unlinkSuccessful = unlinkResult.isSuccessful
```

To get the IDs of the constituent RawContact of of the Contact that has been unlinked,

```kotlin
val rawContactIds = unlinkResult.rawContactIds
```

Once you have the RawContact IDs, you can retrieve the corresponding Contacts via the `Query` API,

```kotlin
val contacts = contactsApi
    .query()
    .where { RawContact.Id `in` rawContactIds }
    .find()
```

> For more info, read [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

Alternatively, you may use the extensions provided in `ContactLikResult`. To get the Contacts
of all unlinked RawContacts,

```kotlin
val contacts = unlinkResult.contacts(contactsApi)
```

------------------

## Changes are immediate and are not applied to the receiver

These apply to set and clear functions.

1. Changes are immediate.
    - These functions will make the changes to the Contacts Provider database immediately. You do
      not need to use update APIs to commit the changes.
2. Changes are not applied to the receiver.
    - This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
      query APIs or refresh extensions or process the result of this function call to get the most
      up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
      Provider database.

## Performing linking/unlinking asynchronously

Linking or unlinking contacts is done in the same thread as the call-site. This may result in a
choppy UI.

To perform the work in a different thread, use the Kotlin coroutine extensions provided in
the `async` module. For more info,
read [Execute work outside of the UI thread using coroutines](./../async/async-execution-coroutines.md).

You may, of course, use other multi-threading libraries or just do it yourself =)

> Extensions for Kotlin Flow and RxJava are also in the v1 roadmap.

## Performing linking/unlinking with permission

Getting and setting/clearing default data require the `android.permission.WRITE_CONTACTS`
permission. If not granted, linking/unlinking data will fail.

TODO Update this section as part of issue [#138](https://github.com/vestrel00/contacts-android/issues/138).

## Syncing is done at the RawContact level

You may link Contacts with RawContacts that belong to different Accounts. Any RawContact Data
modifications are synced per Account sync settings.

> For more info, read [Sync contact data across devices](./../entities/sync-contact-data.md).

RawContacts that are not associated with an Account are local to the device and therefore will not
be synced even if it is linked to a Contact with a RawContact that is associated with an Account.

> For more info, read about [Local (device-only) contacts](./../entities/about-local-contacts.md).

------------------------

## Developer notes (or for advanced users)

> The following section are note from developers of this library for other developers. It is copied
> from the [DEV_NOTES](./../dev-notes.md). You may still read the following as a consumer of the library
> in case you need deeper insight.

### Behavior of linking/merging/joining contacts (AggregationExceptions)

The native Contacts app terminology has changed over time;

- API 22 and below; join / separate
- API 23; merge / unmerge
- API 24 and above; link / unlink

However, the internals have not changed; `KEEP_TOGETHER` / `KEEP_SEPARATE`. These operations are
supported by the `ContactsContract.AggregationExceptions`.

For example, given the following tables,

```
### Contacts table
Contact id: 32, displayName: X, starred: 0, timesContacted: 1, lastTimeContacted: 1573071785456, customRingtone: content://media/internal/audio/media/109, sendToVoicemail: 0
Contact id: 33, displayName: Y, starred: 1, timesContacted: 2, lastTimeContacted: 1573071750624, customRingtone: content://media/internal/audio/media/115, sendToVoicemail: 1

### RawContacts table
RawContact id: 30, contactId: 32, displayName: X, accountName: x@x.com, accountType: com.google, starred: 0, timesContacted: 1, lastTimeContacted: 1573071785456, customRingtone: content://media/internal/audio/media/109, sendToVoicemail: 0
RawContact id: 31, contactId: 33, displayName: Y, accountName: y@y.com, accountType: com.google, starred: 1, timesContacted: 2, lastTimeContacted: 1573071750624, customRingtone: content://media/internal/audio/media/115, sendToVoicemail: 1

### Data table
Data id: 57, rawContactId: 30, contactId: 32, mimeType: vnd.android.cursor.item/group_membership, data1: 18
Data id: 58, rawContactId: 30, contactId: 32, mimeType: vnd.android.cursor.item/name, data1: X, isPrimary: 1, isSuperPrimary: 1
Data id: 59, rawContactId: 30, contactId: 32, mimeType: vnd.android.cursor.item/email_v2, data1: x@x.com
Data id: 60, rawContactId: 30, contactId: 32, mimeType: vnd.android.cursor.item/email_v2, data1: xx@x.com, isPrimary: 1, isSuperPrimary: 1
Data id: 63, rawContactId: 31, contactId: 33, mimeType: vnd.android.cursor.item/group_membership, data1: 6
Data id: 64, rawContactId: 31, contactId: 33, mimeType: vnd.android.cursor.item/name, data1: Y, isPrimary: 1, isSuperPrimary: 1
Data id: 65, rawContactId: 31, contactId: 33, mimeType: vnd.android.cursor.item/email_v2, data1: y@y.com
Data id: 66, rawContactId: 31, contactId: 33, mimeType: vnd.android.cursor.item/email_v2, data1: yy@y.com, isPrimary: 1, isSuperPrimary: 1
```

When Contact **X** links/merges/joins Contact **Y**, the tables becomes;

```
### Contacts table
Contact id: 32, displayName: X, starred: 1, timesContacted: 2, lastTimeContacted: 1573071785456, customRingtone: content://media/internal/audio/media/109, sendToVoicemail: 0

### RawContacts table
RawContact id: 30, contactId: 32, displayName: X, accountName: x@x.com, accountType: com.google, starred: 0, timesContacted: 1, lastTimeContacted: 1573071785456, customRingtone: content://media/internal/audio/media/109, sendToVoicemail: 0
RawContact id: 31, contactId: 32, displayName: Y, accountName: y@y.com, accountType: com.google, starred: 1, timesContacted: 2, lastTimeContacted: 1573071750624, customRingtone: content://media/internal/audio/media/115, sendToVoicemail: 1

### Data table
Data id: 57, rawContactId: 30, contactId: 32, mimeType: vnd.android.cursor.item/group_membership, data1: 18
Data id: 58, rawContactId: 30, contactId: 32, mimeType: vnd.android.cursor.item/name, data1: X, isPrimary: 1, isSuperPrimary: 1
Data id: 59, rawContactId: 30, contactId: 32, mimeType: vnd.android.cursor.item/email_v2, data1: x@x.com
Data id: 60, rawContactId: 30, contactId: 32, mimeType: vnd.android.cursor.item/email_v2, data1: xx@x.com, isPrimary: 1, isSuperPrimary: 0
Data id: 63, rawContactId: 31, contactId: 32, mimeType: vnd.android.cursor.item/group_membership, data1: 6
Data id: 64, rawContactId: 31, contactId: 32, mimeType: vnd.android.cursor.item/name, data1: Y, isPrimary: 1, isSuperPrimary: 0
Data id: 65, rawContactId: 31, contactId: 32, mimeType: vnd.android.cursor.item/email_v2, data1: y@y.com
Data id: 66, rawContactId: 31, contactId: 33, mimeType: vnd.android.cursor.item/email_v2, data1: yy@y.com, isPrimary: 1, isSuperPrimary: 0
```

**What changed?**

Contact Y's row has been deleted and its column values have been merged into Contact X row. If the
reverse occurred (Contact Y merged with Contact X), Contact Y's row would still be deleted. The
difference is that Contact X's display name will be set to Contact Y's display name, which is done
by the native Contacts app manually by setting Contact Y's Data name row to be the "default"
(isPrimary and isSuperPrimary both set to 1).

> The AggregationExceptions table records the linked RawContacts's IDs in ascending order regardless
> of the order used in RAW_CONTACT_ID1 and RAW_CONTACT_ID2 at the time of merging.

The RawContacts and Data table remains the same except the joined contactId column values have now
been changed to the id of Contact X. All Data rows' isSuperPrimary value has been set to 0 though
the isPrimary columns remain the same. In other words, this clears any "default" set before the
link. These are done automatically by the Contacts Provider during the link operation.

What is not done automatically by the Contacts Provider is that the name row of former Contact X is
set as the default. The native Contacts app does this manually. The Contacts Providers automatically
sets the Contact display name to whatever the default name row is for the Contact, if available. For
more info on Contact display name resolution, read the **Contact Display Name and Default Name
Rows** section.

> Note that display name resolution is different for APIs below 21 (pre-lollipop).

The display name of the RawContacts remain the same.

The Groups table remains unmodified.

**Options updates**

Changes to the options (starred, timesContacted, lastTimeContacted, customRingtone, and
sendToVoicemail) of a RawContact may affect the options of the parent Contact. On the other hand,
changes to the options of the parent Contact will be propagated to all child RawContact options.

**Photo updates**

A RawContact may have a full-sized photo saved as a file and a thumbnail version of that saved in
the Data table in a photo mimetype row. A Contact's full-sized photo and thumbnail are simply
references to the "chosen" RawContact's full-sized photo and thumbnail (though the URIs may differ).

> Note that when removing the photo in the native contacts app, the photo data row is not
> immediately deleted, though the `PHOTO_FILE_ID` is immediately set to null. This may result in
> the `PHOTO_URI` and `PHOTO_THUMBNAIL_URI` to still have a valid image uri even though the photo
> has been "removed". This library immediately deletes the photo data row, which seems to work
> perfectly.

**Data inserts**

In the native Contacts app, Data inserted in combined (raw) contacts mode will be associated to the
first RawContact in the list sorted by the RawContact ID.

> This may not be the same as the RawContact referenced by `ContactsColumns.NAME_RAW_CONTACT_ID`.

**UI changes?**

The native Contacts App does not display the groups field when displaying / editing Contacts that
have multiple RawContacts (linked/merged/joined) in combined mode. However, it does allow editing
individual RawContact Data rows in which case the groups field is displayed and editable.

In the native Contacts app, the name attribute used comes from the name row with IS_SUPER_PRIMARY
set to true. This and all other "unique" mimetypes (organization) and non-unique mimetypes (email)
per RawContact are shown only if they are not blank.

**Showing multiple RawContact's data in the same edit screen (combined mode)**

In older version of the native, Android Open Source Project (AOSP) Contacts app, data from multiple
RawContacts was being shown in the same edit screen. This caused a lot of confusion about which data
belonged to which RawContact. Newer versions of AOSP Contacts only allow editing one RawContact at a
time to avoid confusion. Though, several RawContacts' data are still shown (not-editable)
in the same screen.

### AggregationExceptions table

Given the following Contacts and their RawContacts;

- Contact A
    - RawContact 1
- Contact B
    - RawContact 2
- Contact C
    - RawContact 3
- Contact D
    - RawContact 4

Linking one by one in this order;

- Contact B link Contact A
- Contact C link Contact D
- Contact C link Contact B

Results in the following AggregationExceptions rows respectively;

```
Aggregation exception id: 430, type: 1, rawContactId1: 1, rawContactId2: 2
```

```
Aggregation exception id: 430, type: 1, rawContactId1: 1, rawContactId2: 2
Aggregation exception id: 432, type: 1, rawContactId1: 3, rawContactId2: 4
```

```
Aggregation exception id: 436, type: 1, rawContactId1: 1, rawContactId2: 2
Aggregation exception id: 439, type: 1, rawContactId1: 1, rawContactId2: 3
Aggregation exception id: 442, type: 1, rawContactId1: 1, rawContactId2: 4
Aggregation exception id: 440, type: 1, rawContactId1: 2, rawContactId2: 3
Aggregation exception id: 443, type: 1, rawContactId1: 2, rawContactId2: 4
Aggregation exception id: 444, type: 1, rawContactId1: 3, rawContactId2: 4
```

There is a pattern here. RawContact ids are sorted in ascending order and linked from least to
greatest exhaustively but no double links (1-2 is the same as 2-1).

- RawContact 1 has a row with RawContact 2, 3, and 4.
- RawContact 2 has a row with RawContact 3 and 4.
- RawContact 3 has a row with RawContact 4.

Linking all in one go;

- Contact C link Contact A, B, D

Results in the same AggregationExceptions rows.

Unlinking results in the same AggregationExceptions rows **except** the type is 2
(`TYPE_KEEP_SEPARATE`).

### Contact Display Name and Default Name Rows

If available, the "default" (isPrimary and isSuperPrimary set to 1) name row for a Contact is
automatically set as the Contact display name by the Contacts Provider. Otherwise, the Contacts
Provider chooses from any of the other suitable data from the aggregate Contact.

> The `ContactsColumns.NAME_RAW_CONTACT_ID` is automatically updated by the Contacts Provider
> along with the display name.

The default status of other sources (e.g. email) does not affect the Contact display name.

The native Contacts app also sets the most recently updated name as the default at every update.
This results in the Contact display name changing to the most recently updated name from one of the
associated RawContacts. The "most recently updated name" is the name field that was last updated by
the user when editing in the Contacts app, which is irrelevant to its value. It does not matter if
the user deleted the last character of the name, added the same character back, and then saved. It
still counts as the most recently updated.

All of the above only applies to API 21 and above.

**Display name resolution is different for APIs below 21 (pre-Lollipop)!**

The `ContactsColumns.NAME_RAW_CONTACT_ID` was added in API 21. It changed the way display names are
resolved for Contacts with more than one constituent RawContacts, which is what has been described
so far.

Before this change (APIs 20 and below), the native Contacts app is still able to set the Contact
display name somehow. I'm not sure how. If someone figures it out, please let me know. I tried
updating the Contact `DISPLAY_NAME` directly but it does not work. Setting a name row as default
also does not affect the Contact `DISPLAY_NAME`.

## Effects of linking/unlinking contacts

When two or more Contacts (along with their constituent RawContacts) are linked into a single
Contact those Contacts will be merged into one of the existing Contact row. The Contacts that have
been merged into the single Contact will have their entries/rows in the Contacts table deleted.

Unlinking will result in the original Contacts prior to linking to have new rows in the Contacts
table with different IDs because the previously deleted row IDs cannot be reused.

Getting Contacts that have been linked into a single Contact or Contacts whose row IDs have change
after unlinking is still possible using the Contact lookup key.

For more info, read about [Contact lookup key vs ID](./../entities/about-contact-lookup-key.md).
