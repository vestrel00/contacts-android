# SIM Contacts

This library gives you APIs that allow you to read and write Contacts stored in the SIM card.

- [`SimContactsQuery`](./../sim/query-sim-contacts.md)
- [`SimContactsInsert`](./../sim/insert-sim-contacts.md)
- [`SimContactsUpdate`](./../sim/update-sim-contacts.md)
- [`SimContactsDelete`](./../sim/delete-sim-contacts.md)

## SIM card state

In order for any SIM card read or write operations to succeed, the default/active SIM card must 
be in a ready state. If no SIM card is in a ready state, then read/write operations will fail
immediately. 

To check if the default/active SIM card is in a ready state,

```kotlin
val isSimCardReady = Contacts(context).sim().cardInfo.isReady
```

## SIM Contact data

SIM Contact data consists of the `name` and `number`.

> ℹ️ Support for `email` was recently added in Android 12. I don't think it is stable yet. 
> Regardless, it is too new so this library will wait a bit before adding support for it.

### Character limits

The `name` and `number` are subject to the SIM card's maximum character limit, which is typically 
around 20-30 characters. This may vary per SIM card. Inserts or updates will fail if the limit is 
breached.

The `SimContactsInsert` and `SimContactsUpdate` APIs provided in this library automatically 
detect max character limits and returns appropriate errors when limits are breached. However,
you may also access these limits yourself if you want;

```kotlin
val nameMaxLength = Contacts(context).sim().cardInfo.maxCharacterLimits().nameMaxLength()
val numberMaxLength = Contacts(context).sim().cardInfo.maxCharacterLimits().numberMaxLength()
```

Character limits are cached internally in shared preferences so that calculations need not occur
everytime these functions are invoked. If you want to clear the cache to ensure recalculation;

```kotlin
Contacts(context).sim().cardInfo.maxCharacterLimits().clearCachedNameAndNumberMaxLengths()
```

### SIM Contact row ID

The SIM contact that an ID is pointing to may change if the contact is deleted in the database and
another contact is inserted. The inserted contact may be assigned the ID of the deleted
contact.

DO NOT RELY ON THIS TO MATCH VALUES IN THE DATABASE! The SIM table does not support selection
by ID so you can't use this for anything anyways. 

### Duplicate entries are allowed

Duplicate entries, multiple entries having the same name and/or number, are allowed. This follows
the behavior of other smart phone and non-smart phone applications.

### Blanks are not allowed

Blank contacts (name AND number are both null or blank) will NOT be inserted. The name OR number
can be null or blank but not both.

## Some OEMs automatically sync SIM card data with Contacts Provider data

Samsung phones import contacts from SIM into the Contacts Provider. When using the builtin Samsung 
Contacts app, modifications made to the SIM contacts from the Contacts Provider are propagated to 
the SIM card and vice versa.

Samsung is most likely syncing the SIM contacts with the copy in the Contacts Provider via
SyncAdapters. The RawContacts created in the Contacts Provider have a non-remote account name and
type (pointing to the SIM card),

```
accountName: primary.sim.account_name, accountType: vnd.sec.contact.sim 
```

Furthermore, SIM contacts imported into the Contacts Provider have the same restrictions as the SIM 
card in that only columns available in the SIM are editable (_id, name, number, emails). Editing 
SIM contacts using 3rd party apps such as the Google Contacts app are not supported.

