package contacts.entities.custom.pokemon

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper

internal class PokemonMapperFactory :
    AbstractCustomDataEntityMapper.Factory<PokemonField, PokemonDataCursor, Pokemon> {

    override fun create(
        cursor: Cursor, includeFields: Set<PokemonField>
    ): AbstractCustomDataEntityMapper<PokemonField, PokemonDataCursor, Pokemon> =
        PokemonMapper(PokemonDataCursor(cursor, includeFields))
}

private class PokemonMapper(cursor: PokemonDataCursor) :
    AbstractCustomDataEntityMapper<PokemonField, PokemonDataCursor, Pokemon>(cursor) {

    override fun value(cursor: PokemonDataCursor) = Pokemon(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        name = cursor.name,
        nickname = cursor.nickname,
        level = cursor.level,
        pokeApiId = cursor.pokeApiId,

        isRedacted = false
    )
}