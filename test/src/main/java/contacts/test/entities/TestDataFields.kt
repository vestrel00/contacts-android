package contacts.test.entities

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.AbstractCustomDataFieldSet
import contacts.core.entities.MimeType

@ConsistentCopyVisibility
internal data class TestDataField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {

    override val customMimeType: MimeType.Custom = TestDataMimeType
}

internal object TestDataFields : AbstractCustomDataFieldSet<TestDataField>() {

    val Value = TestDataField(ColumnName.DATA)

    override val all: Set<TestDataField> = setOf(Value)

    override val forMatching: Set<TestDataField> = emptySet()
}