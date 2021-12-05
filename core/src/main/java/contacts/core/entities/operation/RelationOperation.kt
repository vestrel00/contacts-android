package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.RelationField
import contacts.core.entities.MimeType
import contacts.core.entities.RelationEntity

internal class RelationOperation(isProfile: Boolean, includeFields: Set<RelationField>) :
    AbstractDataOperation<RelationField, RelationEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.Relation

    override fun setValuesFromData(
        data: RelationEntity, setValue: (field: RelationField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Relation.Type, data.type?.value)
        setValue(Fields.Relation.Label, data.label)
        setValue(Fields.Relation.Name, data.name)
    }
}