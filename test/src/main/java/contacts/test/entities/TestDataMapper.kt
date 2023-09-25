package contacts.test.entities

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper

internal class TestDataMapperFactory :
    AbstractCustomDataEntityMapper.Factory<TestDataField, TestDataCursor, TestData> {

    override fun create(
        cursor: Cursor, includeFields: Set<TestDataField>?
    ): AbstractCustomDataEntityMapper<TestDataField, TestDataCursor, TestData> =
        TestDataMapper(TestDataCursor(cursor, includeFields))
}

private class TestDataMapper(cursor: TestDataCursor) :
    AbstractCustomDataEntityMapper<TestDataField, TestDataCursor, TestData>(cursor) {

    override fun value(cursor: TestDataCursor) = TestData(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        value = cursor.value,

        isRedacted = false
    )
}