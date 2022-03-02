# Query custom data

This library provides several query APIs that support custom data integration.

1. `Query`
    - [Query contacts (advanced)](/howto/query-contacts-advanced.md)
2. `BroadQuery`
    - [Query contacts (broad)](/howto/query-contacts.md)
3. `ProfileQuery`
    - [Query device owner Contact profile](/howto/query-profile.md)
4. `DataQuery`
    - [Query specific data kinds](/howto/query-data-sets.md)

To help illustrate how custom data integrates with these query APIs, we'll use the `HandleName`
and `Gender` custom data.

> For more info, read [Integrate the gender custom data](/howto/integrate-gender-custom-data.md)
> and [Integrate the handle name custom data](/howto/integrate-handlename-custom-data.md).

## Getting custom data from a Contact or RawContact

Custom data, just like regular data kinds, are attached to a RawContact. They follow the same rules
as regular data kinds. 

> For more info, read about [API Entities](/howto/about-api-entities.md).

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

For more info, read [Query specific data kinds](/howto/query-data-sets.md).

## The `include` function and custom data

All of the above mentioned APIs provide an `include` function that allows you to include only a 
given set of fields (data) in each of the returned entities. Custom data entries provides fields 
that can be used in this function. 

By default, not calling the `include` function will include all fields, including custom data fields. 

For example, to explicitly include all `HandleName` fields, 

```kotlin
.include(HandleNameFields.all)
```

For more info, read [Include only certain fields for read and write operations](/howto/include-only-desired-data.md).

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