# Integrate the Pokemon custom data

This library provides extensions for `Pokemon` custom data that allows you to read and write
pokemon data for all of your contacts. These (optional) extensions live in the 
`customdata-pokemon` module. 

> ℹ️ If you are looking to create your own custom data or get more insight on how the `Pokemon` 
> custom data was built, read [Integrate custom data](./../customdata/integrate-custom-data.md).

## Register the pokemon custom data with the Contacts API instance

You may register the `Pokemon` custom data when creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(
    context,
    customDataRegistry = CustomDataRegistry().register(
        PokemonRegistration()
    )
)
```

Or, alternatively after creating the `Contacts` API instance,

```kotlin
val contactsApi = Contacts(context)
PokemonRegistration().registerTo(contactsApi.customDataRegistry)
```

## Get/add/remove pokemon custom data

Just like regular data kinds, pokemon custom data belong to a RawContact. A RawContact may have 
0, 1, or more pokemons.

To get the pokemons of a RawContact,

```kotlin
val pokemonSequence = rawContact.pokemons(contactsApi)
val pokemonList = rawContact.pokemonList(contactsApi)
```

To get the pokemons of all RawContacts belonging to a Contact,

```kotlin
val pokemonSequence = contact.pokemons(contactsApi)
val pokemonList = contact.pokemonList(contactsApi)
```

To add a pokemon to a (mutable) RawContact,

```kotlin
mutableRawContact.addPokemon(contacts, mutablePokemon)
// or
mutableRawContact.addPokemon(contacts) {
    name = "ditto"
    nickname = "copy-cat"
    level = 24
    pokeApiId = 132
}
```

To add a pokemon to a the first RawContact or a Contact,

```kotlin
mutableContact.addPokemon(contacts, mutablePokemon)
// or
mutableContact.addPokemon(contacts) {
    name = "ditto"
    nickname = "copy-cat"
    level = 24
    pokeApiId = 132
}
```

## Use the pokemon custom data in queries, inserts, updates, and deletes

Once you have registered your pokemon custom data with the `Contacts` API instance, the API 
instance is now able to perform read and write operations on it.

- [Query custom data](./../customdata/query-custom-data.md)
- [Insert custom data into new or existing contacts](./../customdata/insert-custom-data.md)
- [Update custom data](./../customdata/update-custom-data.md)
- [Delete custom data](./../customdata/delete-custom-data.md)

## Syncing pokemon custom data

This library does not provide sync adapters for pokemon custom data. Unless you implement your
own sync adapter, pokemon custom data...

- will NOT be synced across devices
- will NOT be shown in AOSP and [Google Contacts][google-contacts] apps, and other Contacts apps
  that show custom data from other apps

For more info, read [Sync contact data across devices](./../entities/sync-contact-data.md).

[google-contacts]: https://play.google.com/store/apps/details?id=com.google.android.contacts