package contacts.test.entities

import contacts.core.entities.custom.CustomDataFieldMapper

internal class TestDataFieldMapper : CustomDataFieldMapper<TestDataField, TestDataEntity> {

    override fun valueOf(field: TestDataField, customDataEntity: TestDataEntity): String? = when (field) {
        TestDataFields.Value -> customDataEntity.value
        else -> throw TestDataException("Unrecognized test data field $field")
    }
}