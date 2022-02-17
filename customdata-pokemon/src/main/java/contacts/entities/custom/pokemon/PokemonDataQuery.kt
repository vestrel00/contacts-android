package contacts.entities.custom.pokemon

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory

/**
 * Queries for [Pokemon]s.
 */
fun DataQueryFactory.pokemons(): DataQuery<PokemonField, PokemonFields, Pokemon> =
    customData(PokemonMimeType)