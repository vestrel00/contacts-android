package contacts.entities.custom.gender

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.AbstractCustomDataFieldSet
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
     * Same as [all], but as a function.
     *
     * This mainly exists for Java support.
     */
    @JvmStatic
    fun all(): Set<GenderField> = all

    /**
     * [Type] and [Label] are not meant for matching.
     *
     * See [AbstractCustomDataFieldSet.forMatching] for more info.
     */
    override val forMatching: Set<GenderField> = emptySet()


    /**
     * Same as [forMatching], but as a function.
     *
     * This mainly exists for Java support.
     */
    @JvmStatic
    fun forMatching(): Set<GenderField> = forMatching

}