package contacts.entities.custom.gender

import contacts.AbstractCustomDataField
import contacts.AbstractCustomDataField.ColumnName
import contacts.AbstractCustomDataFieldSet
import contacts.entities.MimeType

data class GenderField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {
    override val customMimeType: MimeType.Custom = GenderMimeType
}

object GenderFields : AbstractCustomDataFieldSet<GenderField>() {

    @JvmField
    val Type = GenderField(ColumnName.TYPE)

    @JvmField
    val Label = GenderField(ColumnName.LABEL)

    override val all = setOf(Type, Label)

    /**
     * [Type] amd [Label] are not meant for matching.
     *
     * See [AbstractCustomDataFieldSet.forMatching] for more info.
     */
    override val forMatching = emptySet<GenderField>()
}