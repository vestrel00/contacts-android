# Developer Notes

This document contains useful developer notes that should be kept in mind during development. It 
serves as a memory of all the quirks and gotcha's of things like Android's `ContactsContract`.

This is only meant to be read by contributors of this library, not consumers!

## Contacts Provider / ContactsContract

It is important to know about the ins and outs of Android's Contacts Provider. After all, this API 
is just a wrapper around it. 

> A very sweet, sugary wrapper! Sugar. Spice. And everything nice. :D

It is important to get familiar with the [official documentation of the Contact's Provider][1].

Here is a summary;

There are 3 main database tables used in dealing with contacts;

1. Contacts
2. RawContacts
3. Data

> There are more but that is covered later.

All of these tables and their fields are enumerated and documented in
`android.provider.ContactsContract`. 

Each table serves a different purpose;

1. Contacts
    - Rows representing different people.
2. RawContacts
    - Rows that link Contacts rows to specific Accounts.
3. Data
    - Rows containing data (e.g. name, email) for a RawContacts row.
    
These tables contain the following (notable) information (columns);

1. `Contacts`
    - `_ID`
    - `DISPLAY_NAME_PRIMARY`
2. `RawContacts`
    - `_ID`: the `Contacts._ID`
    - `ACCOUNT_NAME`: the `Account.name`
    - `ACCOUNT_TYPE` the `Account.type`
3. `Data`
    - `RAW_CONTACT_ID`: the `RawContacts._ID`
    - `CONTACT_ID`: the `Contacts._ID`
    - `DATA_1` to `DATA_15`: contains a piece of contact data 
      (e.g. first and last name, email address and type) determined by 
      the `MIMETYPE`
    - `MIMETYPE`: the type of data that this row's `DATA_X` columns contain
      (e.g. name and email data)
      
The tables are connected the following way;

- RawContacts contains a reference to the Contacts row Id.
- Data contains a reference to the RawContacts row Id and Contacts row Id. 

### Contacts; Display Name

The `Contacts.DISPLAY_NAME` name may be different than the Data `StructuredName` display name! If a
structured name in the Data table is not provided, then other kinds of data will be used as the 
`Contacts` row display name. For example, if an email is provided but no structured name then the
display name will be the email. When a structured name is inserted, the Contacts Provider 
automatically updates the Contacts row display name.

> In the case of `StructuredName`, the `Contacts.DISPLAY_NAME` is made up of the prefix, given,
> middle, family name, and suffix and not the unstructured display name.

If no data rows suitable to be a display name are available, then the Contacts row display name will
be null. Data suitable to be a Contacts row display name are enumerated in `DisplayNameSources`;

- email
- nickname
- organization
- phone number
- structured name

Data not suitable to be display names are;

- address
- event
- group
- im
- note
- relation
- sip
- website

The kind of data used as the display for the Contact is set in
`ContactNameColumns.DISPLAY_NAME_SOURCE`.

**A note about `StructuredName`**

There may be a scenario where the unstructured `StructuredName.DISPLAY_NAME` does not match the
structured components. Such scenarios are possible but is considered incorrect. For example,
it is possible to programmatically set the display name to "Ice Cold" but set the given and family
name to "Hot Fire". The `Contacts.DISPLAY_NAME` is made up of the prefix, given, middle, family
name, and suffix ("Hot Fire") and not the unstructured display name.

The Contacts Provider's [general matching][3] algorithm does **not** include the
`Contacts.DISPLAY_NAME`. However, the `StructuredName.DISPLAY_NAME` is included in the matching
process but not the rest of the structured components (e.g. given and family name).

The native Contacts app displays the `Contacts.DISPLAY_NAME`. So, here comes the unusual scenario
that looks like a bug. The [general matching][3] algorithm will match the text "Ice" or "Cold" but
not "Hot" or "Fire". The end result is that searching for the Contact "Ice Cold" will show a
Contact called "Hot Fire"!

### Contact Display Name and Default Name Rows

If available, the "default" (isPrimary and isSuperPrimary set to 1) name row for a Contact is 
automatically set as the Contact display name by the Contacts Provider. Otherwise, the Contacts
Provider chooses from any of the other suitable data from the aggregate Contact.

> The `ContactsColumns.NAME_RAW_CONTACT_ID` is automatically updated by the Contacts Provider
> along with the display name.

The default status of other sources (e.g. email) does not affect the Contact display name.

The native Contacts app also sets the most recently updated name as the default at every update. 
This results in the Contact display name changing to the most recently updated name from one of the
associated RawContacts. The "most recently updated name" is the name field that was last updated
by the user when editing in the Contacts app, which is irrelevant to its value. It does not matter
if the user deleted the last character of the name, added the same character back, and then saved. 
It still counts as the most recently updated. 

All of the above only applies to API 21 and above.

**Display name resolution is different for APIs below 21 (pre-Lollipop)!**

The `ContactsColumns.NAME_RAW_CONTACT_ID` was added in API 21. It changed the way display names
are resolved when linking, which is what has been described so far.

Before this change (APIs 20 and below), the native Contacts app is still able to set the Contact
display name somehow. I'm not sure how. If someone figures it out, please let me know. I tried 
updating the Contact DISPLAY_NAME directly but it does not work. Setting a name row as default also
does not affect the Contact DISPLAY_NAME.

### RawContacts; Accounts + Contacts

The RawContacts table links the Contact to the `android.accounts.Account` that it belongs to. 

Each new RawContacts row created results in;

- a new row in the Contacts table (unless the RawContact is associated to another existing Contact)
- a new row in the RawContacts with account name and type set to null
- 0 or more rows in the Data table with a reference to the new Contacts and RawContacts Ids

> It is possible to create RawContacts without any rows in the Data table. See the **Data required**
> section for more details.

For example, creating 4 new contacts using the native Android Contacts app results in;

```
Contact id: 4, displayName: First Local Contact
Contact id: 5, displayName: Second Local Contact
Contact id: 6, displayName: Third Local Contact
Contact id: 7, displayName: Third Local Contact
RawContact id: 4, accountName: null, accountType: null
RawContact id: 5, accountName: null, accountType: null
RawContact id: 6, accountName: null, accountType: null
RawContact id: 7, accountName: null, accountType: null
Data id: 15, rawContactId: 4, contactId: 4, data: First Local Contact
Data id: 16, rawContactId: 5, contactId: 5, data: Second Local Contact
Data id: 17, rawContactId: 6, contactId: 6, data: Third Local Contact
Data id: 18, rawContactId: 7, contactId: 7, data: Third Local Contact
```

**Local Contacts / RawContacts**

RawContacts inserted without an associated account are considered local or device-only raw contacts,
which are not synced.

The native Contacts app hides the following UI fields when inserting or updating local raw contacts;
- Event
- Relation
- Group memberships
To enforce this behavior, this library ignores all of the above during inserts and updates for local
raw contacts.

**Lollipop (API 22) and below**

When an Account is added, from a state where no accounts have yet been added to the system, the
Contacts Provider automatically sets all of the null `accountName` and `accountType` in the
RawContacts table to that Account's name and type;

```
RawContact id: 4, accountName: vestrel00@gmail.com, accountType: com.google
RawContact id: 5, accountName: vestrel00@gmail.com, accountType: com.google
RawContact id: 6, accountName: vestrel00@gmail.com, accountType: com.google
RawContact id: 7, accountName: vestrel00@gmail.com, accountType: com.google
```

RawContacts inserted without an associated account will automatically get assigned to an account if
there are any available. This may take a few seconds, whenever the Contacts Provider decides to do
it. Dissociating RawContacts from Accounts will result in the Contacts Provider associating those
back to an Account.

**Marshmallow (API 23) and above**

The Contacts Provider no longer associates local contacts to an account when an account is or
becomes available. Local contacts remain local.

**Account removal**

Removing the Account will delete all of the associated rows in the Contact, RawContact, Data, and
Groups tables locally. This includes user Profile data in those tables.

**SyncColumns modifications**

This library supports modifying the `SyncColumns.ACCOUNT_NAME` and `SyncColumns.ACCOUNT_TYPE` of the
RawContacts table in some cases only. In some cases does not work as intended and produces unwanted
side-effects. It probably has something to do with syncing with remote servers and local Account /
sync data not matching up similar to errors on network requests if the system time does not match
network time.

The motivation behind changing the Account columns of the RawContacts table rows is that it would
allow users to;

- Associate local RawContacts (those that are not associated with an Account) to an Account,
  allowing syncing between devices.
- Dissociate RawContacts from their Account such that they remain local to the device and not synced
  between devices.
- Transfer RawContacts from one Account to another.

When modifying the SyncColumns directly, the first works as intended. The second works with some
unwanted side-effects. The third does not work at all and produces unwanted side-effects.

These are the behaviors that I have found;

- Associating local RawContact A to Account X.
    - Works as intended.
    - RawContact A is now associated with Account X and is synced across devices.
- Dissociating RawContact A (setting the SyncColumns' Account name and type to null) from Account X.
    - Partially works with some unwanted-side effects.
    - Dissociates RawContact A from the device but not other devices.
    - RawContact A is no longer visible in the native Contacts app UNLESS it retains the group
      membership to at least the default group from an Account.
    - At this point, RawContact A is a local contact. Changes to this local RawContact A will not be
      synced across devices.
    - If RawContact A is updated in another device and synced up to the server, then a syncing
      side-effect occurs because the RawContact A in the device is different from the RawContact A
      in the server. This causes the Contacts Provider to create another RawContact, resulting in a
      "duplicate". The two RawContact As may get aggregated to the same Contact depending on how
      similar they are.
    - If local RawContact A is re-associated back to Account X, it will still no longer be synced.
- Associating RawContact A from original Account X to Account Y.
    - Does not work and have bad side-effects.
    - No change in other devices.
    - For Lollipop (API 22) and below, RawContact A is no longer visible in the native Contacts app
      and syncing Account Y in system settings fails.
    - For Marshmallow (API 23) and above, RawContact A is no longer visible in the native Contacts
      app. RawContact A is automatically deleted locally at some point by the Contacts Provider.
      Syncing Account Y in system settings succeeds.

Given that associating originally local RawContacts to an Account is the only thing that actually
works, it is the only function that will be exposed to consumers.

If consumers want to transfer RawContacts from one Account to another, they can create a copy of a
RawContact associated with the desired Account and then delete the original RawContact. Same idea
can be used to transform an Account-associated RawContact to a local RawContact. Perhaps we can
implement some functions in this library that does these things? We won't for now because the native
Contacts app does not support these functions anyways. It can always be implemented later if the
community really wants.

Here are some other things to note.

1. The Contacts Provider automatically creates a group membership to the default group of the target
   Account when the account changes. This occurs even if the group membership already exists
   resulting in duplicates.
2. The Contacts Provider DOES NOT delete existing group memberships when the account changes.
   This has to be done manually to prevent duplicates.

### RawContacts; Deletion

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
row. In this case, it is marked as deleted and its reference to a contact id is nulled.

### Multiple RawContacts Per Contact

Each row in the Contacts table may be associated with more than one row in the RawContacts table. 
The Contacts Provider may consolidate multiple contacts belonging to different accounts and combine 
them into a single entry in the Contacts table whilst maintaining the separate entries in the 
RawContacts table.

A more likely scenario that causes multiple RawContacts per Contact is when two or more Contacts are
"linked" (or "merged" for API 23 and below, or "joined" for API 22 and below).

### Behavior of linking/merging/joining contacts (AggregationExceptions)

> The Contacts app terminology has changed over time;
>   - API 22 and below; join / separate
>   - API 23; merge / unmerge
>   - API 24 and above; link / unlink 
> 
> However, the internals have not changed; KEEP_TOGETHER / KEEP_SEPARATE.
> 
> These operations are supported by the `ContactsContract.AggregationExceptions`.

Given the following tables;

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
difference is that Contact X's display name will be set to Contact Y's display name, which is
done by the native Contacts app manually by setting Contact Y's Data name row to be the "default" 
(isPrimary and isSuperPrimary both set to 1).

> The AggregationExceptions table records the linked RawContacts's IDs in ascending order regardless
> of the order used in RAW_CONTACT_ID1 and RAW_CONTACT_ID2 at the time of merging.

The RawContacts and Data table remains the same except the joined contactId column values have now
been changed to the id of Contact X. All Data rows' isSuperPrimary value has been set to 0 though 
the isPrimary columns remain the same. In other words, this clears any "default" set before the 
link. These are done automatically by the Contacts Provider during the link operation.

What is not done automatically by the Contacts Provider is that the name row of former Contact X is
set as the default. The native Contacts app does this manually. The Contacts Providers automatically
sets the Contact display name to whatever the default name row is for the Contact, if available.
For more info on Contact display name resolution, read the **Contact Display Name and Default Name
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

In the native Contacts app, Data inserted in combined contacts mode will be associated to the first
RawContact in the list sorted by the RawContact ID. 

> This may not be the same as the RawContact referenced by `ContactsColumns.NAME_RAW_CONTACT_ID`.

**UI changes?**

The native Contacts App does not display the groups field when displaying / editing Contacts that
have multiple RawContacts (linked/merged/joined) in combined mode. However, it does allow editing 
individual RawContact Data rows in which case the groups field is displayed and editable.

In the native Contacts app, the name attribute used comes from the name row with IS_SUPER_PRIMARY
set to true. This and all other "unique" mimetypes (organization) and non-unique mimetypes (email)
per RawContact are shown only if they are not blank.

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
(TYPE_KEEP_SEPARATE).

### Data Table

The Data table uses generic column names (e.g. "data1", "data2", ...) using the column "mimetype" to
distinguish the type of data in that generic column. For example, the column name of 
`StructuredName.DISPLAY_NAME` is the same as `Email.ADDRESS`, which is "data1". 

Each row in the Data table consists of a piece of RawContact data (e.g. a phone number), its
"mimetype", and the associated RawContact and Contact id. A row does not contain all of the data for
a contact.

RawContacts may only have one row of certain mimetypes and may have multiple rows of other 
mimetypes. Here is the list.

**Unique mimetype per RawContact**

- Name (StructuredName)
- Nickname
- Note
- Organization
- Photo
- SipAddress

**Non-unique mimetype per Raw Contact**

- Address (StructuredPostal)
- Email
- Event
- GroupMembership
- Im
- Phone
- Relation
- Website

Although some mimetypes are unique per RawContact, none of those mimetypes are unique per Contact
because a Contact is an aggregate of one or more RawContacts!

### Data Primary and Super Primary Rows

As per documentation, for a set of data rows with the same mimetype (e.g. a set of emails), there 
should only be one primary data row (e.g. email) per RawContact and one super primary data row per
Contact. Furthermore, a data row that is super primary must also be primary.

Unfortunately, the Contacts Provider does not do any data set validation for the Data columns 
`IS_PRIMARY` and `IS_SUPER_PRIMARY`. This means that it is possible to set more than one data row of
the same mimetype as primary for the same RawContact and super primary for the same aggregate
Contact. It is also possible to set a data row as super primary but not primary. Upholding the the
contract is left to us... 

For example, given this relationship;

- Contact
    - RawContact X
        - Email A
        - Email B
    - RawContact Y
        - Email C
        - Email D
        
When Emails A, B, C, and D are inserted with the RawContacts or after the RawContacts have been 
created, we get the following state;

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 0           | 0                 |
| C         | 0           | 0                 |
| D         | 0           | 0                 |


The state does not change when RawContact X is linked with RawContact Y.

After setting Email A as the "default" email, it becomes primary and super primary;

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 1           | 1                 |
| B         | 0           | 0                 |
| C         | 0           | 0                 |
| D         | 0           | 0                 |

Then setting Email B as the default email, it becomes primary and super primary. Email A is no
longer primary or super primary.

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 1           | 1                 |
| C         | 0           | 0                 |
| D         | 0           | 0                 |

Then setting Email C as the default email, it becomes primary and super primary. Email B is still
primary because it belongs to a different RawContact than Email C. However, Email B is no longer the
super primary as there can only be one per aggregate Contact.

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 1           | 0                 |
| C         | 1           | 1                 |
| D         | 0           | 0                 |

Then setting Email D as the default email, it becomes primary and super primary. Email C is no 
longer primary or super primary.

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 1           | 0                 |
| C         | 0           | 0                 |
| D         | 1           | 1                 |

Then clearing the default email D, removes its primary and super primary status. However, email B 
remains a primary but not a super primary.

| **Email** | **Primary** | **Super Primary** |
|-----------|-------------|-------------------|
| A         | 0           | 0                 |
| B         | 1           | 0                 |
| C         | 0           | 0                 |
| D         | 0           | 0                 |

The above behavior is observed from the native Contacts app. The "super primary" data of an 
aggregate Contact is referred to as the "default".

> At this point, the native Contacts app still shows email B as the first email in the list even
> though it isn't the "default" (super primary) because it is still a primary. This adds a bit of
> confusion in my opinion, especially when more than 2, 3, or 4 RawContacts are linked. A "fix" 
> would be to only order the list of emails using "super primary" instead of "super primary" and 
> "primary". OR to remove the primary status of the data set of all linked RawContacts.
> 
> One benefit of the native Contacts implementation of this is that it retains the primary status
> when unlinking RawContacts. 
> 
> This library should follow what the native Contacts app is doing in spirit of recreating the
> native experience as closely as possible, even if it seems like a lesser experience.

### Data Table Joins

All columns accessible via cursors returned from Data table queries are specified in
`DataColumnsWithJoins`, which includes the `DataColumns`, `ContactsColumns`, and
`ContactOptionsColumns`. In code, mentions of the "Data table" typically refers to the joined table.

The `DataColumns` gives us access to all of the columns in the Data table. All other joined columns,
including the `ContactsColumns` are appended to each row in the query. This means that the 
`ContactsColumns`; `DISPLAY_NAME`, `PHOTO_URI`, and `PHOTO_THUMBNAIL_URI` are repeated for all Data
rows belonging to the same Contact.

The `ContactOptionsColumns` values joined with the Data table are the values of the Contact, not
the RawContact that the Data row belongs to! The same applies to the "display_name".

### Data Updates

A new row in the Data table is created for each new piece of data (e.g. email address) entered for 
the contact. 

Removing a piece of existing data results in the deletion of the row in the Data table if that row
no longer contains any meaningful data (no meaningful non-null "datax" columns left). This is the 
behavior of the native Android Contacts app. Therefore, querying for null fields is not possible. 
For example, there may be no Data rows that exist where the email address is null. Thus, a query to 
search for all contacts with null email address may return 0 contacts even if there are some
contacts without email addresses.

### Data Required

Creating blank RawContacts without email address (or other fields), results in no rows in the Data
table for the email address, and all other fields. There are a few exceptions. The following 
Data rows are automatically created for all contacts, if not provided;

- Group membership, underlying value defaults to the account's default system group
- Name, underlying value defaults to null
- Nickname, underlying value defaults to null
- Note, underlying value defaults to null

> Note that all of the above rows are only automatically created for RawContacts that are associated
> with an Account.

If a valid account is provided, the default (auto add) system group membership row is automatically
created immediately by the Contacts Provider at the time of contact insertion. The name, nickname,
and note are automatically created at a later time.

If a valid account is not provided, none of the above data rows are automatically created.

**Blank RawContacts**

The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call them
"blanks") to exist. The native Contacts app does not allow insertion of new RawContacts without at
least one data row. It also deletes blanks on update. Despite seemingly not allowing blanks, the
native Contacts app shows them.

There are two scenarios where blanks may exist.

1. Contact with RawContact(s) with no Data row(s).
    - In this case, the Contact is blank as well as its RawContact(s).
2. Contact that has RawContact with Data row(s) and a RawContact with no Data row.
    - In this case, the Contact and the RawContact with Data row(s) are not blank but the RawContact
    with no Data row is blank.

### Data `StructuredName`

The `DISPLAY_NAME` is the unstructured representation of the name. It is made up of structured
components; `PREFIX`, `GIVEN_NAME`, `MIDDLE_NAME`, `FAMILY_NAME`, and `SUFFIX`.

When updating or inserting a row;

- If the display name is null and there are non-null structured components provided (e.g. given and
  family name), the Contacts Provider will automatically set the display name by combining the
  structured components.
- If the display name is not null and all structured components are null, the Contacts Provider
  automatically (to the best of its ability) derive the values for all the structured components.
- If the display name and structured components are not null, the Contacts Provider does nothing
  automatically.

### Data `StructuredPostal`

The `FORMATTED_ADDRESS` is the unstructured representation of the postal address. It is made up of
structured components; `STREET`, `POBOX`, `NEIGHBORHOOD`, `CITY`, `REGION`, `POSTCODE`, and
`COUNTRY`.

When updating or inserting a row;

- If the formatted address is null and there are non-null structured components provided (e.g.
  street and city), the Contacts Provider will automatically set the formatted address by combining
  the structured components.
- If the formatted address is not null and all structured components are null, the Contacts Provider
  automatically sets the street value to the formatted address.
- If the formatted address and structured components are not null, the Contacts Provider does
  nothing automatically.

### Groups Table & Accounts

Contacts are assigned to one or more groups via the `GroupMembership`. It typically looks like this;

```
Group id: 1, systemId: Contacts, readOnly: 1, title: My Contacts, favorites: 0, autoAdd: 1, accountName: vestrel00@gmail.com, accountType: com.google
Group id: 2, systemId: null, readOnly: 1, title: Starred in Android, favorites: 1, autoAdd: 0, accountName: vestrel00@gmail.com, accountType: com.google
Group id: 3, systemId: Friends, readOnly: 1, title: Friends, favorites: 0, autoAdd: 0, accountName: vestrel00@gmail.com, accountType: com.google
Group id: 4, systemId: Family, readOnly: 1, title: Family, favorites: 0, autoAdd: 0, accountName: vestrel00@gmail.com, accountType: com.google
Group id: 5, systemId: Coworkers, readOnly: 1, title: Coworkers, favorites: 0, autoAdd: 0, accountName: vestrel00@gmail.com, accountType: com.google
Group id: 6, systemId: null, readOnly: 0, title: Custom Group, favorites: 0, autoAdd: 0, accountName: vestrel00@gmail.com, accountType: com.google
`````

The actual groups are in a separate table; Groups. Each group is associated with an Account. No
group can exist without an account. It is account-exclusive.

Each account will have its own set of the above system groups. This means that there may be multiple
groups with the same title belonging to different accounts.

System ids are typically Contacts, Friends, Family, and Coworkers. These ids are typically the same
across all copies of Android. Notes;
- The Contacts system group is the default group in which all raw contacts of an account belongs to.
  Therefore, it is typically hidden when showing the list of groups in the UI.
- The starred (favorites) group is not a system group as it has null system id. However, it behaves
  like one in that it is read only and it comes with most (if not all) copies of the native app.

Removing the Account will delete all of the associated rows in the Groups table.

**Groups, duplicate titles**

The Contacts Provider allows multiple groups with the same title (case-sensitive comparison) 
belonging to the same account to exist. In older versions of Android, the native Contacts app 
allows the creation of new groups with existing titles. In newer versions, duplicate titles are not 
allowed. Therefore, this library does not allow for duplicate titles.

In newer versions, the group with the duplicate title gets deleted either automatically by the 
Contacts Provider or when viewing groups in the native Contacts app. It's not an immediate failure 
on insert or update. This could lead to bugs!

### Groups Table & GroupMemberships (Data Table)

There may be multiple groups with the same title from different accounts. Therefore, the group
membership should point to the group belonging to the same account as the raw contact. The native
Contacts app displays only the groups belonging to the selected account.

Updating group memberships of existing raw contacts seem to be almost instant. All raw contacts must
be a part of at least the default group (system id is "Contacts"). Raw contacts with no group
membership will be asynchronously added to the Account's default group by the Contacts Provider.

Membership to the default group should never be deleted!

### Starred in Android (Favorites)

When the `ContactOptionsColumns.STARRED` column of a Contact in the Contacts table is set to true,
the Contacts Provider automatically adds a group membership to the favorites group for all 
RawContacts linked to the Contact. Setting `STARRED` to false removes all group memberships to the
favorites group.

> If the RawContact is not associated with an Account, then no group memberships that are created.

The `STARRED` is interdependent with group memberships to the favorites group. Adding a group 
membership to the favorites group results in `STARRED` being set to true. Removing the membership 
sets it to false.

Raw contacts that are not associated with an account do not have any group memberships. Even though
these raw contacts may not have a membership to the favorites group, they may still be "starred"
(favorited) via the `ContactOptionsColumns.STARRED` column in the Contacts table, which is not
dependent on the existence of a favorites group membership.

**Refresh RawContact instances after changing the starred value.** Otherwise, performing an update 
on the RawContact with a stale set of group memberships may revert the star/unstar operation. For 
example, query returns a starred RawContact -> set starred to false -> update RawContact (still 
containing a group membership to the favorites group) -> starred will be set back to true.

### Group memberships & Local RawContacts

Local RawContacts may have a group membership to the default system group of an Account without
being associated with the Account...

The native Contacts app may not have an edit-RawContact option for newly inserted RawContacts that
have no group membership to the default group when an Account is available. Though, edits can still
be made in other ways. Instead, an option to "Add to contacts" is shown that adds a membership to
the default group but does not associate the raw contact to the Account that owns the group. The
edit UI does not show the group membership field.

Weirdly, this only occurs when there is exactly only one Account. If there are no Accounts or there
are two or more Accounts, then this does not occur. Also, this does not occur for a Contact with a
RawContact that has a group membership AND a RawContact that has no group membership.

### Groups; Deletion

Prior to Android 8.0 (Oreo, API 26), group deletion is unpredictable. Groups that are marked for
deletion remain in the DB and is still shown in the native Contacts app. Sometimes they do get
deleted at some point but the trigger for the actual deletion eludes me.

The native Contacts app (prior to API 26) does NOT support group deletion perhaps because groups
syncing isn't implemented or at least not to the same extent as contacts syncing. Therefore, this
library will also not support group deletion for API versions lower than 26.

### Groups; UI

In newer Android versions of the native Contacts app, "groups" are now being referred to as
"labels". However, the underlying code still uses groups. Google is probably just trying to make it
more user friendly by calling it label instead of group.

### User Profile

There exist one (profile) Contacts row that identifies the user;
`ContactsColumns.IS_USER_PROFILE`. There is at least one RawContacts row that is associated with the
user profile; `RawContactsColumns.RAW_CONTACT_IS_USER_PROFILE`. Associated RawContacts may or may
not be associated with an Account. The RawContacts row(s) may have rows in the Data table as usual.
These profile table rows have special IDs that differ from regular rows. See  
`ContactsContract.isProfileId`.

> Note that the Contacts Provider will throw an IllegalArgument exception when attempting to include
> `ContactsColumns.IS_USER_PROFILE` and `RawContactsColumns.RAW_CONTACT_IS_USER_PROFILE` columns
> in Data table queries. I have not yet tried including these columns in the Contacts or RawContacts
> table queries.

The profile Contact row may not be merged / linked with other contacts and do not belong to any
group (favorites / starred).

Profile rows in the Contacts, RawContacts, and Data table are not visible via queries in the
respective tables. They will not be in the resulting cursor. To get the profile Contacts table rows,
query the `Profile.CONTENT_URI`. To get profile RawContacts table rows, query the
`Profile.CONTENT_RAW_CONTACTS_URI`. To get the profile Data table rows, query the
`Profile.CONTENT_RAW_CONTACTS_URI` appended with the RawContact id and
`RawContacts.Data.CONTENT_DIRECTORY`.

To insert a new profile RawContact, use `Profile.CONTENT_RAW_CONTACTS_URI`. It will automatically
be associated with the profile Contact. If the profile Contact does not yet exist, it will be  
created automatically.

To insert a new profile Data row, either;

- insert to the `Profile.CONTENT_RAW_CONTACTS_URI` appended with the RawContact id and
`RawContacts.Data.CONTENT_DIRECTORY`
- insert to the Data table directly, referencing the RawContact id

Same rules apply to all table rows. If all profile RawContacts table rows have been deleted, then
associated Contacts and Data table rows will automatically be deleted.

**Profile aggregation**

The RawContacts of a (Contact) Profile are linked via the indexed rows;
`Profile.CONTENT_RAW_CONTACTS_URI`. Therefore, the AggregationsExceptions table is not used here.

**Profile and users**

Note that as of Android 5 Lollipop, there may exist multiple users in a device. Each user has a
separate list of accounts and contact data. This also means that each user has a separate (local)
profile contact.

**Profile and Accounts**

According to the `Profile` documentation; "... each account (including data set, if applicable) on
the device may contribute a single raw contact representing the user's personal profile data from
that source."

In other words, one account can have one profile RawContact. Whether or not profile RawContacts
associated to an Account can be carried over and synced across devices and users is up to the
Contacts Provider / Sync provider for that Account.

> From my experience, profile RawContacts associated to an Account is not carried over / synced
> across devices or users.

Despite the documentation of "one profile RawContact per one Account", the Contacts Provider allows
for multiple RawContacts per Account, including multiple local RawContacts (no Account). Thus, we
should let consumers exploit this but set defaults to be one-for-one.

Creating / setting up the profile in the native Contacts app results in the creation of a local
RawContact (not associated with an Account) even if there are available Accounts.

The Contacts Provider does not associate local contacts to an account when an account is or becomes
available (regardless of API level).

Removing the Account will delete all of the associated rows in the Contact, RawContact, Data, and
Groups tables. This includes user Profile data in those tables.

**Profile permissions**

Profile permissions (READ_PROFILE and WRITE_PROFILE) have been removed since API 23. However, they
are still required for API 22 and below. Reading and writing the profile is included in the Contacts
permissions. There is no need to ask for profile permissions at runtime because prior to API 23,
permissions in the AndroidManifest have to be accepted prior to installation.

### Syncing Data / Sync Adapters
First, it’s good to know the official documentation of sync adapters; 
https://developer.android.com/guide/topics/providers/contacts-provider#SyncAdapters

Now, let’s ingest the official docs… Data belonging to a RawContact that is associated with a Google 
account will be available anywhere the Google account is used; in any Android or iOS device, a web 
browser, etc… Data is synced by Google’s sync adapters to and from their remote servers. Syncing 
depends on the account sync settings, which can be configured in the native system settings app and 
possibly through some remote configuration.

This library does not provide any sync adapters. Instead, it relies on existing sync adapters to do 
the syncing. Sync adapters and syncing are really out of scope of this library. Syncing is its own 
thing that typically happens outside of an application UI. This library is focused on reading and 
writing native and custom data to and from the local database. Syncing the local database to and 
from a remote service is a different story altogether =)

### Custom Data / MimeTypes

First, it’s good to know the official documentation of custom data rows; 
https://developer.android.com/guide/topics/providers/contacts-provider#CustomData

Now, let’s ingest the official docs… Custom mimetypes do not belong to the native Contacts Provider 
mimetype set (e.g. address, email, phone, etc). The Contacts Provider allows for the creation of 
new / custom mimetypes. This is especially useful for social media apps (Facebook, Twitter, 
WhatsApp, etc) that want to attach extra pieces of data to a particular RawContact.

Custom data are NOT synced, including those that belong to RawContacts that are associated with an 
Account. Custom sync adapters are required to sync custom data. This library currently does NOT 
provide custom sync adapters to sync custom data!

Custom data from other apps such as Facebook, Twitter, WhatsApp, etc may or may not be synced. It 
all depends on those applications and their custom sync adapters (if they have any) and sync settings.

For insight on how aforementioned social media services may be syncing their data, read through the 
official documentation; https://developer.android.com/guide/topics/providers/contacts-provider#SocialStream

### Unused ContactsContract Stuff

We are currently not utilizing these things because I haven't found usages of them while using the
native Contacts app. They are probably working behind the scenes but until we find uses for these,
let's leave it out because YAGNI.

- `Settings`. Contacts-specific settings for various Accounts (settings for an Account).
    - Might be useful to add this for `SHOULD_SYNC` and `UNGROUPED_VISIBLE`.
- `ContactsColumns.IN_VISIBLE_GROUP` + `Groups.GROUP_VISIBLE`. Flag indicating if the contacts
   belonging to this group should be visible in any user interface.

## Java Support

This library is intended to be Java-friendly. The policy is that we should attempt to write 
Java-friendly code that does not increase lines of code by much or add external dependencies to 
cater exclusively to Java users.

## Creating Entities & data class

First, consumers are not allowed to create immutable entities. Those must come from the API itself
to ensure data integrity. Whether or not we will change this in the future is debatable =)

Consumers are able to set read-only and private or internal variables though because all `Entity` 
implementations are data classes. Data classes provide a `copy` function that allows for setting any
property no matter their visibility and even if the constructor is private. As a matter of fact, 
setting the constructor of a `data class` as `private` gives this warning by Android Studio: 
"Private data class constructor is exposed via the 'copy' method.

There is currently no way to disable the `copy` function of data classes (that I know of). The only
thing we can do is to provide documentation to consumers, insisting against the use of the `copy`
method as it may lead to unwanted side effects when updating and deleting contacts.

> We could just use regular classes instead of data classes but entities should be data classes
> because it is what they are (know what I mean?!). Also, I'd hate to have to generate equals and
> hashcode functions for them, which will make the code harder to maintain. Though, we might do this
> anyways at some point if we want to make it possible for a mutable entity to equal an immutable
> entity. Time will tell =)
> FIXME? Hide / disable data class `copy` function if kotlin ever allows it. 
> https://discuss.kotlinlang.org/t/data-class-copy-visibility-modifier/19746

