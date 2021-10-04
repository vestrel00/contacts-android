package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.RelationField
import contacts.core.entities.MimeType
import contacts.core.entities.MutableRelation

internal class RelationOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<RelationField, MutableRelation>(isProfile) {

    override val mimeType = MimeType.Relation

    override fun setData(
        data: MutableRelation, setValue: (field: RelationField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Relation.Type, data.type?.value)
        setValue(Fields.Relation.Label, data.label)
        setValue(Fields.Relation.Name, data.name)
    }
}