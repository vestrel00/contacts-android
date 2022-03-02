# Integrate the handle name custom data

This library provides extensions for `HandleName` custom data that allows you to read and write
handle name data for all of your contacts. These (optional) extensions live in the 
`customdata-handlename` module. 

> If you are looking to create your own custom data or get more insight on how the `HandleName` 
> custom data was built, read [Integrate custom data](/docs/customdata/integrate-custom-data.md).

## Register the handle name custom data with the Contacts API instance

You may register the `HandleName` custom data when creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(
    context,
    customDataRegistry = CustomDataRegistry().register(
        HandleNameRegistration()
    )
)
```

Or, alternatively after creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(context)
HandleNameRegistration().registerTo(contactsApi.customDataRegistry)
```

## Get/add/remove handle name custom data

Just like regular data kinds, handle name custom data belong to a RawContact. A RawContact may have 
0, 1, or more handle names.

To get the handle names of a RawContact,

```kotlin
val handleNameSequence = rawContact.handleNames(contactsApi)
val handleNameList = rawContact.handleNameList(contactsApi)
```

To get the handle names of all RawContacts belonging to a Contact,

```kotlin
val handleNameSequence = contact.handleNames(contactsApi)
val handleNameList = contact.handleNameList(contactsApi)
```

To add a handle name to a (mutable) RawContact,

```kotlin
mutableRawContact.addHandleName(contacts, mutableHandleName)
// or
mutableRawContact.addHandleName(contacts) {
    handle = "CoolDude91"
}
```

To add a handle name to a the first RawContact or a Contact,

```kotlin
mutableContact.addHandleName(contacts, mutableHandleName)
// or
mutableContact.addHandleName(contacts) {
    handle = "CoolGal89"
}
```

## Use the handle name custom data in queries, inserts, updates, and deletes

Once you have registered your handle name custom data with the `Contacts` API instance, the API 
instance is now able to perform read and write operations on it.

- [Query custom data](/docs/customdata/query-custom-data.md)
- [Insert custom data into new or existing contacts](/docs/customdata/insert-custom-data.md)
- [Update custom data](/docs/customdata/update-custom-data.md)
- [Delete custom data](/docs/customdata/delete-custom-data.md)

## Syncing handle name custom data

This library does not provide sync adapters for handle name custom data. Unless you implement your
own sync adapter, handle name custom data...

- will NOT be synced across devices
- will NOT be shown in AOSP and [Google Contacts][google-contacts] apps, and other Contacts apps
  that show custom data from other apps

For more info, read [Sync contact data across devices](/docs/entities/sync-contact-data.md).

[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts