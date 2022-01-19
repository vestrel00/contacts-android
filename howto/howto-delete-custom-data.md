# How do I use delete APIs to delete custom data?

This library provides several APIs that supports deleting custom data.

1. `DataDelete`
   - [How do I delete existing sets of data?](/howto/howto-delete-data-sets.md)
2. `Delete`
    - [How do I delete Contacts/RawContacts?](/howto/howto-delete-contacts.md)
3. `ProfileDelete`
    - [How do I delete the device owner Contact profile?](/howto/howto-delete-profile.md)
4. `Update`
    - [How do I update contacts?](/howto/howto-update-contacts.md)
5. `ProfileUpdate`
    - [How do I update the device owner Contact profile?](/howto/howto-update-profile.md)

To help illustrate how custom data integrates with these update APIs, we'll use the `HandleName`
and `Gender` custom data.

> For more info about custom data, read [How do I integrate custom data?](/howto/howto-integrate-custom-data.md)

## Deleting custom data via Contacts/RawContacts

Custom data, just like regular data kinds, are attached to a RawContact. They follow the same rules
as regular data kinds.

> For more info, read [How do I learn more about the API entities?](/howto/howto-learn-more-about-api-entities.md)

For example, you are able to delete existing handle names and the gender of an existing RawContact,

```kotlin
mutableRawContact.removeHandleName(contactsApi, handleName)
mutableRawContact.setGender(contactsApi, null)
```

There are also extensions that allow you to delete custom data of an existing RawContact via a
Contact, which can be made up of one or more RawContacts,

```kotlin
mutableContact.removeHandleName(contactsApi, handleName)
mutableContact.setGender(contactsApi, null)
```

Once you have removed custom data, you can perform the update operation on the RawContact to commit 
your changes into the database using `Update` or `ProfileUpdate`.

You may also delete an entire Contact or RawContact using `Delete` or `ProfileDelete` in order
delete all associated data.

## Deleting set of custom data directly

All custom data are compatible with the `DataDelete` API, which allows you to delete sets of
existing regular and custom data kinds.

For example, to delete a set of `HandleName`s and `Gender`s,

```kotlin
val handleNames: List<MutableHandleName>
val genders: List<MutableGender>

val deleteResult = Contacts(this)
   .data()
   .delete()
   .data(handleNames + genders)
   .commit()
```

For more info, read [How do I delete existing sets of data?](/howto/howto-delete-data-sets.md)
