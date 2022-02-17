package contacts.entities.custom.pokemon

import contacts.core.entities.custom.CustomDataFieldMapper

internal class PokemonFieldMapper : CustomDataFieldMapper<PokemonField, PokemonEntity> {

    override fun valueOf(field: PokemonField, customDataEntity: PokemonEntity): String? =
        when (field) {
            PokemonFields.Name -> customDataEntity.name
            PokemonFields.Nickname -> customDataEntity.nickname
            PokemonFields.Level -> customDataEntity.level?.toString()
            PokemonFields.PokeApiId -> customDataEntity.pokeApiId?.toString()
            else -> throw PokemonDataException("Unrecognized pokemon field $field")
        }
}