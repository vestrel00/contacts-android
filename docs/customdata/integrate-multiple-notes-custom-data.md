# Integrate the multiple notes custom data

This library provides extensions for `MultipleNotes` custom data that overrides the built-in 
[note][built-in-note] data kind in order to enable you to read and write multiple notes for all of 
your contacts. These (optional) extensions live in the `customdata-multiplenotes` module.

> ⚠️ If a built-in data kind is overridden, then instances of it retrieved from queries will always
> be null and will be ignored in insert and update operations. Reading/writing the underlying data
> will have to be done via the custom data API.

> ℹ️ If you are looking to create your own custom data or get more insight on how the `MultipleNotes`
> custom data was built, read [Integrate custom data](./../customdata/integrate-custom-data.md).

## Built-in note data kind vs multiple notes custom data

The behavioral contract that has been upheld by many different OEMs for the built-in [note][built-in-note]
data kind is that a RawContact may only have at most one note. I verified using my own devices that
the following OEMs adheres to this implicit contract;

- Google Pixel
- Samsung (OneUI)
- Xiaomi / Redmi (HyperOS)
- RedMagic (RedMagic OS)
- Lenovo (ZUI)
- Apple (iOS)

However, [some OEMs such as Oppo and its ColorOS allows a RawContact to have multiple Notes](https://github.com/vestrel00/contacts-android/discussions/342).

If you are developing specifically for an OS such as ColorOS, then this module might be useful to you.
Otherwise, it is **NOT RECOMMENDED** to use this module.

## ⚠️ Built-in note data kind read/write functions will be disabled

By integrating this custom data into your app, instances of the built-in `NoteEntity` retrieved from
queries will always be null and will be ignored in insert and update operations. Reading/writing 
note data will have to be done via the custom data API.

For example, the following will be null even if the RawContact actually has a non-null note...

```kotlin
val note = rawContact.note
```

To read the RawContact's note(s), you must use the custom data extension functions...

```kotlin
val notesList = rawContact.multipleNotesList(contactsApi)
```

The same applies to write operations.

## Register the multiple notes custom data with the Contacts API instance

You may register the `MultipleNotes` custom data when creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(
    context,
    customDataRegistry = CustomDataRegistry().register(
        MultipleNotesRegistration()
    )
)
```

Or, alternatively after creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(context)
MultipleNotesRegistration().registerTo(contactsApi.customDataRegistry)
```

## Get/add/remove multiple notes custom data

Just like regular data kinds, multiple notes custom data belong to a RawContact. A RawContact may 
have 0, 1, or more multiple notes.

To get the multiple notes of a RawContact,

```kotlin
val multipleNotesSequence = rawContact.multipleNotess(contactsApi)
val multipleNotesList = rawContact.multipleNotesList(contactsApi)
```

To get the multiple notes of all RawContacts belonging to a Contact,

```kotlin
val multipleNotesSequence = contact.multipleNotess(contactsApi)
val multipleNotesList = contact.multipleNotesList(contactsApi)
```

To add a multiple notes to a (mutable) RawContact,

```kotlin
mutableRawContact.addMultipleNotes(contacts, mutableMultipleNotes)
// or
mutableRawContact.addMultipleNotes(contacts) {
    note = "CoolDude91"
}
```

To add a multiple notes to the first RawContact or a Contact,

```kotlin
mutableContact.addMultipleNotes(contacts, mutableMultipleNotes)
// or
mutableContact.addMultipleNotes(contacts) {
    note = "CoolGal89"
}
```

## Use the multiple notes custom data in queries, inserts, updates, and deletes

Once you have registered your multiple notes custom data with the `Contacts` API instance, the API
instance is now able to perform read and write operations on it.

- [Query custom data](./../customdata/query-custom-data.md)
- [Insert custom data into new or existing contacts](./../customdata/insert-custom-data.md)
- [Update custom data](./../customdata/update-custom-data.md)
- [Delete custom data](./../customdata/delete-custom-data.md)

## Syncing multiple notes custom data

Since the underlying data kind used is the built-in note data kind, one (or more) notes for a 
RawContact...

- will be synced
  - if there are two or more notes, one is guaranteed to be synced while the others may or may not be synced
- are shown in the [Google Contacts][google-contacts] app (and maybe also AOSP Contacts?)
  - In the Google Contacts app, all notes are shown in the Contact view activity. However,
    only the first note is shown and is editable in the Contact edit activity. Removing the first
    note, saving/updating, and then editing again will allow the next note to be editable.

For more info, read [Sync contact data across devices](./../entities/sync-contact-data.md).

[built-in-note]: https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Note
[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts