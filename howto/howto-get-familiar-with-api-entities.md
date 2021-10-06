## How do I get familiarized with the Contacts entities?

First, it's important to understand the most basic concept of the [Android Contacts Provider / ContactsContract][1].
Afterwards, everything in this library should just make sense.

There is only one thing you need to know outside of this library. The library handles the rest of
the details so you don't have to =)

#### Contacts Provider / ContactsContract Basic Concept

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
[How do I link/unlink Contacts?](/howto/howto-link-unlink-contacts.md).

In the background, the Contacts Provider syncs all data from the local database to the remote database
and vice versa (depending on system contact sync settings). Read more in
[How does contact data get synced across devices?](/howto/howto-sync-contact-data.md).

That's all you need to know! Hopefully it wasn't too much. I know it was difficult for me to grasp
in the beginning =P.

Once you internalize this one to many relationship between **Contacts -> RawContacts -> Data**, you
have unlocked the full potential of this library and **the world is at the palm of your hands**!

#### Contacts API Entities

This library provides entities that model everything in the Contacts Provider database.

- `Contact`
    - Primarily contains a list of RawContacts that are associated with this contact.
    - To easily access aggregate data from all RawContacts in the list, read
      [How do I use some miscellaneous extension functions to make my life easier?](/howto/howto-use-miscellaneous-extensions.md)
- `RawContact`
    - Contains contact data that belong to an account.
- `CommonDataEntity`
    - A specific type of data of a RawContact. These entities model the common data kinds that are
      provided by the Contacts Provider. These should be self-explanatory from just the class names.
      For more info, look at the individual class/member variable documentation.
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

> You can find all of the above in the `contacts.core.entities` package. Note that there are other
> entities that are not mentioned in this howto fo brevity.

All entities are `Parcelable` to support state retention during app/activity/fragment/view recreation.

Each entity has an immutable version (typically returned by queries) and a mutable version
(typically used by insert, update, and delete functions). Most immutable entities have a `toMutable`
function that returns a mutable copy (typically to be used for inserts and updates and other
mutating API functions).

Custom data types may also be integrated into the contacts database (though not synced across devices).
Read more in [How do I integrate custom data?](/howto/howto-integrate-custom-data.md).

[1]: https://developer.android.com/guide/topics/providers/contacts-provider