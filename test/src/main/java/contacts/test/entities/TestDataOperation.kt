package contacts.test.entities

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class TestDataOperationFactory :
    AbstractCustomDataOperation.Factory<TestDataField, TestDataEntity> {

    override fun create(
        isProfile: Boolean, includeFields: Set<TestDataField>
    ): AbstractCustomDataOperation<TestDataField, TestDataEntity> =
        TestDataOperation(isProfile, includeFields)
}

private class TestDataOperation(isProfile: Boolean, includeFields: Set<TestDataField>) :
    AbstractCustomDataOperation<TestDataField, TestDataEntity>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = TestDataMimeType

    override fun setCustomData(
        data: TestDataEntity, setValue: (field: TestDataField, value: Any?) -> Unit
    ) {
        setValue(TestDataFields.Value, data.value)
    }
}