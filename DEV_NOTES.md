# Developer Notes

This document contains useful developer notes that should be kept in mind during development. It 
serves as a memory of all the quirks and gotcha's of things like Android's `ContactsContract`.

> I thought about putting this in CONTRIBUTING.md but I think it's better that it is a separate 
document.

## Contacts Provider

It is important to know about the ins and outs of Android's Contacts Provider. After all, this API 
is just a wrapper around it. 

> A very sweet, sugary wrapper! Sugar. Spice. And everything nice. :D

It is important to get familiar with the [official documentation of the Contact's Provider]{1]. 

Here is a summary;

There are 3 database tables used in dealing with contacts;

1. Contacts
2. RawContacts
3. Data

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

#### Contacts; Display Name

The Contacts display name may be different than the Data `StructuredName` display name! If a 
structured name in the Data table is not provided, then other kinds of data will be used as the 
`Contacts` row display name. For example, if an email is provided but no structured name then the
display name will be the email. When a structured name is inserted, the Contacts Provider 
automatically updates the Contacts row display name.

If no data rows suitable to be a display name are available, then the Contacts row display name will
be null. Data suitable to be a Contacts row display name are enumerated in `DisplayNameSources`;
 
- company name
- email address
- nickname
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

#### Contact Display Name and Default Name Rows

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

#### RawContacts; Accounts + Contacts

The RawContacts table links the Contact to the `android.accounts.Account` that it belongs to. 

When there are no available Accounts in the device, each new RawContacts row created results in;

- a new row in the Contacts table (unless the RawContact is associated to another existing Contact)
- a new row in the RawContacts with account name and type set to null
- new row(s) in the Data table with a reference to the new Contacts and RawContacts Ids

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

When an Account is added, all of the null `accountName` and `accountType` in the RawContacts table 
are set to that Account's name and type;

```
RawContact id: 4, accountName: vestrel00@gmail.com, accountType: com.google
RawContact id: 5, accountName: vestrel00@gmail.com, accountType: com.google
RawContact id: 6, accountName: vestrel00@gmail.com, accountType: com.google
RawContact id: 7, accountName: vestrel00@gmail.com, accountType: com.google
```

Removing the Account will delete all of the associated rows in the Contact, RawContact, and 
Data tables.

Note that all of these operations are not instantaneous! It make take a few seconds for the 
RawContacts to be updated of deleted.

Creating a new RawContacts row where the account name and type are null when there are available 
Accounts results in the RawContacts row to be updated by the Android Contacts Provider automatically
later on to have an existing Account's name and type. 

#### RawContacts; Deletion

Deleting a contact's Contacts row, RawContacts row(s), and associated Data row(s) are best explained
in the documentation in `ContactsContract.RawContacts`;

> When a raw contact is deleted, all of its Data rows as well as StatusUpdates, 
> AggregationExceptions, PhoneLookup rows are deleted automatically. 
> 
> When all raw contacts associated with a Contacts row are deleted, the Contacts  row itself is also 
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
row. In this case, it is marked as deleted and is effectively invisible.

**Contacts & RawContacts may be deleted automatically in some cases!**

When a RawContact does not have an associated Account, the RawContact row is automatically deleted
when all of its Data rows are deleted. On the contrary, when a RawContact has an associated Account,
the RawContact row remains when all of its Data rows are deleted.

This will, in turn, also delete the Contact if there are no more RawContacts belonging to it.

#### Multiple RawContacts Per Contact

Each row in the Contacts table may be associated with more than one row in the RawContacts table. 
The Contacts Provider may consolidate multiple contacts belonging to different accounts and combine 
them into a single entry in the Contacts table whilst maintaining the separate entries in the 
RawContacts table.

A more common scenario that causes multiple RawContacts per Contact is when two or more Contacts are
"linked" (or "merged" for API 23 and below, or "joined" for API 22 and below).

#### Behavior of linking/merging/joining contacts

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
#### Contacts table
Contact id: 32, displayName: X, starred: 0, timesContacted: 1, lastTimeContacted: 1573071785456, customRingtone: content://media/internal/audio/media/109, sendToVoicemail: 0
Contact id: 33, displayName: Y, starred: 1, timesContacted: 2, lastTimeContacted: 1573071750624, customRingtone: content://media/internal/audio/media/115, sendToVoicemail: 1

#### RawContacts table
RawContact id: 30, contactId: 32, accountName: x@x.com, accountType: com.google, starred: 0, timesContacted: 1, lastTimeContacted: 1573071785456, customRingtone: content://media/internal/audio/media/109, sendToVoicemail: 0
RawContact id: 31, contactId: 33, accountName: y@y.com, accountType: com.google, starred: 1, timesContacted: 2, lastTimeContacted: 1573071750624, customRingtone: content://media/internal/audio/media/115, sendToVoicemail: 1

#### Data table
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
#### Contacts table
Contact id: 32, displayName: X, starred: 1, timesContacted: 2, lastTimeContacted: 1573071785456, customRingtone: content://media/internal/audio/media/109, sendToVoicemail: 0

#### RawContacts table
RawContact id: 30, contactId: 32, accountName: x@x.com, accountType: com.google, starred: 0, timesContacted: 1, lastTimeContacted: 1573071785456, customRingtone: content://media/internal/audio/media/109, sendToVoicemail: 0
RawContact id: 31, contactId: 33, accountName: y@y.com, accountType: com.google, starred: 1, timesContacted: 2, lastTimeContacted: 1573071750624, customRingtone: content://media/internal/audio/media/115, sendToVoicemail: 1

#### Data table
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
the isPrimary columns remain the same. In other words, this clears any "default" (isPrimary and 
isSuperPrimary both set to 1) set before the link. These are done automatically by the Contacts 
Provider during the link operation.

What is not done automatically by the Contacts Provider is that the name row of former Contact X is
set as the default. The native Contacts app does this manually. The Contacts Providers automatically
sets the Contact display name to whatever the default name row is for the Contact, if available.
For more info on Contact display name resolution, read the **Contact Display Name and Default Name
Rows** section.

> Note that display name resolution is different for APIs below 21 (pre-lollipop).

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
set to true. This and all other "unique" mimetypes (company/organization) and non-unique mimetypes
(email) per RawContact are shown only if they are not blank.

#### AggregationExceptions table

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

#### Data Table

The Data table uses generic column names (e.g. "data1", "data2", ...) using the column "mimetype" to
distinguish the type of data in that generic column. For example, the column name of 
`StructuredName.DISPLAY_NAME` is the same as `Email.ADDRESS`, which is "data1". 

Each row in the Data table consists of a piece of contact data (e.g. a phone number), its 
"mimetype", and the associated contact id. A row does not contain all of the data for a contact. 

> A RawContact has one (could be 0 if no accounts are available) or more entries in the Data 
> `table`. A Contact has one or more associated RawContacts. All data belonging to all RawContacts
> of a Contact belong to that Contact via aggregation.

RawContacts may only have one row of certain mimetypes and may have multiple rows of other 
mimetypes. Here is the list.

**Unique mimetype per RawContact**

- Company (Organization)
- Name (StructuredName)
- Nickname
- Note
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

#### Data Primary and Super Primary Rows

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

#### Data Table Joins

All columns returned by the Data table are specified in `DataColumnsWithJoins`, which includes the
`DataColumns`, `ContactsColumns`, and `ContactOptionsColumns`. 

The `DataColumns` gives us access to all of the columns in the Data table. All other joined columns,
including the `ContactsColumns` are appended to each row in the query. This means that the 
`ContactsColumns`; `DISPLAY_NAME`, `PHOTO_URI`, and `PHOTO_THUMBNAIL_URI` are repeated for all Data
rows belonging to the same Contact.

The `ContactOptionsColumns` values joined with the Data table are the values of the Contact, not
the RawContact that the Data row belongs to!

#### Data Updates

A new row in the Data table is created for each new piece of data (e.g. email address) entered for 
the contact. 

Removing a piece of existing data results in the deletion of the row in the Data table if that row
no longer contains any meaningful data (no meaningful non-null "datax" columns left). This is the 
behavior of the native Android Contacts app. Therefore, querying for null fields is not possible. 
For example, there may be no Data rows that exist where the email address is null. Thus, a query to 
search for all contacts with null email address may return 0 contacts even if there are some
contacts without email addresses.

#### Data Required

Creating new RawContacts without email address (or other fields), results in no row in the Data 
table for the email address, and all other fields. There are a few exceptions. The following 
Data rows are automatically created for all contacts, if not provided;

- Group membership?, defaults to the account's default system group
- Name, defaults to null 
- Nickname, defaults to null 
- Note, defaults to null

If a valid account is provided, the default (auto add) group membership row is automatically created
immediately by the Contacts Provider at the time of contact insertion. The name, nickname, and note
are automatically created at a later time.

When there are no available accounts, none of the above data rows are automatically created. This is
a problem because then queries for all contacts will not return existing contacts that do not have 
any rows in the Data table. As a workaround, the RawContacts table is queried for all rows with a
non-null contact id.

#### Data `StructuredName`

The `DISPLAY_NAME` of the `StructuredName` row in the Data table is automatically set by the 
Contacts Provider by combining the other name elements; `GIVEN_NAME`, `FAMILY_NAME`, etc. 
For example, if given and family name is "vandolf" and "estrellado", then the display name 
will be set to "vandolf estrellado".

The inverse is also true. If the display name is provided but not the other elements 
(given name, family name, etc), then the Contacts Provider will automatically derive the other
values from the display name. For example, if the display name is set to "vandolf estrellado", 
then the given and family names are "vandolf" and "estrellado" respectively.

#### Data `StructuredPostal`

The `FORMATTED_ADDRESS` of the `StructuredPostal` row in the data table is automatically set by the 
Contacts Provider by combining the other address elements; `STREET`, `CITY`, etc. This is similar to
the `StructuredName`.

The inverse may not be true as the Contacts Provider does not seem to be able to derived the other
address elements from the `FORMATTED_ADDRESS`.

#### Groups Table & Accounts

Contacts are assigned to one or more groups via the `GroupMembership`. The actual groups are in a
separate table. It typically looks like this;

```
Group id: 1, systemId: Contacts, readOnly: 1, title: My Contacts, favorites: 0, autoAdd: 1
Group id: 2, systemId: null, readOnly: 1, title: Starred in Android, favorites: 1, autoAdd: 0
Group id: 3, systemId: Friends, readOnly: 1, title: Friends, favorites: 0, autoAdd: 0
Group id: 4, systemId: Family, readOnly: 1, title: Family, favorites: 0, autoAdd: 0
Group id: 5, systemId: Coworkers, readOnly: 1, title: Coworkers, favorites: 0, autoAdd: 0
Group id: 6, systemId: null, readOnly: 0, title: Custom Group, favorites: 0, autoAdd: 0
`````

> Note that the **ids will vary** as the user adds and removes accounts! Furthermore, each account
> will have its own set of the above groups. This means that there may be multiple groups with the
> same title belonging to different accounts.

The first 5 (this number depends on the OS / manufacturer) are system groups that are read-only.
Newly created contacts are automatically assigned to group 1 (notice autoAdd is true). Group 2
is usually the favorites group, though other custom groups can also be marked as favorites. Custom
groups created by users can be written to, deleted, set as favorites, and set to auto add.

Like RawContacts, creating a new Groups row where the account name and type are null when there are
available Accounts (or when an account becomes available) results in the Groups row to be updated by
the Contacts Provider automatically later on to have an existing Account's name and type. 

Removing the Account will delete all of the associated rows in the Groups table.

**Groups, no available accounts**

The native Contacts app does not display the groups field when creating or updating contacts when
there are no available accounts present. To enforce this behavior, this library does not allow
creation of groups without associated accounts.

**Groups, duplicate titles**

The Contacts Provider and the native Contacts app allows multiple groups with the same title 
belonging to the same account to exist. Therefore, this library also allows this behavior even 
though it is considered a bug for most consumers. If desired, it is up to consumers to protect 
against multiple groups from the same account having the same titles.

#### Groups Table & GroupMemberships (Data Table)

There may be multiple groups with the same title from different accounts. Therefore, the group
membership should point to the group belonging to the same account as the contact. The native 
Contacts app displays only the groups belonging to the selected account.

Updating group memberships of existing contacts seem to be almost instant. All contacts must be a 
part of at least the default contact group 1 (may vary). Contacts with no group membership will be 
asynchronously added to the default group by the Contacts Provider.

Membership to the default group should never be deleted!

**Starred in Android (Favorites)**

Setting the `ContactOptionsColumns.STARRED` of a contact in the Contacts table to true results in 
the addition of a group membership to the favorites group of the associated account. Setting it to
false removes that membership. 

The inverse works too. Adding a group membership to the favorites group results in 
`ContactOptionsColumns.STARRED` being set to true. Removing the membership sets it to false.

When there are no accounts, there are also no groups and group memberships that can exist. Even
though the favorites group does not exist, contacts may still be starred because 
`ContactOptionsColumns.STARRED` is a column in the Contacts table and is not dependent on the
existence of the favorites row in the Groups table and a membership to the favorites group in the
Data table. When an account is added, all of the starred contacts also gain a membership to the
favorites group.

#### Groups; Deletion

Deleting a group simply sets the "deleted" column of the row to true. It's up to a sync adapter to
actually perform the group deletion. The RawContacts table shares the same mechanism as the Groups
table. Both have columns defined in `SyncColumns`. However, unlike the RawContacts syncing where
deletion is immediate (even without internet connection), group deletion is unpredictable. Groups
that are marked for deletion remain in the DB and is still shown in the native Contacts app. 
Sometimes they do get deleted at some point but the trigger for the actual deletion eludes me.

The native Contacts app does not support group deletion or updates perhaps because groups syncing 
isn't implemented or at least not to the same extent as contacts syncing. Therefore, this library
will also not support group deletion.

#### Sync Adapters

This library does not add any custom sync adapters to keep it short and sweet. This relies on the
default system sync settings and functions.

## Java Support

This library is intended to be Java-friendly. The policy is that we should attempt to write 
Java-friendly code that does not increase lines of code by much or add external dependencies to 
cater exclusively to Java consumers.

## Creating Entities

Entities (e.g. `MutableContact`) are done my constructing an instance using the default no-parameter
constructor. Then, the individual attributes / properties are set afterwards (Kotlin's `apply` 
comes in handy here). The constructor is made internal so that consumers do not have the option
to set read-only and private or internal variables such as `id` and `rawId`. 

> FIXME? Make constructors public if Kotlin ever supports private setter for an attribute in the 
> constructor. 
> See https://discuss.kotlinlang.org/t/private-setter-for-var-in-primary-constructor/3640.

HOWEVER! Consumers are still able to set read-only and private or internal variables though because 
all `Entity` classes are data classes. Data classes provide a `copy` function that allows for 
setting any property no matter their visibility and even if the constructor is private. As a matter
of fact, setting the constructor of a `data class` as `private` gives this warning by Android
Studio: "Private data class constructor is exposed via the 'copy' method.

There is currently no way to disable the `copy` function of data classes (that I know of). The only
thing we can do is to provide documentation to consumers, insisting against the use of the `copy`
method as it may lead to unwanted modifications when updating and deleting contacts.

> We could just use regular classes instead of data classes but entities should be data classes
> because it is what they are (know what I mean?!).
> FIXME? Hide / disable data class `copy` function if kotlin ever allows it. 

## Mutable Entities? 

Why not use a more conventional builder pattern? Why "mutable"? Because it's the simplest way to 
implement a form of the builder pattern without having the write builder code or adding a dependency
on Google's `AutoValue`. Furthermore, having "mutable" entities as data classes with all properties
defined in the constructor allows it to be `Parcelize`d. Besides, one of the main benefits of the
conventional builder pattern really only benefits Java consumers. That is function chaining. Kotlin 
consumers may just use `apply` (and other similar ones).

## Why Not Add Android X / Support Library Dependency?

I want to keep the dependency list of this library to a minimum. The Contacts Provider is native to
Android since the beginning. I want to honor that fact by avoiding adding dependencies here. I made
a bit of an exception by adding the [Dexter library][2] for permissions handling for the permissions
modules (not in the core modules). I'm tempted to remove the Dexter dependency and implement 
permissions handling myself because Dexter brings in a lot of other dependencies with it. However,
it is not part of the core `contacts` module so I'm able to live with this.

[1]: https://developer.android.com/guide/topics/providers/contacts-provider
[2]: https://github.com/Karumi/Dexter
