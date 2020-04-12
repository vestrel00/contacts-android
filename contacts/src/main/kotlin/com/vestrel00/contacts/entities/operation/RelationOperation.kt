package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.AbstractField
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableRelation

internal class RelationOperation : AbstractDataOperation<MutableRelation>() {

    override val mimeType = MimeType.RELATION

    override fun setData(
        data: MutableRelation, setValue: (field: AbstractField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Relation.Type, data.type?.value)
        setValue(Fields.Relation.Label, data.label)
        setValue(Fields.Relation.Name, data.name)
    }
}