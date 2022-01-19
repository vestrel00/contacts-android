# How do I use update APIs to update custom data?

This library provides several update APIs that support custom data integration.

1. `Update`
    - [How do I update contacts?](/howto/howto-update-contacts.md)
2. `ProfileUpdate`
    - [How do I update the device owner Contact profile?](/howto/howto-update-profile.md)
3. `DataUpdate`
    - [How do I update existing sets of data?](/howto/howto-update-data-sets.md)

To help illustrate how custom data integrates with these update APIs, we'll use the `HandleName`
and `Gender` custom data.

> For more info about custom data, read [How do I integrate custom data?](/howto/howto-integrate-custom-data.md)

## Updating custom data via Contacts/RawContacts

Custom data, just like regular data kinds, are attached to a RawContact. They follow the same rules
as regular data kinds.

> For more info, read [How do I learn more about the API entities?](/howto/howto-learn-more-about-api-entities.md)

For example, you are able to update existing handle names and the gender of an existing RawContact,

```kotlin
mutableRawContact.handleNames(contactsApi).firstOrNull()?.apply {
   handle = "gal91"
}
mutableRawContact.gender(contactsApi)?.apply {
   type = GenderEntity.Type.FEMALE
}
```

There are also extensions that allow you to update custom data of an existing RawContact via a 
Contact, which can be made up of one or more RawContacts,

```kotlin
mutableContact.handleNames(contactsApi).firstOrNull()?.apply {
   handle = "gal91"
}
mutableContact.genders(contactsApi).firstOrNull()?.apply {
   type = GenderEntity.Type.FEMALE
}
```

Once you have made the updates to existing custom data, you can perform the update operation on the 
RawContact to commit your changes into the database using `Update` or `ProfileUpdate`.

## Updating sets of custom data directly

All custom data are compatible with the `DataUpdate` API, which allows you to update sets of 
existing regular and custom data kinds.

For example, to update a set of `HandleName`s and `Gender`s,

```kotlin
val handleNames: List<MutableHandleName>
val genders: List<MutableGender>

val updateResult = Contacts(this)
   .data()
   .update()
   .data(handleNames + genders)
   .commit()
```

For more info, read [How do I update existing sets of data?](/howto/howto-update-data-sets.md)

## The `include` function and custom data

All of the above mentioned APIs provide an `include` function that allows you to include only a
given set of fields (data) to be processed in the update operation. Custom data entries provides
fields that can be used in this function.

By default, not calling the `include` function will include all fields, including custom data fields.

For example, to specifically include only `HandleName` and `Gender` fields,

```kotlin
.include(HandleNameFields.all + GenderFields.all)
```

For more info, read [How do I include only the data that I want?](/howto/howto-include-only-desired-data.md)

## Blank data are deleted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are
deleted by update APIs.

For more info, read [How do I learn more about "blank" data?](/howto/howto-learn-more-about-blank-data.md)
