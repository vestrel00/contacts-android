package contacts.entities.custom.pokemon

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContact

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region RawContact

/**
 * Returns the sequence of [Pokemon]s of this RawContact.
 */
fun RawContact.pokemons(contacts: Contacts): Sequence<Pokemon> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<Pokemon>(this, PokemonMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [Pokemon]s of this RawContact.
 */
fun RawContact.pokemonList(contacts: Contacts): List<Pokemon> = pokemons(contacts).toList()

// endregion

// region MutableRawContact

/**
 * Returns the sequence of [MutablePokemonEntity]s of this RawContact.
 */
fun MutableRawContact.pokemons(contacts: Contacts): Sequence<MutablePokemonEntity> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutablePokemonEntity>(this, PokemonMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [MutablePokemonEntity]s of this RawContact.
 */
fun MutableRawContact.pokemonList(contacts: Contacts): List<MutablePokemonEntity> =
    pokemons(contacts).toList()

/**
 * Adds the given [pokemon] to this RawContact.
 */
fun MutableRawContact.addPokemon(contacts: Contacts, pokemon: MutablePokemonEntity) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, pokemon)
}

/**
 * Adds a pokemon (configured by [configurePokemon]) to this RawContact.
 */
fun MutableRawContact.addPokemon(
    contacts: Contacts,
    configurePokemon: NewPokemon.() -> Unit
) {
    addPokemon(contacts, NewPokemon().apply(configurePokemon))
}

/**
 * Removes all instances of the given [pokemon] from this RawContact.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableRawContact.removePokemon(
    contacts: Contacts,
    pokemon: MutablePokemonEntity,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, pokemon)
}

/**
 * Removes all pokemons from this RawContact.
 */
fun MutableRawContact.removeAllPokemons(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, PokemonMimeType)
}

// endregion

// region NewRawContact

/**
 * Returns the sequence of [NewPokemon]s of this RawContact.
 */
fun NewRawContact.pokemons(contacts: Contacts): Sequence<NewPokemon> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewPokemon>(this, PokemonMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [NewPokemon]s of this RawContact.
 */
fun NewRawContact.pokemonList(contacts: Contacts): List<NewPokemon> =
    pokemons(contacts).toList()

/**
 * Adds the given [pokemon] to this RawContact.
 */
fun NewRawContact.addPokemon(contacts: Contacts, pokemon: NewPokemon) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, pokemon)
}

/**
 * Adds a pokemon (configured by [configurePokemon]) to this RawContact.
 */
fun NewRawContact.addPokemon(
    contacts: Contacts,
    configurePokemon: NewPokemon.() -> Unit
) {
    addPokemon(contacts, NewPokemon().apply(configurePokemon))
}

/**
 * Removes all instances of the given [pokemon] from this RawContact.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun NewRawContact.removePokemon(
    contacts: Contacts,
    pokemon: NewPokemon,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, pokemon)
}

/**
 * Removes all pokemons from this RawContact.
 */
fun NewRawContact.removeAllPokemons(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, PokemonMimeType)
}

// endregion