If you find any issues when using the `SimContacts` APIs, please 
[raise an issue](https://github.com/vestrel00/contacts-android/issues/new) if you find any bugs
or [start a discussion](https://github.com/vestrel00/contacts-android/discussions/new) and share
your thoughts or knowledge =)

## Multi SIM card support

[Android 5.1 adds support for using more than one cellular carrier SIM card at a time](https://developer.android.com/about/versions/lollipop/android-5.1#multisim). 
This feature lets users activate and use additional SIMs on devices that have two or more SIM card slots.

The APIs in this library have not been tested against dual SIM card configurations. It should still
work, at the very least the current default/active SIM card should be accessible.

Please [raise an issue](https://github.com/vestrel00/contacts-android/issues/new) if you find any bugs
or [start a discussion](https://github.com/vestrel00/contacts-android/discussions/new) and share 
your thoughts or knowledge =)

## Limitations

Projections, selections, and order is not supported by the `IccProvider`. Therefore, we are unable
to provide `include`, `where`, `orderBy`, `limit`, and `offset` functions in our `SimContactsQuery`
API.

Due to all of these limitations, all queries will return all contacts in the SIM card. Consumers of
this library can perform their own sorting and pagination if they wish.

Depending on memory size,
[SIM cards can hold 200 to 500+ contacts](https://www.quora.com/How-many-contacts-can-I-save-on-my-SIM-card).
The most common being around 250. Most, if not all, SIM cards have less than 1mb memory (averaging
32KB to 64KB). Therefore, memory and speed should not be affected much by not being able to
sort/order and paginate at the query level. 

## Debugging

To look at all of the rows in the SIM Contacts table, use the `Context.logSimContactsTable`
function in the `debug` module.

For more info, read [Debug the Sim Contacts table](../debug/debug-sim-contacts-tables.md).

## Known issues

Samsung phones (and perhaps other OEMs) support emails (in addition to name and number) data ahead 
of the Android 12 release. Updating and deleting SIM contacts that have email data using the APIs
provided in this library may fail. This issue does not occur when moving the SIM card to a different
phone that does not support emails. 

------------------------

## Developer notes (or for advanced users)

In building the `SimContacts` APIs provided in this library, I used the following hardware to 
observe the behavior of reading/writing to the SIM card.

| **Smart phones**                | **Non-smart phones** | **SIM cards** |
|---------------------------------|----------------------|---------------|
| Nexus 6P (Android 8)            | BLU Z5 (unknown OS)  | Mint Mobile   |
| Samsung Galaxy A71 (Android 11) |                      |               |

For software, I used the following apps.

| **Apps**                                                                                       | **Smart phones**             |
|------------------------------------------------------------------------------------------------|------------------------------|
| [SIM Card Info v1.1.6](https://play.google.com/store/apps/details?id=com.midi.siminfo)         | Nexus 6P                     |
| [Samsung Contacts v12.7.10.12](https://samsung-contacts.en.uptodown.com/android)               | Samsung Galaxy A71           |

> ℹ️ The AOSP Contacts app and [Google Contacts app](https://play.google.com/store/apps/details?id=com.google.android.contacts)
> can only import contacts from SIM card so they are not very helpful for us with this investigation.

For Android code references, I used the internal `IccProvider.java` as reference to what the Android
OS might be doing when 3rd party applications perform CRUD operations on SIM contacts.

- [IccProvider @ Android 8](https://android.googlesource.com/platform/frameworks/opt/telephony/+/8f696ee3/src/java/com/android/internal/telephony/IccProvider.java)
- [IccProvider @ Android 11](https://android.googlesource.com/platform/frameworks/opt/telephony/+/ef289bf/src/java/com/android/internal/telephony/IccProvider.java)
- [IccProvider @ Android 12](https://android.googlesource.com/platform/frameworks/opt/telephony/+/51302ef/src/java/com/android/internal/telephony/IccProvider.java)

I'm using the `content://icc/adn` URI to read/write from/to SIM card.

> ℹ️ **All of the investigation that I have done here may not apply for all SIM cards and phone OEMs!** 
> There is just way too many different SIM cards and phones out there for a single person (me) to 
> test. However, I think that my findings should apply to most cases.

### Figuring out how to perform CRUD operations

First, I added 20 contacts (name and number) to the SIM contacts using the _BLU Z5_. The first
contact is named "a" with number "1", the second is named "ab" with number "12", and so on. The last
contact is named "abcdefghijklmnopqrst" with number "12345678901234567890". I did this because the
_BLU Z5_ has determined that the maximum character limit for the name and number for my 
_Mint Mobile_ SIM card is 20.

> ℹ️ The character limits are most likely set by the SIM card and/or calculated by the OS managing 
> it based on how much total memory is available.

I also added a contact named "bro" with no number and a nameless contact with with number
"5555555555". For a total of 22 contacts in the SIM card.

I loaded the SIM card to my _Nexus 6P_. Then, I logged all of the rows in `content://icc/adn` using
the `Context.logSimContactsTable` debug function I wrote up in the `debug` module.

```
SIM Contact id: 0, name: A, number: 1, emails: null
SIM Contact id: 1, name: Ab, number: 12, emails: null
SIM Contact id: 2, name: Abc, number: 123, emails: null
SIM Contact id: 3, name: Abcd, number: 1234, emails: null
SIM Contact id: 4, name: Abcde, number: 12345, emails: null
SIM Contact id: 5, name: Abcdef, number: 123456, emails: null
SIM Contact id: 6, name: Abcdefg, number: 1234567, emails: null
SIM Contact id: 7, name: Abcdefgh, number: 12345678, emails: null
SIM Contact id: 8, name: Abcdefghi, number: 123456789, emails: null
SIM Contact id: 9, name: Abcdefghij, number: 1234567890, emails: null
SIM Contact id: 10, name: Abcdefghijk, number: 12345678901, emails: null
SIM Contact id: 11, name: Abcdefghijkl, number: 123456789012, emails: null
SIM Contact id: 12, name: Abcdefghijklm, number: 1234567890123, emails: null
SIM Contact id: 13, name: Abcdefghijklmn, number: 12345678901234, emails: null
SIM Contact id: 14, name: Abcdefghijklmno, number: 123456789012345, emails: null
SIM Contact id: 15, name: Abcdefghijklmnop, number: 1234567890123456, emails: null
SIM Contact id: 16, name: Abcdefghijklmnopq, number: 12345678901234567, emails: null
SIM Contact id: 17, name: Abcdefghijklmnopqr, number: 123456789012345678, emails: null
SIM Contact id: 18, name: Abcdefghijklmnopqrs, number: 1234567890123456789, emails: null
SIM Contact id: 19, name: Abcdefghijklmnopqrst, number: 12345678901234567890, emails: null
SIM Contact id: 20, name: Bro, number: , emails: null
SIM Contact id: 21, name: , number: 5555555555, emails: null
```

Our `SimContactsQuery` also retrieves the same exact results!

I am able to see all of the contacts in the _SIM Info_ app **except** for the nameless contact with
number "5555555555". I attempted to add a nameless contact using the _SIM Info_ app but it does not
allow reading/writing nameless contacts. 

> ℹ️ This is probably a bug in the _SIM Info_ app or a limitation that is intentionally imposed for 
> some reason. I wish I could see the source code of the app!

Deleting the first contact with ID of 0 using the _SIM Info_ app works just fine. Deleting the contact
with ID of 2 using our `SimContactsDelete` works just fine too. At this point the first 5 rows in the 
table are;

```
SIM Contact id: 1, name: Ab, number: 12, emails: null
SIM Contact id: 3, name: Abcd, number: 1234, emails: null
SIM Contact id: 4, name: Abcde, number: 12345, emails: null
SIM Contact id: 5, name: Abcdef, number: 123456, emails: null
SIM Contact id: 6, name: Abcdefg, number: 1234567, emails: null
```

Inserting a contact using the _SIM Info_ app and our `SimContactsInsert` (in that order) works just 
fine, resulting in two new rows being added. One very interesting to note is that the IDs of the 
previously deleted rows (0 and 2) have been assigned to the newly inserted contacts!

```
SIM Contact id: 0, name: SIM Info Contact, number: 8, emails: null
SIM Contact id: 1, name: Ab, number: 12, emails: null
SIM Contact id: 2, name: SimContactsInsert, number: 9, emails: null
SIM Contact id: 3, name: Abcd, number: 1234, emails: null
SIM Contact id: 4, name: Abcde, number: 12345, emails: null
SIM Contact id: 5, name: Abcdef, number: 123456, emails: null
SIM Contact id: 6, name: Abcdefg, number: 1234567, emails: null
```

This means that the IDs should not be used as a reference to a particular contact because it could 
"change" in the process of deleting and inserting.

As for updates, let's start with this table...

```
SIM Contact id: 3, name: Abcd, number: 1234, emails: null
SIM Contact id: 4, name: Abcde, number: 12345, emails: null
```

Notice that Contact ID 0, 1, and 2 are available. Using the _SIM Info_ app to "update" the contact
with ID 4, we get...

```
SIM Contact id: 3, name: Abcd, number: 1234, emails: null
SIM Contact id: 4, name: xxx, number: 12345, emails: null
```

The ID remains 4. We get the same result using our `SimContactsUpdate` API =)

Thus, we have implemented CRUD APIs!!!

### Figuring out character limits

The _BLU Z5_ non-smartphone has determined that the maximum character limit for the name and number 
for my _Mint Mobile_ SIM card is 20. 

I inserted a contact with a name with 26 characters and another contact with a number with 21 
characters using the _SIM Info_ app. The first insert (26 char name) succeeded but the second failed
(21 char number).

```
SIM Contact id: 0, name: abcdefghijklmnopqrstuvwxyz, number: 1, emails: null
```

I did the same using our `SimContactsInsert`... The same thing occurred. This means that the 
character limit is imposed on the number but perhaps not the name OR maybe the name has not reached 
the maximum. I tried inserting a name with over 100 characters and it failed. So there is a 
character limit for the name. I tried inserting names of shorter and shorter lengths until I find
the max. It seems to be 30 characters.

The character limits for the name is different for my _Mint Mobile_ SIM card is different in the
_BLU Z5_ vs _Nexus 6P_.

|            | _BLU Z5_ | _Nexus 6P_ |
|------------|----------|------------|
| **name**   | 20       | 30         |
| **number** | 20       | 20         |

I took out the SIM card from the _Nexus 6P_ and plugging it back into the _BLU Z5_ to see if it will
show the contacts that go over the 20 character limit. Both contacts with names longer than 20 
characters are shown in the _BLU Z5_ BUT the name is truncated to 20. This could mean one of two 
things;

- The phones determine the character limits based on SIM card memory.
- The SIM card specifies the character limits but the _BLU Z5_ hard codes it to 20 regardless.

Time to check with the _Samsung Galaxy A71_! The Samsung yielded the same results as the Nexus. So,
perhaps it is just the self-imposed limitation of the BLU phone.

One interesting difference between the Samsung and the Nexus is that our `SimContactsInsert` was
indicating that the insert succeeded in the Samsung even though no new row was created in the SIM
table (oh Samsung lol). The result Uri returned by the insert operation is null in the Nexus but
not null in the Samsung.

**What this all means?** 

- Our `SimContactsInsert` and `SimContactsUpdate` APIs need to be able to detect the maximum 
  character limits for the `name` and `number` before performing the actual insert or update 
  operation. 
  - To figure out the max character limits, we can attempt to insert a string of length 30 (most
    names should fit there and most SIM cards have lower limits). Keep attempting to insert until
    insert succeeds (query if the row is actually created instead of just relying on the insert 
    result), making the string shorter each time. Delete the successful insert and record the 
    length of the string. 
    - Do this for both `name` and `number` and store the results in shared preferences mapped to a 
      unique ID of the SIM card. We do not want to do this calculation everytime our APIs are used!
  - Max character limits should be exposed our users via a public API.
- Furthermore, we cannot rely on the result of the insert operation alone. If the result Uri is not
  null, we must perform a query to sanity check that the actual name and number was inserted!

### Emails

There is an "emails" column in the SIM table. CRUD operations for it was not officially supported 
until recently in Android 12.

- [IccProvider @ Android 11](https://android.googlesource.com/platform/frameworks/opt/telephony/+/ef289bf/src/java/com/android/internal/telephony/IccProvider.java)
- [IccProvider @ Android 12](https://android.googlesource.com/platform/frameworks/opt/telephony/+/51302ef/src/java/com/android/internal/telephony/IccProvider.java)

Look for "TODO" comments in the `IccProvider``. You will see TODOs for emails in Android 11 but 
not Android 12.

**On my Samsung Galaxy A71 running Android 11...**

The column name is actually "emails" with an "s" (plural). What I observed,

- no email = ","
- at least one email = "email,"

There seems to be a trailing "," regardless. It seems like the emails are in CSV format 
(comma separated values).

I was not able to delete rows with emails in them. I even tried updating the where clause used in
our `SimContactsDelete` to include the email but it does not work.

The builtin Samsung Contacts app is able to insert, update, and delete rows with emails. This 
probably means that we don't have access to the internal APIs that the Samsung Contacts app has.
Keep in mind that my Samsung is running Android 11 and support for email was not added until 
Android 12. 

> ℹ️ Classic Samsung to add features farther ahead of time than vanilla Android =)

**On my Nexus 6P running Android 8...**

The contacts with emails are shown without email data (emails are null in the SIM table). These
rows are able to be updated and deleted.

**On my BLU Z5...**

SIM contacts with emails are shown without the email data. These rows are able to be updated and 
deleted.

### Duplicate entries

Duplicate entries, multiple entries having the same name and/or number, seem to be allowed by
smart phone and non-smart phone applications.

### Other considerations

It seems like there are new APIs around SIM Contacts that were introduced in API 31;

- https://developer.android.com/reference/android/provider/ContactsContract.SimContacts
- https://developer.android.com/reference/android/provider/SimPhonebookContract

Those APIs are too new to be used by this library, which supports API levels down to 19. So, we'll 
stick with using the `content://icc/adn` uri to read/write to SIM card until it becomes deprecated,
if ever.