## Immutable vs Mutable Entities

This library provides **true immutability** for immutable entities.

Take a look at the current (simplified) hierarchy;

```kotlin
sealed interface ContactEntity {
    val rawContacts: List<RawContactEntity>
}
data class Contact(
    override val rawContacts: List<RawContact>
) : ContactEntity
data class MutableContact(
    override val rawContacts: List<MutableRawContact>
) : ContactEntity

sealed interface RawContactEntity
data class RawContact(
    val addresses: List<Address>
) : RawContactEntity
data class MutableRawContact(
    val addresses: MutableList<MutableAddress>
) : RawContactEntity

data class Address(
    val formattedAddress: String?
)
data class MutableAddress(
    var formattedAddress: String?
)
```

> Note the use of `sealed class` is to prevent consumers from defining their own entities. This
> restriction may or may not change in the future.

Notice that there is nothing mutable in the immutable `Contact`. Everything are `val`s and the data
structures used (i.e. `RawContact`, `Address`, and `List`) are all immutable. This provides
consumers 100% confidence that immutable entities are not mutable. They will not change or mutate
in any way. Once they are constructed, they will always remain the same.

Why immutability is so important will not be covered in this dev notes because it would be too big
(that's what she said) and there are blogs and books written about this. One of the most important
advantages of immutability is that it is thread-safe. Immutable instances can be used in several
different threads without the need for synchronization and worries about deadlocks. In other words,
they are thread-safe and faster than the mutable version.

The current structure also allows consumers to be able to distinguish between immutable and mutable
entities exhaustively. E.G.

```kotlin
fun doSomethingAndReturn(contact: ContactEntity) = when (contact) {
    is Contact -> {}
    is MutableContact -> {}
}
```

> Note that the **mutable entities provided in this library are NOT thread-safe**. Consumers will
> have to perform their own synchronizations if they want to use and mutate mutable entities in
> multi-threaded scenarios.

### The cost of the current immutability implementation

The cost of implementing true immutability is more lines of code. Notice that the `MutableContact`
does not inherit from `Contact`. The same goes for the other entities. This leads to having to write
seemingly duplicate code when writing functions and extensions.

// FIXME? Furthermore, equality between immutable and mutable entities are not yet implemented. This
means that `Contact("john") == MutableContact("john")` will return false even though their
underlying contents are the same. This can be fixed by overriding the equals and hashcode functions
of all entities. However, that is a lot more code that I would like to avoid, which is why I'm
using `data class` for all entities in the first place! This may change in the future if the
community really wants to change it =)

