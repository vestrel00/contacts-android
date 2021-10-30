package contacts.test.entities

import contacts.core.entities.custom.CustomDataFieldMapper

internal class TestDataFieldMapper : CustomDataFieldMapper<TestDataField, TestData> {

    override fun valueOf(field: TestDataField, customEntity: TestData): String = when (field) {
        TestDataFields.Value -> customEntity.value
        else -> throw TestDataException("Unrecognized test data field $field")
    }
}