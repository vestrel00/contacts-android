package contacts.entities.custom.pokemon

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class PokemonOperationFactory :
    AbstractCustomDataOperation.Factory<PokemonField, PokemonEntity> {

    override fun create(
        callerIsSyncAdapter: Boolean, isProfile: Boolean, includeFields: Set<PokemonField>?
    ): AbstractCustomDataOperation<PokemonField, PokemonEntity> = PokemonOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        includeFields = includeFields
    )
}

private class PokemonOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<PokemonField>?
) : AbstractCustomDataOperation<PokemonField, PokemonEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType: MimeType.Custom = PokemonMimeType

    override fun setCustomData(
        data: PokemonEntity, setValue: (field: PokemonField, value: Any?) -> Unit
    ) {
        setValue(PokemonFields.Name, data.name)
        setValue(PokemonFields.Nickname, data.nickname)
        setValue(PokemonFields.Level, data.level)
        setValue(PokemonFields.PokeApiId, data.pokeApiId)
    }
}