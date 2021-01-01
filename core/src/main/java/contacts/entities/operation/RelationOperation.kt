package contacts.entities.operation

import contacts.CommonDataField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableRelation

internal class RelationOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableRelation>(isProfile) {

    override val mimeType = MimeType.Relation

    override fun setData(
        data: MutableRelation, setValue: (field: CommonDataField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Relation.Type, data.type?.value)
        setValue(Fields.Relation.Label, data.label)
        setValue(Fields.Relation.Name, data.name)
    }
}