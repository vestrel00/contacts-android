# How do I learn more about the API entities?

First, it's important to understand the most basic concept of the
[Android Contacts Provider / ContactsContract](https://developer.android.com/guide/topics/providers/contacts-provider).
Afterwards, everything in this library should just make sense.

There is only one thing you need to know outside of this library. The library handles the rest of
the details so you don't have to =)

## Contacts Provider / ContactsContract Basic Concept

There are 3 main database tables used in dealing with contacts.  There are more tables but you
don't need to know that =) These tables are all connected.

1. Contacts
    - Rows representing different people.
    - E.G. John Doe
2. RawContacts
    - Rows that link Contacts rows to specific Accounts.
    - E.G. John Doe from john.doe@gmail.com, John Doe from john.dow@hotmail.com
3. Data
    - Rows containing data (e.g. name, email) for a RawContacts row.
    - E.G. John Doe from Gmail's name and email, John Doe from Hotmail's phone and address

In the example given (E.G.) above,

- there is one row in the Contacts table for the person John Doe
- there are 2 rows in the RawContacts table that make up the Contact John Doe
- there are 4 rows in the Data table belonging to the Contact John Doe.
    - 2 of these rows belong to John Doe from Gmail and the other 2 belong to John Doe from Hotmail

In the background, the Contacts Provider automatically performs the RawContacts linking/aggregation
into a single Contact. To forcefully link or unlink sets of RawContacts, read
[How do I link/unlink Contacts?](/contacts-android/howto/howto-link-unlink-contacts.html).

In the background, the Contacts Provider syncs all data from the local database to the remote database
and vice versa (depending on system contact sync settings). Read more in
[How does contact data get synced across devices?](/contacts-android/howto/howto-sync-contact-data.html).

That's all you need to know! Hopefully it wasn't too much. I know it was difficult for me to grasp
in the beginning =P.

Once you internalize this one to many relationship between **Contacts -> RawContacts -> Data**, you
have unlocked the full potential of this library and **the world is at the palm of your hands**!

## Contacts API Entities

This library provides entities that model everything in the Contacts Provider database.

- `Contact`
    - Primarily contains a list of RawContacts that are associated with this contact.
- `RawContact`
    - Contains contact data that belong to an account.
    - There may be more than one RawContact per Contact.
- `CommonDataEntity`
    - A specific kind of data of a RawContact. These entities model the common data kinds that are
      provided by the Contacts Provider.
    - `Address`
    - `Email`
    - `Event`
    - `GroupMembership`
    - `Im`
    - `Name`
    - `Nickname`
    - `Note`
    - `Organization`
    - `Phone`
    - `Photo`
    - `Relation`
    - `SipAddress`
    - `Website`

You can find all of the above in the `contacts.core.entities` package. Note that there are other
entities that are not mentioned in this howto for brevity.

All entities are `Parcelable` to support state retention during app/activity/fragment/view recreation.

Each entity has an immutable version (typically returned by queries) and a mutable version
(typically used by insert, update, and delete functions). Most immutable entities have a `toMutable`
function that returns a mutable copy (typically to be used for inserts and updates and other
mutating API functions).

Custom data types may also be integrated into the contacts database (though not synced across devices).
Read more in [How do I integrate custom data?](/contacts-android/howto/howto-integrate-custom-data.html).

## Common data kinds count restrictions

A `RawContact` may have at most one OR no limits of certain kinds of data.

A RawContact may have 0 or 1 one of each these data kinds;

- `Name`
- `Nickname`
- `Note`
- `Organization`
- `Photo`
- `SipAddress`

A RawContact may have 0, 1, or more of each these data kinds;

- `Address`
- `Email`
- `Event`
- `GroupMembership`
- `Im`
- `Phone`
- `Relation`
- `Website`

The Contacts Provider may or may not enforce these count restrictions. However, the native Contacts
app imposes these restrictions. Therefore, this library also imposes these restrictions and
disables consumers from violating them.

The core library does not explicitly expose count restrictions to consumers. However, it is exposed
when integrating custom data via the `CustomDataCountRestriction`.

## Common data kinds Account restrictions

Entries of some data kinds should not be allowed to exist for local RawContacts (those that are not
associated with an Account). These data kinds are;

- `GroupMembership`
    - Groups can only exist if it is associated with an Account. Therefore, memberships to groups
      is not possible when there is no associated Account.
- `Event`
    - It is not clear why this requires an associated Account. Maybe because these are typically
      birth dates that users expect to be synced with their calendar across devices?
- `Relation`
    - It is not clear why this requires an associated Account...

The Contacts Provider may or may not enforce these Account restrictions. However, the native Contacts
app imposes these restrictions. Therefore, this library also imposes these restrictions and
disables consumers from violating them.

## Automatic common data kinds creation

An entry of each of the following data kinds are automatically created for all contacts, if not
provided;

- `GroupMembership`, underlying value defaults to the account's default system group
- `Name`, underlying value defaults to null
- `Nickname`, underlying value defaults to null
- `Note`, underlying value defaults to null

This automatic creation occur automatically in the background (typically after creation) only for
RawContacts that are associated with an Account. If a valid account is provided, membership to the
(auto add) system group is automatically created immediately by the Contacts Provider at the time of
creation. The name, nickname, and note are automatically created at a later time.

If a valid account is not provided, no entries of the above are automatically created.

To determine if a RawContact is associated with an Account or not, read
[How do I query for Accounts?](/contacts-android/howto/howto-query-accounts.html).

## Accessing contact data

When you have an instance of `Contact`, you have complete (and correct) access to data stored in it.

To access data of a Contact with only one RawContact,

```kotlin
val contact: Contact
val rawContact: RawContact = contact.rawContacts.first()
Log.d(
    "Contact",
    """
        Display name: ${contact.displayNamePrimary}
        Last updated: ${contact.lastUpdatedTimestamp}
        Starred?: ${contact.options?.starred}
        Send to voicemail?: ${contact.options?.sendToVoicemail}
        Ringtone: ${contact.options?.customRingtone}

        Addresses: ${rawContact.addresses}
        Emails: ${rawContact.emails}
        Events: ${rawContact.events}
        IMs: ${rawContact.ims}
        Name: ${rawContact.name}
        Nickname: ${rawContact.nickname}
        Note: ${rawContact.note}
        Organization: ${rawContact.organization}
        Phones: ${rawContact.phones}
        Relations: ${rawContact.relations}
        SipAddress: ${rawContact.sipAddress}
        Websites: ${rawContact.websites}
    """.trimIndent()
    // Groups and photo require separate blocking function calls.
)
```

To access data of a Contact with possibly more than one RawContact, we can use `ContactData`
extensions to make our life easier,

```kotlin
val contact: Contact
Log.d(
    "Contact",
    """
        ID: ${contact.id}

        Display name: ${contact.displayNamePrimary}
        Display name alt: ${contact.displayNameAlt}

        Photo Uri: ${contact.photoUri}
        Thumbnail Uri: ${contact.photoThumbnailUri}

        Last updated: ${contact.lastUpdatedTimestamp}

        Starred?: ${contact.options?.starred}
        Send to voicemail?: ${contact.options?.sendToVoicemail}
        Ringtone: ${contact.options?.customRingtone}

        Aggregate data from all RawContacts of the contact
        -----------------------------------
        Addresses: ${contact.addressList()}
        Emails: ${contact.emailList()}
        Events: ${contact.eventList()}
        Group memberships: ${contact.groupMembershipList()}
        IMs: ${contact.imList()}
        Names: ${contact.nameList()}
        Nicknames: ${contact.nicknameList()}
        Notes: ${contact.noteList()}
        Organizations: ${contact.organizationList()}
        Phones: ${contact.phoneList()}
        Relations: ${contact.relationList()}
        SipAddresses: ${contact.sipAddressList()}
        Websites: ${contact.websiteList()}
        -----------------------------------
    """.trimIndent()
    // There are also aggregate data functions that return a sequence instead of a list.
)
```

Each Contact may have more than one of the following data if the Contact is made up of 2 or more
RawContacts; name, nickname, note, organization, sip address.

For more info on how to easily aggregate data from all RawContacts in a Contact, read
[How do I use some miscellaneous extension functions to make my life easier?](/contacts-android/howto/howto-use-miscellaneous-extensions.html)