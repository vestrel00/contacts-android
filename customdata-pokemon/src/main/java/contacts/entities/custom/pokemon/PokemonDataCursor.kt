package contacts.entities.custom.pokemon

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor

internal class PokemonDataCursor(cursor: Cursor, includeFields: Set<PokemonField>?) :
    AbstractCustomDataCursor<PokemonField>(cursor, includeFields) {

    val name: String? by string(PokemonFields.Name)
    val nickname: String? by string(PokemonFields.Nickname)
    val level: Int? by int(PokemonFields.Level)
    val pokeApiId: Int? by int(PokemonFields.PokeApiId)
}