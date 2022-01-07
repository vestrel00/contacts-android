# How do I learn more about the API entities?

First, it's important to understand the most basic concept of the
[Android Contacts Provider / ContactsContract](https://developer.android.com/guide/topics/providers/contacts-provider).
Afterwards, everything in this library should just make sense.

There is only one thing you need to know outside of this library. The library handles the rest of
the details so you don't have to!

## Contacts Provider / ContactsContract Basic Concept

There are 3 main database tables used in dealing with contacts. These tables are all connected.

1. Contacts
    - Rows representing different people.
    - E.G. John Doe
2. RawContacts
    - Rows that link Contacts rows to specific Accounts.
    - E.G. John Doe from john.doe@gmail.com, John Doe from john.dow@hotmail.com
3. Data
    - Rows containing data (e.g. name, email) for a RawContacts row.
    - E.G. John Doe from Gmail's name and email, John Doe from Hotmail's phone and address

> There are more tables but it won't be covered in this howto for brevity. 

In the example given (E.G.) above,

- there is one row in the Contacts table for the person John Doe
- there are 2 rows in the RawContacts table that make up the Contact John Doe
- there are 4 rows in the Data table belonging to the Contact John Doe.
    - 2 of these rows belong to John Doe from Gmail and the other 2 belong to John Doe from Hotmail

In the background, the Contacts Provider automatically performs the RawContacts linking/aggregation
into a single Contact. To forcefully link or unlink sets of RawContacts, read
[How do I link/unlink Contacts?](/howto/howto-link-unlink-contacts.md)

In the background, the Contacts Provider syncs all data from the local database to the remote database
and vice versa (depending on system contact sync settings). Read more in
[How does contact data get synced across devices?](/howto/howto-sync-contact-data.md)

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
- `DataEntity`
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
(typically used by insert, update, and delete functions). Most immutable entities have a 
`mutableCopy` function that returns a mutable copy (typically to be used for inserts and updates 
and other mutating API functions).

Custom data types may also be integrated into the contacts database (though not synced across devices).
Read more in [How do I integrate custom data?](/howto/howto-integrate-custom-data.md)

## Data kinds count restrictions

A `RawContact` may have at most one OR no limits of certain kinds of data.

A RawContact may have 0 or 1 of each of these data kinds;

- `Name`
- `Nickname`
- `Note`
- `Organization`
- `Photo`
- `SipAddress`

A RawContact may have 0, 1, or more of each of these data kinds;

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

## Data kinds Account restrictions

Entries of some data kinds should not be allowed to exist for local RawContacts (those that are not
associated with an Account). 

For more info, read [How do I learn more about "local" (device-only) contacts?](/howto/howto-learn-more-about-local-contacts.md)

## Automatic data kinds creation

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

> Note that the query APIs in this library do not return blanks in results. In this case, the `Name`, 
> `Nickname`, and `Note` will not be included in the RawContact because their primary values are all
> null. Blanks are also ignored on insert and deleted on update. 
> For more info, read [How do I learn more about "blank" data?](/howto/howto-learn-more-about-blank-data.md)

If a valid account is not provided, no entries of the above are automatically created.

To determine if a RawContact is associated with an Account or not, read
[How do I query for Accounts?](/howto/howto-query-accounts.md)

## Data integrity

There is a section in the official Contacts Provider documentation about "Data Integrity";
https://developer.android.com/guide/topics/providers/contacts-provider#DataIntegrity

It enumerates four general rules to follow to retain the "integrity of data" :D Paraphrasing in terms 
of this library, the rules are as follows; 

1. Always add a `Name` for every `RawContact`.
2. Always link new Data to their parent `RawContact`.
3. Change data only for those raw contacts that you own.
4. Always use the constants defined in `ContactsContract` and its subclasses for authorities, 
   content URIs, URI paths, column names, MIME types, and TYPE values.
   
This library follows rules 2 and 4. 

Rule 1 is ignored because the native Contacts app also ignores that rule. Enforcing this rule means 
that a name has to be provided for every `RawContact`, which is not practical at all. Users should 
be able to create contacts with just an email or phone number, without a name. This library follows 
the native Contacts app behavior, which also disregards this rule =P

Rule 3 is intentionally ignored. There are two types of data; 

a. those that are defined in the Contacts Provider (e.g. name, email, phone number, etc)
b. those that are defined by other apps (e.g. custom data from other apps)

This library allows modification of native data kinds and custom data kinds. Native data kinds should 
obviously be modifiable as it is the entire reason why the Contacts Provider exposes these data kinds
to us in the first place. The question is, should this library provide functions for modifying 
(insert, update, delete) custom data defined by other apps/services such as other apps 
(e.g. WhatsApp, Facebook, etc)? The answer to that will be determined when the time comes to support 
custom data from other apps in the future... (Probably, yes!)

For more info, read [How do I integrate custom data from other apps?](/howto/howto-integrate-custom-data-from-other-apps.md)

## Accessing contact data

When you have an instance of `Contact`, you have complete (and correct) access to data stored in it.

To access data of a Contact with only one RawContact,

```kotlin
val contact: Contact
val rawContact: RawContact = contact.rawContacts.first()
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

        Addresses: ${rawContact.addresses}
        Emails: ${rawContact.emails}
        Events: ${rawContact.events}
        Group memberships: ${rawContact.groupMemberships}
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
    // Photo require separate blocking function calls.
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
[How do I use some miscellaneous extension functions to make my life easier?](/howto/howto-use-miscellaneous-extensions.md)

To look into the actual Contacts Provider tables, read [How do I debug the Contacts Provider tables?](/howto/howto-debug-contacts-provider-tables.md)

To learn more about the Contact lookup key, read [How do I learn more about the Contact lookup key vs ID?](/howto/howto-learn-more-contact-lookup-key.md)

## Redacting entities

All `Entity` in this library are `Redactable, which indicates that there could be sensitive private 
user data that could be redacted, for legal purposes. If you are logging contact data in production 
to remote data centers for analytics or crash reporting, then it is important to redact certain 
parts of every contact's data.

For more info, read [How do I redact entities and API input and output in production?](/howto/howto-redact-apis-and-entities.md)

## Syncing contact data

Syncing contact data, including groups, are done automatically by the Contacts Provider depending on
the account sync settings.

For more info, read [How do I sync contact data across devices?](/howto/howto-sync-contact-data.md)