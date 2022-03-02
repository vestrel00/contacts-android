# Integrate the Google Contacts custom data

This library provides extensions for custom data from the [Google Contacts][google-contacts] app; 
`FileAs` and `UserDefined`, which allows you to read and write Google Contacts data for all of your 
contacts. These (optional) extensions live in the `customdata-googlecontacts` module.

> If you are looking to create your own custom data or get more insight on how the `FileAs` and 
> `UserDefined` custom data was built, read [Integrate custom data](/howto/integrate-custom-data.md).

## Register the Google Contacts custom data with the Contacts API instance

You may register all Google Contacts custom data when creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(
    context,
    customDataRegistry = CustomDataRegistry().register(
        GoogleContactsRegistration()
    )
)
```

Or, alternatively after creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(context)
GoogleContactsRegistration().registerTo(contactsApi.customDataRegistry)
```

## Read/write Google Contacts custom data 

### Get/set `FileAs`

Just like regular data kinds, `FileAs` custom data belong to a RawContact. A RawContact may only
have 0 or 1 `FileAs`.

To get the `FileAs` of a RawContact,

```kotlin
val fileAs = rawContact.fileAs(contactsApi)
```

To get the `FileAs` of all RawContacts belonging to a Contact,

```kotlin
val fileAsSequence = contact.fileAs(contactsApi)
val fileAsList = contact.fileAsList(contactsApi)
```

To set the `FileAs` of a (mutable) RawContact,

```kotlin
mutableRawContact.setFileAs(contacts, mutableFileAs)
// or
mutableRawContact.setFileAs(contacts) {
    name = "Robot"
}
```

To set the `FileAs` of the first RawContact in a Contact,

```kotlin
mutableContact.setFileAs(contacts, mutableFileAs)
// or
mutableContact.setFileAs(contacts) {
  name = "Robot"
}
```

### Get/add/remove `UserDefined`

Just like regular data kinds, `UserDefined` custom data belong to a RawContact. A RawContact may 
have 0, 1, or more `UserDefined`.

To get the `UserDefined` list/sequence of a RawContact,

```kotlin
val userDefinedSequence = rawContact.userDefined(contactsApi)
val userDefinedList = rawContact.userDefinedList(contactsApi)
```

To get the `UserDefined` of all RawContacts belonging to a Contact,

```kotlin
val userDefinedSequence = contact.userDefined(contactsApi)
val userDefinedList = contact.userDefinedList(contactsApi)
```

To add a `UserDefined` to a (mutable) RawContact,

```kotlin
mutableRawContact.addUserDefined(contacts, mutableUserDefined)
// or
mutableRawContact.addUserDefined(contacts) {
    field = "My Field"
    label = "My Label"
}
```

To add a `UserDefined` to the first RawContact in a Contact,

```kotlin
mutableContact.addUserDefined(contacts, mutableUserDefined)
// or
mutableContact.addUserDefined(contacts) {
  field = "My Field"
  label = "My Label"
}
```

## Use the Google Contacts custom data in queries, inserts, updates, and deletes

Once you have registered the Google Contacts custom data with the `Contacts` API instance, the API 
instance is now able to perform read and write operations on it.

- [Query custom data](/howto/query-custom-data.md)
- [Insert custom data into new or existing contacts](/howto/insert-custom-data.md)
- [Update custom data](/howto/update-custom-data.md)
- [Delete custom data](/howto/delete-custom-data.md)

## Google Contacts app data integrity

When inserting or updating a `UserDefined` data kind, the Google Contacts app enforces 
`UserDefined.field` and `UserDefined.label` to both be non-null and non-blank. Otherwise, the insert
or update operation fails. To protect the data integrity that the Google Contacts app imposes, this 
library is silently not performing insert or update operations for these instances. Consumers are 
informed via documentation.

Both `field` and `label` must be non-null and non-blank strings in order for insert and update
operations to be performed on them. The corresponding fields must also be included in the insert
or update operation. Otherwise, the update and insert operation will silently NOT be performed.

We might change the way we handle this in the future. Maybe we'll throw an exception instead or
fail the entire insert/update and bubble up the reason. For now, to avoid complicating the API
in these early stages, we'll go with silent but documented. We'll see what the community thinks!

## Google Contacts app UI

In the [Google Contacts app][google-contacts], the `FileAs` and `UserDefined` custom data are only 
shown for RawContacts that are associated with a Google Account. Local (device-only) RawContacts do 
not have these custom data!

> For more info on local contacts, read about [Local (device-only) contacts](/howto/about-local-contacts.md).

## Syncing Google Contacts custom data

The [Google Contacts app][google-contacts] comes with sync adapters that is responsible for syncing
`FileAs` and `UserDefined` custom data. As long as you have the Google Contacts app installed, 
these custom data should remain synced depending on account sync settings.

> This library does not provide sync adapters for Google Contacts custom data.

For more info, read [Sync contact data across devices](/howto/sync-contact-data.md).

[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts