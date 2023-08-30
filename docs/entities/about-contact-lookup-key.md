# Contact lookup key vs ID

The _Contact ID_ is a number in the Contacts table that serves as the _unique identifier for a row
in the **local** Contacts table_. These look like any number used as an ID in a database table. For
example; `4`, `8`, `15`, `16`, `23`, `42`, ...

The _Contact lookup key_ is a string that serves as the _unique identifier for an aggregate contact 
in the **local and remote** databases_. These look like randomly generated or hashed strings. 
For example; `2059i4a27289d88a0a4e7`, `0r62-2A2C2E`, ...

The official documentation for the Contact lookup key is,

> ℹ️ An opaque value that contains hints on how to find the contact if its row id changed as a result
> of a sync or aggregation.

Let's dissect the documentation,

- "if its row id changed".
    - This means that a Person's row ID can change!
- "as a result of a sync".
    - The Contacts Provider allows sync adapters to modify the local and remote Contacts databases
      to ensure that Contact data is synced per user account.
- "as a result of...aggregation".
    - Two or more Contacts (along with their constituent RawContacts) can be linked into a single
      Contact. When this happens, those Contacts will be consolidated into a single (existing)
      Contact row. Unlinking will result in the original Contacts prior to linking to have different
      IDs in the Contacts table because the previously deleted row IDs cannot be reused.

Unlike the Contact ID, the lookup key's components (it can be more than one if there are more than
one constituent RawContact) is the same across devices (for contacts that are associated
with an Account and are synced). The lookup key points to a person entity rather than just a row 
in a table. It is the unique identifier used by local and remote sync adapters to identify an 
aggregate contact. 

> ℹ️ Actually, it seems like the Contact lookup key is a reference to a RawContact (or all of its
> constituent RawContacts). RawContacts have a reference to the parent Contact via the Contact ID. 
> Similarly, the parent Contact has a reference to all of its constituent RawContacts via the 
> lookup key.

Note that RawContacts do not have a lookup key. It is exclusive to Contacts. However, RawContacts 
associated with an Account that have a SyncAdapter typically have a non-null value in the 
`ContactsContract.SyncColumns.SOURCE_ID` column, which is typically used as a component in the 
parent Contact's lookup key.

> ⚠️Setting the RawContact's `SOURCE_ID` to a different value will change the lookup key of the
> parent Contact, which may break existing shortcuts!

## When to use Contact lookup key vs Contact ID?

Use the **Contact lookup key** when you need to save a reference to a Contact that you want to 
fetch after some period of time.

- Saving/restoring activity/fragment instance state.
- Saving to an external database, preferences, or files.
- Creating shortcuts.

Use the **Contact ID** for everything else.

- Performing read/write operations in the same function call or session in your app.
- Performing read/write operations that require ID (e.g. Contact photo and options).

## How to get the Contact lookup key?

Lookup keys are included in queries by default but are not required. This means that if you use do
not invoke the `include` function in query APIs, then it will be included in the returned Contacts.
However, if you do specify fields to include by invoking the `include` function, then you must 
explicitly specify the lookup key,

```kotlin
.include(Fields.Contact.LookupKey)
```

`Contact`s instances returned by the query will contain a value in the `Contact.lookupKey` property.

For more info, read [Include only certain fields for read and write operations](./../basics/include-only-desired-data.md).

## How to get Contacts using lookup keys?

Use the `decomposedLookupKeys` functions in `contacts.core.util.ContactLookupKey.kt` to get contacts 
by lookup key,

```kotlin
val contacts = query.where { decomposedLookupKeys(lookupKeys) whereOr { Contact.LookupKey contains it } }.find()
```

Or use the `lookupKeyIn` extensions in `contacts.core.util.ContactLookupKey.kt` to get contacts
by lookup key,

```kotlin
val contacts = query.where { Contact.lookupKeyIn(lookupKeys) }.find()
```

For an explanation on why you should use those functions instead of the lookup key directly,
read the function documentation.

Note that if the lookup key is a reference to a linked Contact (a Contact with two or more
constituent RawContacts), and the linked Contact is unlinked, then the query will return
multiple Contacts.

> ℹ️ For more info, read [Query contacts (advanced)](./../basics/query-contacts-advanced.md).

## Moving RawContacts between accounts and the lookup key

Moving a RawContact to a different Account will cause the Contact lookup key to change. 
In these cases, the changes to the lookup key will only be applied after the Contacts Provider and 
sync adapters sync the changes. This means that the local changes are not immediately applied.

> ℹ️ For more info, read [Sync contact data across devices](./../entities/sync-contact-data.md).

Moving a RawContact from one Account to another will result in a failed query using lookup keys.

For example, using the default AOSP Contacts app or the Google Contacts app...

1. View a contact's details.
2. Create a shortcut to it in the home screen (launcher).
   - This shortcut uses the Contact lookup key (not the ID) to form a lookup URI.
3. Move the RawContact to a different Account.
4. Tap the shortcut in the home screen (launcher).

Both Contacts apps will say that the Contact no longer exist or has been removed. This is not a bug.
It is expected behavior due to the way the Contacts Provider works.

> ℹ️ For more info, read [Move RawContacts across Accounts](./../accounts/move-raw-contacts-across-accounts.md).

## Linking/unlinking contacts and the lookup key

Linking and unlinking RawContacts will change the value of the lookup key. However, as discussed
in prior sections, you are still able to use the lookup key to find the aggregate Contact even
though the Contact ID has changed.

Linking/unlinking contacts will result in a successful lookup using lookup keys prior to the
Account change.

For example, using the default AOSP Contacts app or the Google Contacts app...

1. View a contact's details.
2. Create a shortcut to it in the home screen (launcher).
    - This shortcut uses the Contact lookup key (not the ID) to form a lookup URI.
3. Link the contact to another contact.
4. Tap the shortcut in the home screen (launcher).
5. Unlink the contact.
6. Tap the shortcut in the home screen (launcher).

In both cases, the shortcut successfully opens the correct aggregate Contact.

> ℹ️ For more info on linking/unlinking, read [Link unlink Contacts](./../other/link-unlink-contacts.md).

------------------------

## Developer notes (or for advanced users)

> ℹ️ The following section are note from developers of this library for other developers. It is copied
> from the [DEV_NOTES](./../dev-notes.md). You may still read the following as a consumer of the library
> in case you need deeper insight.

The `Contacts._ID` is the unique identifier for the row in the Contacts table. The
`Contacts.LOOKUP_KEY` is the unique identifier for an aggregate Contact (a person). The `_ID` may
change due to aggregation and sync. The same goes for the `LOOKUP_KEY` but unlike the `ID` it may 
still be used to find the aggregate contact.

Unlike the Contact ID, the lookup key is the same across devices (for contacts that are associated 
with an Account and are synced).

> ℹ️ The following investigation was done with a much larger data set. I has been simplified here 
> for brevity.

Let's take a look at the following Contacts and RawContacts table rows,

```
#### Contacts table
Contact id: 55, lookupKey: 0r55-2E4644502A2E50563A503840462E2A404C2A562E4644502A2E50, displayNamePrimary: Contact With Local RawContact
Contact id: 56, lookupKey: 2059i6f5de8460f7f227e, displayNamePrimary: Contact With Synced RawContact
#### RawContacts table
RawContact id: 55, contactId: 55, sourceId: null, displayNamePrimary: Contact With Local RawContact
RawContact id: 56, contactId: 56, sourceId: 2059i6f5de8460f7f227e, displayNamePrimary: Contact With Synced RawContact
```

There are two Contacts each having one RawContact.

Notice that the lookup keys are a bit different.

- Contact With Local RawContact: 0r55-2E4644502A2E50563A503840462E2A404C2A562E4644502A2E50
- Contact With Synced RawContact: 2059i6f5de8460f7f227e

The Contact with synced RawContact uses te RawContact's `SOURCE_ID` as part of its lookup key.

The Contact with unsynced, device-only, local RawContact has a much longer (or shorter e.g. 0r62-2A2C2E)
lookup key and starts with "0r<RawContact ID>-" and all characters after it are in uppercase. The
other thing to notice is that the "55" in "0r55-" seems to be the same as the RawContact ID (I did
a bit more experiments than what is written in these notes to confirm that it is indeed the
RawContact ID and not the Contact ID). We probably don't need to worry about these details though
the Contacts Provider probably uses these things internally. We also should not rely on it.

In any case, it is safe to assume that the **Contact lookup key is a reference to a RawContact**
(or reference to more than one constituent RawContact when multiple RawContacts are linked). Again,
an internal Contacts Provider detail we should not rely on BUT is probably relevant when implementing
sync adapters.

**When we link** the two, we get...

