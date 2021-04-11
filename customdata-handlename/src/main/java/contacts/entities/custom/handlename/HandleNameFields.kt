package contacts.entities.custom.handlename

import contacts.AbstractCustomDataField
import contacts.AbstractCustomDataField.ColumnName
import contacts.AbstractCustomDataFieldSet
import contacts.entities.MimeType

data class HandleNameField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {

    override val customMimeType: MimeType.Custom = HandleNameMimeType
}

object HandleNameFields : AbstractCustomDataFieldSet<HandleNameField>() {

    @JvmField
    val Handle = HandleNameField(ColumnName.DATA)

    override val all: Set<HandleNameField> = setOf(Handle)

    /**
     * Same as [all], but as a function.
     *
     * This mainly exists for Java support.
     */
    @JvmStatic
    fun all(): Set<HandleNameField> = all

    /**
     * The [Handle] may be used for matching.
     *
     * See [AbstractCustomDataFieldSet.forMatching] for more info.
     */
    override val forMatching: Set<HandleNameField> = setOf(Handle)


    /**
     * Same as [forMatching], but as a function.
     *
     * This mainly exists for Java support.
     */
    @JvmStatic
    fun forMatching(): Set<HandleNameField> = forMatching

}