On a side note, the same cost is incurred by Kotlin's standard libs. For example, notice that
`AbstractMutableList` does not inherit from and is completely separate from `AbstractList`. I'm sure
stdlib devs also had to write seemingly duplicate code in implementations of the `List` interface.

### Avoiding the cost... Shortcuts and pitfalls.

One thing that may come to mind in attempts to reduce lines of seemingly duplicate code is to have
just a mutable implementation of an immutable declaration. For example, we can restructure the
hierarchy to;

```kotlin
sealed interface Contact {
    val rawContacts: List<RawContact>
}
data class MutableContact(
    override val rawContacts: List<MutableRawContact>
) : Contact

sealed interface RawContact {
    val addresses: List<Address>
}
data class MutableRawContact(
    override var addresses: MutableList<MutableAddress>
) : RawContact

sealed interface Address {
    val formattedAddress: String?
}
data class MutableAddress(
    override var formattedAddress: String?
) : Address
```

Notice that there is a non-concrete declaration (i.e. `Contact`, `RawContact`, and `Address`) and
just one concrete implementation (i.e. `MutableContact`, `MutableRawContact`, and `MutableAddress`).

> Note that a `val` declaration can be overridden by a `var`. Keep in mind that `val` only requires
> getters whereas `var` requires both getters and setters. Therefore, a `var` cannot be overridden
> by a `val`. Or maybe there is a different reason Kotlin imposes this restriction =)
> On a similar note, the `List` interface can be overridden to a `MutableList`.

