package contacts.entities.custom.pokemon

import contacts.core.entities.custom.CustomDataException

/**
 * Exception thrown for any pokemon data errors.
 */
class PokemonDataException(message: String) : CustomDataException(message)