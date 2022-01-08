# How do I use query APIs to get custom data?

This library provides several query APIs that support custom data integration.

1. `Query`
    - [How do I get a list of contacts in a more advanced way?](/contacts-android/howto/howto-query-contacts-advanced.html)
2. `BroadQuery`
    - [How do I get a list of contacts in the simplest way?](/contacts-android/howto/howto-query-contacts.html)
3. `ProfileQuery`
    - [How do I get the device owner Contact profile?](/contacts-android/howto/howto-query-profile.html)
4. `DataQuery`
    - [How do I get a list of specific data kinds?](/contacts-android/howto/howto-query-data-sets.html)

To help illustrate how custom data integrates with these query APIs, we'll use the `HandleName`
and `Gender` custom data.

> For more info, read [How do I integrate the gender custom data?](/contacts-android/howto/howto-integrate-gender-custom-data.html)
> and [How do I integrate the handle name custom data?](/contacts-android/howto/howto-integrate-handlename-custom-data.html)

## Getting custom data from a Contact or RawContact

Custom data, just like regular data kinds, are attached to a RawContact. They follow the same rules
as regular data kinds. 

> For more info, read [How do I learn more about the API entities?](/contacts-android/howto/howto-learn-more-about-api-entities.html)

For example, you are able to get the handle names and gender of a RawContact,

```kotlin
val handleNames = rawContact.handleNames(contactsApi)
val gender = rawContact.gender(contactsApi)
```

There are also extensions that allow you to get custom data from a Contact, which can be made up of 
one or more RawContacts,

```kotlin
val handleNames = contact.handleNames(contactsApi)
val genders = contact.genders(contactsApi)
```

## Getting specific custom data kinds directly

Every custom data provides an extension to the `DataQuery` that allows you to query for only that
specific custom data kind.

For example, to get all available `HandleName`s and `Gender`s from all contacts,

```kotlin
val handleNames = Contacts(context).data().query().handleNames().find()
val genders = Contacts(context).data().query().genders().find()
```

To get all `HandleName`s starting with the letter "h",

```kotlin
val handleNames = Contacts(context)
    .data()
    .query()
    .handleNames()
    .where { Handle startsWith "h" }
    .find()
```

For more info, read [How do I get a list of specific data kinds?](/contacts-android/howto/howto-query-data-sets.html)

## The `include` function and custom data

All of the above mentioned APIs provide an `include` function that allows you to include only a 
given set of fields (data) in each of the returned entities. Custom data entries provides fields 
that can be used in this function. 

By default, not calling the `include` function will include all fields, including custom data fields. 

For example, to explicitly include all `HandleName` fields, 

```kotlin
.include(HandleNameFields.all)
```

For more info, read [How do I include only the data that I want?](/contacts-android/howto/howto-include-only-desired-data.html)

## The `where` function and custom data

The `Query` and `DataQuery` APIs provides a `where` function that allows you to specify a matching
criteria based on specific field values. Custom data entries provides fields that can be used in 
this function. For example, to match `HandleName`s starting with the letter "h",

```kotlin
.where { Handle startsWith "h" }
```

The `BroadQuery` API provides a `whereAnyContactDataPartiallyMatches` function that NOT support
matching custom data. Only native data are included in the matching process.

The `ProfileQuery` API does not provide a where function as there can only be one profile Contact
per device. 

## The `orderBy` function and custom data

The `DataQuery` API provides an `orderBy` function that supports custom data. For example, to order
`HandleName`s,

```kotlin
.orderBy(HandleNameFields.Handle.asc())
```

The `Query` and `BroadQuery` APIs provides an `orderBy` function that only takes in fields from
the Contacts table, not data. So there is no custom data, or native data, support for this.

The `ProfileQuery` API does not provide an `orderBy` function as there can only be at most one 
profile Contact on the device.
