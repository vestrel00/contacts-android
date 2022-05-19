# Insert custom data into new or existing contacts

Regular and custom data can only be created/inserted into the database whenever inserting or
updating new or existing contacts.

This library provides several insert and update APIs that support custom data integration.

1. `Insert`
    - [Insert contacts](./../basics/insert-contacts.md)
2. `ProfileInsert`
    - [Insert device owner Contact profile](./../profile/insert-profile.md)
3. `Update`
    - [Update contacts](./../basics/update-contacts.md)
2. `ProfileUpdate`
    - [Update device owner Contact profile](./../profile/update-profile.md)

To help illustrate how custom data integrates with these query APIs, we'll use the `HandleName`
and `Gender` custom data.

> ℹ️ For more info, read [Integrate the gender custom data](./../customdata/integrate-gender-custom-data.md)
> and [Integrate the handle name custom data](./../customdata/integrate-handlename-custom-data.md).

## Creating/inserting custom data into a RawContact

Custom data, just like regular data kinds, are attached to a RawContact. They follow the same rules
as regular data kinds.

> ℹ️ For more info, read about [API Entities](./../entities/about-api-entities.md).

For example, you are able to add handle names and set gender of a new RawContact,

```kotlin
newRawContact.addHandleName(contactsApi) {
   handle = "dude91"
}
newRawContact.setGender(contactsApi) {
   type = GenderEntity.Type.MALE
}
```

Once you have created/insert the custom data into the RawContact, you can perform the insert 
operation on the new RawContact to commit your changes into the database.

For more info, read [Insert data into new or existing contacts](./../data/insert-data-sets.md).

## The `include` function and custom data

All of the above mentioned APIs provide an `include` function that allows you to include only a 
given set of fields (data) to be processed in the insert operation. Custom data entries provides 
fields that can be used in this function.

By default, not calling the `include` function will include all fields, including custom data fields.

For example, to specifically include only `HandleName` and `Gender` fields,

```kotlin
.include(HandleNameFields.all + GenderFields.all)
```

For more info, read [Include only certain fields for read and write operations](./../entities/include-only-desired-data.md).

## Blank data are not inserted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are
ignored and are not inserted by insert APIs.

For more info, read about [Blank data](./../entities/about-blank-data.md).
