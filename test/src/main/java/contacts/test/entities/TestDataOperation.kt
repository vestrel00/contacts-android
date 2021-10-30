package contacts.test.entities

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class TestDataOperationFactory :
    AbstractCustomDataOperation.Factory<TestDataField, TestData> {

    override fun create(
        isProfile: Boolean, includeFields: Set<TestDataField>
    ): AbstractCustomDataOperation<TestDataField, TestData> =
        TestDataOperation(isProfile, includeFields)
}

private class TestDataOperation(isProfile: Boolean, includeFields: Set<TestDataField>) :
    AbstractCustomDataOperation<TestDataField, TestData>(isProfile, includeFields) {

    override val mimeType: MimeType.Custom = TestDataMimeType

    override fun setCustomData(
        data: TestData, setValue: (field: TestDataField, value: Any?) -> Unit
    ) {
        setValue(TestDataFields.Value, data.value)
    }
}