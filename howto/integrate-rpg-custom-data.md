# Integrate the Role Playing Game (RPG) custom data

This provides extensions for `RpgStats` and `RpgProfession` custom data that allows you to read and 
write rpg data for all of your contacts. These (optional) extensions live in the 
`customdata-rpg` module.

> If you are looking to create your own custom data or get more insight on how the `RpgStats` and 
> `RpgProfession` custom data was built, read [Integrate custom data](/howto/integrate-custom-data.md).

## Register the RPG custom data with the Contacts API instance

You may register all RPG custom data when creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(
    context,
    customDataRegistry = CustomDataRegistry().register(
        RpgRegistration()
    )
)
```

Or, alternatively after creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(context)
RpgRegistration().registerTo(contactsApi.customDataRegistry)
```

## Read/write RPG custom data 

### Get/set `RpgStats`

Just like regular data kinds, `RpgStats` custom data belong to a RawContact. A RawContact may only
have 0 or 1 `RpgStats`.

To get the `RpgStats` of a RawContact,

```kotlin
val rpgStats = rawContact.rpgStats(contactsApi)
```

To get the `RpgStats` of all RawContacts belonging to a Contact,

```kotlin
val rpgStatsSequence = contact.rpgStats(contactsApi)
val rpgStatsList = contact.rpgStatsList(contactsApi)
```

To set the `RpgStats` of a (mutable) RawContact,

```kotlin
mutableRawContact.setRpgStats(contacts, mutableRpgStats)
// or
mutableRawContact.setRpgStats(contacts) {
    level = 78
    speed = 500
    strength = 789
    intelligence = 123
    luck = 999
}
```

To set the `RpgStats` of the first RawContact in a Contact,

```kotlin
mutableContact.setRpgStats(contacts, mutableRpgStats)
// or
mutableContact.setRpgStats(contacts) {
    level = 78
    speed = 500
    strength = 789
    intelligence = 123
    luck = 999
}
```

### Get/set `RpgProfession`

Just like regular data kinds, `RpgProfession` custom data belong to a RawContact. A RawContact may 
only have 0 or 1 `RpgProfession`.

To get the `RpgProfession` of a RawContact,

```kotlin
val rpgProfession = rawContact.rpgProfession(contactsApi)
```

To get the `RpgProfession` of all RawContacts belonging to a Contact,

```kotlin
val rpgProfessionSequence = contact.rpgProfessions(contactsApi)
val rpgProfessionList = contact.rpgProfessionList(contactsApi)
```

To set the `RpgProfession` of a (mutable) RawContact,

```kotlin
mutableRawContact.setRpgProfession(contacts, mutableRpgProfession)
// or
mutableRawContact.setRpgProfession(contacts) {
    title = "swordsman"
}
```

To set the `RpgProfession` of the first RawContact in a Contact,

```kotlin
mutableContact.setRpgProfession(contacts, mutableRpgProfession)
// or
mutableContact.setRpgProfession(contacts) {
    title = "swordsman"
}
```

## Use the RPG custom data in queries, inserts, updates, and deletes

Once you have registered the RPG custom data with the `Contacts` API instance, the API instance is 
now able to perform read and write operations on it.

- [Query custom data](/howto/query-custom-data.md)
- [Insert custom data into new or existing contacts](/howto/insert-custom-data.md)
- [Update custom data](/howto/update-custom-data.md)
- [Delete custom data](/howto/delete-custom-data.md)

## Syncing RPG custom data

This library does not provide sync adapters for RPG custom data. Unless you implement your own sync 
adapter, RPG custom data...

- will NOT be synced across devices
- will NOT be shown in AOSP and [Google Contacts][google-contacts] apps, and other Contacts apps
  that show custom data from other apps

For more info, read [Sync contact data across devices](/howto/sync-contact-data.md).

[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts