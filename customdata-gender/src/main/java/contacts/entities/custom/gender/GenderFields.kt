package contacts.entities.custom.gender

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.AbstractCustomDataFieldSet
import contacts.core.Where
import contacts.core.entities.MimeType

data class GenderField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {

    override val customMimeType: MimeType.Custom = GenderMimeType
}

object GenderFields : AbstractCustomDataFieldSet<GenderField>() {

    @JvmField
    val Type = GenderField(ColumnName.TYPE)

    @JvmField
    val Label = GenderField(ColumnName.LABEL)

    override val all: Set<GenderField> = setOf(Type, Label)

    /**
     * [Type] and [Label] are not meant for matching.
     *
     * See [AbstractCustomDataFieldSet.forMatching] for more info.
     */
    override val forMatching: Set<GenderField> = emptySet()

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

@Suppress("FunctionName")
inline fun GenderFields.Type(where: GenderField.() -> Where<GenderField>): Where<GenderField> =
    where(Type)

@Suppress("FunctionName")
inline fun GenderFields.Label(where: GenderField.() -> Where<GenderField>): Where<GenderField> =
    where(Label)