```
Contact id: 55, lookupKey: 0r55-2E4644502A2E50563A503840462E2A404C2A562E4644502A2E50.2059i6f5de8460f7f227e, displayNamePrimary: Contact With Synced RawContact
#### RawContacts table
RawContact id: 55, contactId: 55, sorceId: null, displayNamePrimary: Contact With Local RawContact
RawContact id: 56, contactId: 55, sourceId: 6f5de8460f7f227e, displayNamePrimary: Contact With Synced RawContact
```

Notice,

- Contact with ID 56 has been deleted.
- Contact with ID 55 still exist with the **lookup keys** of both Contact 55 and 56 **combined separated by a "."**.
    - This holds true in cases where two or more local-only or non-local-only RawContacts are linked.
- RawContacts remain unchanged except reference to Contact 56 has been replaced with 55.
- The primary display name of Contact 55 came from RawContact 55 prior to the link and now comes
  from RawContact 56 after the link.
    - This primary name resolution is probably irrelevant so pay no attention to it.

The most important part to notice is that the lookup keys get combined.

The lookup uri is required to build a `Contacts.CONTENT_LOOKUP_URI`...

```java
/**
 * A content:// style URI for this table that should be used to create
 * shortcuts or otherwise create long-term links to contacts. This URI
 * should always be followed by a "/" and the contact's {@link #LOOKUP_KEY}.
 * It can optionally also have a "/" and last known contact ID appended after
 * that. This "complete" format is an important optimization and is highly recommended.
 * <p>
 * As long as the contact's row ID remains the same, this URI is
 * equivalent to {@link #CONTENT_URI}. If the contact's row ID changes
 * as a result of a sync or aggregation, this URI will look up the
 * contact using indirect information (sync IDs or constituent raw
 * contacts).
 * <p>
 * Lookup key should be appended unencoded - it is stored in the encoded
 * form, ready for use in a URI.
 */
public static final Uri CONTENT_LOOKUP_URI = Uri.withAppendedPath(CONTENT_URI, "lookup");

/**
 * Build a {@link #CONTENT_LOOKUP_URI} lookup {@link Uri} using the
 * given {@link ContactsContract.Contacts#_ID} and {@link #LOOKUP_KEY}.
 * <p>
 * Returns null if unable to construct a valid lookup URI from the
 * provided parameters.
 */
public static Uri getLookupUri(long contactId, String lookupKey) {
    if (TextUtils.isEmpty(lookupKey)) {
        return null;
    }
    return ContentUris.withAppendedId(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey), contactId);
}
```

From the lookup uri, we can lookup the Contact row...

```java
public static Uri lookupContact(ContentResolver resolver, Uri lookupUri) { ... }
```

Or simply get the Contact ID...

```java
// code inside `public static Uri lookupContact`
resolver.query(lookupUri, new String[]{Contacts._ID}, null, null, null)
```

However, given that the lookup key of the deleted Contact 56 still lives on, it is possible to get
the linked Contact 55 using the lookup key of Contact 56 using our standard query APIs!

```kotlin
.where { Contact.LookupKey contains lookupKey }
```

The above is correct as long as these assumptions hold true;

- the lookup key is unique
- there is no lookup key that can contain a shorter lookup key
    - the Contact ID fails this test because a smaller number is contained in a larger number
    - synced contacts have shorter lookup keys than local contacts. However, local contacts'
      lookup keys are capitalized whereas synced contact are not. Also, there seems to be other
      differences in pattern between long and short lookup keys. It should be safe to make this
      assumption.

Until the community finds that this assumption is flawed, we'll assume that it is true! For now, we
can **avoid having to create another API or extensions just for using lookup keys**.

**When we unlink**, we get...

```
#### Contacts table
Contact id: 55, lookupKey: 2059i6f5de8460f7f227e, displayNamePrimary: Contact With Synced RawContact
Contact id: 58, lookupKey: 0r55-2E4644502A2E50563A503840462E2A404C2A562E4644502A2E50, displayNamePrimary: Contact With Local RawContact
#### RawContacts table
RawContact id: 55, contactId: 58, sourceId: null, displayNamePrimary: Contact With Local RawContact
RawContact id: 56, contactId: 55, sourceId: 6f5de8460f7f227e, displayNamePrimary: Contact With Synced RawContact
```

Notice,

