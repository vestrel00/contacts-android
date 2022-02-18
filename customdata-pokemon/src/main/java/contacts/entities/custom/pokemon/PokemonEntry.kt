package contacts.entities.custom.pokemon

import contacts.core.entities.custom.CustomDataRegistry.Entry

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class PokemonEntry : Entry<PokemonField, PokemonDataCursor, PokemonEntity, Pokemon> {

    override val mimeType = PokemonMimeType

    override val fieldSet = PokemonFields

    override val fieldMapper = PokemonFieldMapper()

    override val countRestriction = POKEMON_COUNT_RESTRICTION

    override val mapperFactory = PokemonMapperFactory()

    override val operationFactory = PokemonOperationFactory()
}