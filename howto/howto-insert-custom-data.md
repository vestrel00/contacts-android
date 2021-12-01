# How do I use insert and update APIs to create/insert custom data into new or existing contacts?

Regular and custom data can only be created/inserted into the database whenever inserting or 
updating new or existing contacts.

This library provides several insert and update APIs that support custom data integration.

1. `Insert`
    - [How do I create/insert contacts?](/contacts-android/howto/howto-insert-contacts.html)
2. `ProfileInsert`
    - [How do I create/insert the device owner Contact profile?](/contacts-android/howto/howto-insert-profile.html)
3. `Update`
    - [How do I update contacts?](/contacts-android/howto/howto-update-contacts.html)
2. `ProfileUpdate`
    - [How do I update the device owner Contact profile?](/contacts-android/howto/howto-update-profile.html)
    
To help illustrate how custom data integrates with these query APIs, we'll use the `HandleName`
and `Gender` custom data.

> For more info about custom data, read [How do I integrate custom data?](/contacts-android/howto/howto-integrate-custom-data.html)

## Creating/inserting custom data into a RawContact

Custom data, just like regular data kinds, are attached to a RawContact. They follow the same rules
as regular data kinds. 

For example, you are able to add handle names and set gender of a RawContact,

```kotlin
rawContact.addHandleName(contactsApi, handleName)
rawContact.setGender(contactsApi, gender)
```

> For more info, read [How do I learn more about the API entities?](/contacts-android/howto/howto-learn-more-about-api-entities.html)

There should also be extensions that allow you to set custom data using a Contact, which can be made 
up of one or more RawContacts.

> For more info, read [How do I integrate the gender custom data?](/contacts-android/howto/howto-integrate-gender-custom-data.html)
> and [How do I integrate the handle name custom data?](/contacts-android/howto/howto-integrate-handlename-custom-data.html)

Once you have created/insert the custom data into the RawContact, you can perform the insert or 
update operation on Contact(s) or RawContact(s) to commit your changes into the database.

> For more info, read [How do I create/insert data into new or existing contacts?](/contacts-android/howto/howto-insert-data-sets.html)

## The `include` function and custom data

All of the above mentioned APIs provide an `include` function that allows you to include only a 
given set of fields (data) to be processed in the insert or update operation. Custom data entries 
provides fields that can be used in this function. 

By default, not calling the `include` function will include all fields, including custom data. 

For example, to specifically include only `HandleName` and `Gender` fields, 

```kotlin
.include(HandleNameFields.all + GenderFields.all)
```

For more info, read [How do I include only the data that I want?](/contacts-android/howto/howto-include-only-desired-data.html)

## Blank data are not inserted

Blank data are data entities that have only null, empty, or blank primary value(s). Blanks are 
ignored and are not inserted by insert APIs.

For more info, read [How do I learn more about "blank" data?](/contacts-android/howto/howto-learn-more-about-blank-data.html)