- A new Contact row with ID of 58 is created.
- The lookup keys are separated and distributed between Contact 55 and 58.
- RawContact 55 Contact reference has been set to Contact 58.

Let's compare the Contact-RawContact relationship before and after linking and then unlinking.

|            | **Contact ID** | **Lookup Key**                                                                      | **RawContact.Contact ID** |
|------------|----------------|-------------------------------------------------------------------------------------|---------------------------|
| **Before** | 55,<br>56      | 0r55-2E4644502A2E50563A503840462E2A404C2A562E4644502A2E50,<br>2059i6f5de8460f7f227e | 55,<br>56                 |
| **After**  | 55,<br>58      | 2059i6f5de8460f7f227e,<br>0r55-2E4644502A2E50563A503840462E2A404C2A562E4644502A2E50 | 58,<br>55                 |

Notice,

- Contact ID 55 swapped lookup keys with the former Contact 56 (now 58).
- RawContact ID 55 swapped Contact reference with RawContact 56.

The Contact IDs and lookup keys got shuffled BUT the Contact-RawContact relationship remains the
same if using the lookup keys as point of reference! Here is another way to look at the table,
using the lookup key as the constant...

| **Lookup Key**                                            | **Before**                   | **After**                    |
|-----------------------------------------------------------|------------------------------|------------------------------|
| 0r55-2E4644502A2E50563A503840462E2A404C2A562E4644502A2E50 | Contact 55,<br>RawContact 55 | Contact 58,<br>RawContact 55 |
| 2059i6f5de8460f7f227e                                     | Contact 56,<br>RawContact 56 | Contact 55,<br>RawContact 56 |

Notice that the indirect relationship between the lookup key and RawContacts remains the same
before and after the link-unlink even though the Contact IDs changed.

> ℹ️ As mentioned earlier in this section, the "55" in "0r55-" seems to be referencing the 
> RawContact ID. In other words, since local RawContacts are not synced or tracked in a remote 
> database where Contacts -> RawContacts mappings exist, the Contacts Provider most likely uses this
> "0r<RawContact ID>-" pattern to make the connection. This is not really relevant for us as we are
> not relying on this mechanism. I'm just pointing out my observations, which could be incorrect.

This means that...

- If users of this library saved a reference Contact ID 55, then a link-unlink (or sync adapter
  functions) occur.
    - Getting Contact by ID 55 will result in the RawContact-Data of the former Contact 56 to be
      returned. This is a bug! Same goes if users saved a reference to Contact ID 56.
- If users of this library saved a reference to the lookup keys, then a link-unlink (or sync adapter
  functions) occur.
    - Getting Contact by lookup key will result in the correct RawContact-Data to be returned.

So when to use Contact ID vs lookup key?

- Lookup key: for a reference to a Contact that needs to be loaded after some period of time.
    - Saving/restoring activity/fragment instance state.
    - Saving to an external database, preferences, or files.
    - Creating shortcuts.
- ID: for everything else.
    - Performing read/write operations in the same function call or session in your app.
    - Performing read/write operations that require ID (e.g. Contact photo and options).

Another thing to check is what happens when associating a local RawContact to an Account (move from
device to Account) and vice versa. Is the lookup key of the Contact affected?

After associating the local RawContact to an Account...

```
#### Contacts table
Contact id: 58, lookupKey: 2059i4abd4a8f8ff89642
#### RawContacts table
RawContact id: 55, contactId: 58, sourceId: 4abd4a8f8ff89642
```

The lookup key changed (since the RawContact's source ID has been assigned the value that came from
the sync adapter) but the Contact ID remained the same! In this case, loading a reference to
the previously local Contact will fail! I verified that this is indeed the behavior of the native
(AOSP) Contacts app. Moving the RawContact from device to Google using Google Contacts app while
having Contact details activity opened in the AOSP Contacts app will result in "error Contact does
not exist" message in the AOSP Contacts app!

> ℹ️ The RawContact and its Data also remained the same in this case.

Removing the account from it results in...

```
#### Contacts table
Contact id: 59, lookupKey: 0r58-2E4644502A2E50563A503840462E2A404C2A562E4644502A2E50
#### RawContacts table
RawContact id: 58, contactId: 59, sourceId: null
```

The Contact and RawContacts row have been deleted and new rows have been created to replace them!
I also verified that the Data rows have also been deleted and new rows have been created to
replace them!

This stuff is not really relevant for lookup key but still good to know for implementing
moving RawContacts between accounts.