We, as API contributors, can avoid having to write seemingly duplicate functions and extensions!

However! Can you see what's wrong with this setup? If we do this, we would either be deceiving
consumers to think that the instances of "immutable" class signatures (i.e. `Contact`, `RawContact`,
and `Address`) are actually immutable OR we would have to let consumers know that the API does not
really provide true immutability. Neither option is ideal (nor is it acceptable IMO).

Consumers would have a reference to a `Contact`, which they may assume is immutable because of the
usage of `val` instead of `var`, but in actuality the underlying implementation is mutable... This
could be a cause of really hard to find bugs in multi-threaded usage. Consumers may use `Contact`
with the assumption that it is immutable only to find that it can actually be mutated! We could
fix this by just making the mutable implementation thread-safe but since that is the only
implementation, consumers will be forced to use thread-safe code when they don't have to thereby
negatively affecting performance.

Keep in mind that thread safety is only one of several reasons for immutability. Those other
reasons will be violated too.

Consumers will be shocked if they ever do the following or something similar.

```kotlin
fun x(contact: Contact) = when(contact) {
    is MutableContact -> {} // this is always true
    is Contact -> {} // this is always true
}
```

In any case, I have to admit, it is a nice trick that would save API contributors time. But that's
just it! It's just a trick. A shortcut. A nice little time save at the cost of integrity. It is not
worth it (IMO).

## Why Not Add Android X / Support Library Dependencies?

I want to keep the dependency list of this library to a minimum. The Contacts Provider is native to
Android since the beginning. I want to honor that fact by avoiding adding dependencies here. I made
a bit of an exception by adding the [Dexter library][2] for permissions handling for the permissions
modules (not in the core modules). I'm tempted to remove the Dexter dependency and implement 
permissions handling myself because Dexter brings in a lot of other dependencies with it. However,
it is not part of the core module so I'm able to live with this.

TODO Remove/replace Dexter. It is no longer being maintained.

Keeping dependencies to a minimum is just a small challenge I made up. We will see how long it can
last!

I left comments all over the code on when an androidx dependency may be useful. The most glaring
example of this is @WorkerThread. Even with that, I'll hold off on adding the androidx annotation
lib. I think we can all be consenting adults =)

If the community strongly desires the addition of these support libs, then the community will win =)

[1]: https://developer.android.com/guide/topics/providers/contacts-provider
[2]: https://github.com/Karumi/Dexter
[3]: https://developer.android.com/training/contacts-provider/retrieve-names#GeneralMatch
