package contacts.test.entities

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation

internal class TestDataOperationFactory :
    AbstractCustomDataOperation.Factory<TestDataField, TestDataEntity> {

    override fun create(
        callerIsSyncAdapter: Boolean, isProfile: Boolean, includeFields: Set<TestDataField>
    ): AbstractCustomDataOperation<TestDataField, TestDataEntity> = TestDataOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        includeFields = includeFields
    )
}

private class TestDataOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<TestDataField>
) : AbstractCustomDataOperation<TestDataField, TestDataEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType: MimeType.Custom = TestDataMimeType

    override fun setCustomData(
        data: TestDataEntity, setValue: (field: TestDataField, value: Any?) -> Unit
    ) {
        setValue(TestDataFields.Value, data.value)
    }
}