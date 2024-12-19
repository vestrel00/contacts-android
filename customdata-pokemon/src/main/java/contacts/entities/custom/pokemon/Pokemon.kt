package contacts.entities.custom.pokemon

import contacts.core.entities.*
import kotlinx.parcelize.Parcelize

/**
 * What is a Pokemon? According to Wikipedia...
 *
 * Pokémon (an abbreviation for Pocket Monsters in Japan) is a Japanese media franchise managed by The
 * Pokémon Company, a company founded by Nintendo, Game Freak, and Creatures. The franchise was
 * created by Satoshi Tajiri in 1996, and is centered on fictional creatures called "Pokémon". In
 * Pokémon, humans, known as Pokémon Trainers, catch and train Pokémon to battle other Pokémon for
 * sport. All media works within the franchise are set in the Pokémon universe. The English slogan
 * for the franchise is "Gotta Catch ‘Em All!"
 *
 * https://en.wikipedia.org/wiki/Pokémon
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 */
sealed interface PokemonEntity : CustomDataEntity {

    /**
     * The Pokemon's name.
     */
    val name: String?

    /**
     * A Pokemon Trainer's nickname for the Pokemon.
     */
    val nickname: String?

    /**
     * The Pokemon's level.
     */
    val level: Int?

    /**
     * The Pokemon's id in the PokéApi.
     *
     * See https://pokeapi.co
     */
    val pokeApiId: Int?

    /**
     * The [name].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::name
    override val primaryValue: String?
        get() = name

    override val mimeType: MimeType.Custom
        get() = PokemonMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(name, nickname, level, pokeApiId)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): PokemonEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from PokemonEntity, there is only one interface that extends it; MutablePokemonEntity.
 *
 * The MutablePokemonEntity interface is used for library constructs that require an PokemonEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutablePokemon and NewPokemon. With this, we can create constructs that can
 * keep a reference to MutablePokemon(s) or NewPokemon(s) through the MutablePokemonEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewPokemonEntity, ExistingPokemonEntity, and
 * ImmutablePokemonEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [PokemonEntity]. `
 */
sealed interface MutablePokemonEntity : PokemonEntity, MutableCustomDataEntity {

    override var name: String?
    override var nickname: String?
    override var level: Int?
    override var pokeApiId: Int?

    /**
     * The [name].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::name
    override var primaryValue: String?
        get() = name
        set(value) {
            name = value
        }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutablePokemonEntity
}

/**
 * An existing immutable [PokemonEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class Pokemon internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val name: String?,
    override val nickname: String?,
    override val level: Int?,
    override val pokeApiId: Int?,

    override val isRedacted: Boolean

) : PokemonEntity, ExistingCustomDataEntity,
    ImmutableCustomDataEntityWithMutableType<MutablePokemon> {

    override fun mutableCopy() = MutablePokemon(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        name = name,
        nickname = nickname,
        level = level,
        pokeApiId = pokeApiId,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        nickname = nickname?.redact(),
        level = level?.redact(),
        pokeApiId = pokeApiId?.redact()
    )
}

/**
 * An existing mutable [PokemonEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class MutablePokemon internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var name: String?,
    override var nickname: String?,
    override var level: Int?,
    override var pokeApiId: Int?,

    override val isRedacted: Boolean

) : PokemonEntity, ExistingCustomDataEntity, MutablePokemonEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        nickname = nickname?.redact(),
        level = level?.redact(),
        pokeApiId = pokeApiId?.redact()
    )
}

/**
 * A new mutable [PokemonEntity].
 */
@Parcelize
data class NewPokemon @JvmOverloads constructor(

    override var name: String? = null,
    override var nickname: String? = null,
    override var level: Int? = null,
    override var pokeApiId: Int? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : PokemonEntity, NewCustomDataEntity, MutablePokemonEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        nickname = nickname?.redact(),
        level = level?.redact(),
        pokeApiId = pokeApiId?.redact()
    )
}
