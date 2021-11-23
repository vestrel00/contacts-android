# How do I use query APIs to get custom data?

This library provides several query APIs that support custom data integration.

1. `Query`
    - [How do I get a list of contacts in a more advanced way?](/howto/howto-query-contacts-advanced.md)
2. `BroadQuery`
    - [How do I get a list of contacts in the simplest way?](/howto/howto-query-contacts.md)
3. `ProfileQuery`
    - [How do I get the device owner Contact profile?](/howto/howto-query-profile.md)
4. `DataQuery`
    - [How do I get a list of specific data kinds?](/howto/howto-query-data-sets.md)

To help illustrate how custom data integrates with these query APIs, we'll use the `HandleName`
and `Gender` custom data.

> For more info, read [How do I integrate custom data?](/howto/howto-integrate-custom-data.md)

## Getting custom data from a RawContact

Custom data, just like regular data kinds, are attached to a RawContact. They follow the same rules
as regular data kinds. 

For example, you are able to get the handle names and gender of a RawContact,

```kotlin
val handleNames = rawContact.handleNames(contactsApi)
val gender = rawContact.gender(contactsApi)
```

> For more info, read [How do I learn more about the API entities?](/howto/howto-learn-more-about-api-entities.md)

There should also be extensions that allow you to get custom data from a Contact, which can be made 
up of one or more RawContacts.

> For more info, read [How do I integrate the gender custom data?](/howto/howto-integrate-gender-custom-data.md)
> and [How do I integrate the handle name custom data?](/howto/howto-integrate-handlename-custom-data.md)

## The `include` function and custom data

All of the above mentioned APIs provide an `include` function that allows you to include only a 
given set of fields (data) in each of the returned entities. Custom data entries provides fields 
that can be used in this function. 

By default, not calling the `include` function will include all fields, including custom data. 

For example, to specifically include only `HandleName` and `Gender` fields, 

```kotlin
.include(HandleNameFields.all + GenderFields.all)
```

For more info, read [How do I include only the data that I want?](/howto/howto-include-only-desired-data.md)

## The `where` function and custom data

The `Query` and `DataQuery` APIs provides a `where` function that allows you to specify a matching
criteria based on specific field values. Custom data entries provides fields that can be used in 
this function. For example, to match `HandleName`s starting with the letter "h",

```kotlin
.where(HandleNameFields.Handle startsWith "h")
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

## The `DataQuery` API and custom data

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
    .where(HandleNameFields.Handle startsWith "h")
    .find()
```

For more info, read [How do I get a list of specific data kinds?](/howto/howto-query-data-sets.md)