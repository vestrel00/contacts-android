# How do I integrate the gender custom data?

This library comes with `Gender` custom data that allows you to add gender information for all of
you contacts. This custom data lives in the `customdata-gender` module. It is optional.

> If you are looking to create your own custom data or get more insight on how the `Gender` custom
> data was built, read [How do I integrate custom data?](/contacts-android/howto/howto-integrate-custom-data.html)

## Register the gender custom data with the Contacts API instance

You may register the `Gender` custom data when creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(
    context,
    customDataRegistry = CustomDataRegistry().register(
        GenderRegistration()
    )
)
```

Or, alternatively after creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(context)
GenderRegistration().registerTo(contacts.customDataRegistry)
```

## Get/set gender custom data 

Just like regular data kinds, gender custom data belong to a RawContact. A RawContact may only have
0 or 1 gender.

To get the gender of a RawContact,

```kotlin
val gender = rawContact.gender(contactsApi)
```

To get the genders of all RawContacts belonging to a Contact,

```kotlin
val genderSequence = contact.genders(contactsApi)
val genderList = contact.genderList(contactsApi)
```

To set the gender of a (mutable) RawContact,

```kotlin
mutableRawContact.setGender(contacts, mutableGender)
// or
mutableRawContact.setGender(contacts) {
    type = GenderEntity.Type.MALE
}
```

To set the gender of the first RawContact in a Contact,

```kotlin
mutableContact.setGender(contacts, mutableGender)
// or
mutableContact.setGender(contacts) {
    type = GenderEntity.Type.MALE
}
```

## Use the gender custom data in queries, inserts, updates, and deletes

Once you have registered your custom data with the `Contacts` API instance, the API instance is now
able to perform read and write operations on it.

- [How do I use query APIs to get custom data?](/contacts-android/howto/howto-query-custom-data.html)
- [How do I use insert and update APIs to create/insert custom data into new or existing contacts?](/contacts-android/howto/howto-insert-custom-data.html)
- [How do I use update APIs to update custom data?](/contacts-android/howto/howto-update-custom-data.html)
- [How do I use delete APIs to delete custom data?](/contacts-android/howto/howto-delete-custom-data.html)

## Limitations

This library does not provide sync adapters for gender custom data. Unless you implement your own 
sync adapter, gender custom data...

- will NOT be synced across devices
- will NOT be shown in AOSP and [Google Contacts][google-contacts] apps, and other Contacts apps
  that show custom data from other apps

[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts
