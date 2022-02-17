package contacts.entities.custom.pokemon

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.AbstractCustomDataFieldSet
import contacts.core.entities.MimeType

data class PokemonField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {

    override val customMimeType: MimeType.Custom = PokemonMimeType
}

object PokemonFields : AbstractCustomDataFieldSet<PokemonField>() {

    @JvmField
    val Name = PokemonField(ColumnName.DATA)

    @JvmField
    val Nickname = PokemonField(ColumnName.DATA4)

    @JvmField
    val Level = PokemonField(ColumnName.DATA5)

    @JvmField
    val PokeApiId = PokemonField(ColumnName.DATA6)

    override val all: Set<PokemonField> = setOf(Name, Nickname, Level, PokeApiId)

    /**
     * Only string should be used for matching.
     *
     * See [AbstractCustomDataFieldSet.forMatching] for more info.
     */
    override val forMatching: Set<PokemonField> = setOf(Name, Nickname)

    /**
     * Same as [all], but as a function. This mainly exists for Java support. This makes it visible
     * to Java consumers when accessing this using the object reference directly.
     */
    @JvmStatic
    fun all() = all

    /**
     * Same as [forMatching], but as a function. This makes it visible to Java consumers when
     * accessing this using the object reference directly.
     */
    @JvmStatic
    fun forMatching() = forMatching
}