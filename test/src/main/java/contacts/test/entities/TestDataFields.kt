package contacts.test.entities

import contacts.core.AbstractCustomDataField
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.AbstractCustomDataFieldSet
import contacts.core.Where
import contacts.core.entities.MimeType

internal data class TestDataField internal constructor(private val columnName: ColumnName) :
    AbstractCustomDataField(columnName) {

    override val customMimeType: MimeType.Custom = TestDataMimeType
}

internal object TestDataFields : AbstractCustomDataFieldSet<TestDataField>() {

    val Value = TestDataField(ColumnName.DATA)

    override val all: Set<TestDataField> = setOf(Value)

    override val forMatching: Set<TestDataField> = emptySet()
}

@Suppress("FunctionName")
internal inline fun TestDataFields.Handle(
    where: TestDataField.() -> Where<TestDataField>
): Where<TestDataField> = where(Value)