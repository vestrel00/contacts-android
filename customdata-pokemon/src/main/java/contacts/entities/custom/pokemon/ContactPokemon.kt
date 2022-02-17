package contacts.entities.custom.pokemon

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact
import contacts.core.util.sortedById

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region Contact

/**
 * Returns the sequence of [Pokemon]s from all [Contact.rawContacts] ordered by id
 */
fun Contact.pokemons(contacts: Contacts): Sequence<Pokemon> = rawContacts
    .asSequence()
    .flatMap { it.pokemons(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [Pokemon]s from all [Contact.rawContacts] ordered by the [Pokemon.id].
 */
fun Contact.pokemonList(contacts: Contacts): List<Pokemon> = pokemons(contacts).toList()

// endregion

// region MutableContact

/**
 * Returns the sequence of [MutablePokemon]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.pokemons(contacts: Contacts): Sequence<MutablePokemonEntity> = rawContacts
    .asSequence()
    .flatMap { it.pokemons(contacts) }
    .sortedById()

/**
 * Returns the list of [MutablePokemonEntity]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.pokemonList(contacts: Contacts): List<MutablePokemonEntity> =
    pokemons(contacts).toList()

/**
 * Adds the given [pokemon] to the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 */
fun MutableContact.addPokemon(contacts: Contacts, pokemon: MutablePokemonEntity) {
    rawContacts.firstOrNull()?.addPokemon(contacts, pokemon)
}

/**
 * Adds a new pokemon s(configured by [configurePokemon]) to the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 */
fun MutableContact.addPokemon(
    contacts: Contacts,
    configurePokemon: NewPokemon.() -> Unit
) {
    addPokemon(contacts, NewPokemon().apply(configurePokemon))
}

/**
 * Removes all instances of the given [pokemon] from all [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableContact.removePokemon(
    contacts: Contacts,
    pokemon: MutablePokemonEntity,
    byReference: Boolean = false
) {
    for (rawContact in rawContacts) {
        rawContact.removePokemon(contacts, pokemon, byReference)
    }
}

/**
 * Removes all pokemons from all [MutableContact.rawContacts].
 */
fun MutableContact.removeAllPokemons(contacts: Contacts) {
    for (rawContact in rawContacts) {
        rawContact.removeAllPokemons(contacts)
    }
}

// endregion