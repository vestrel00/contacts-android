package contacts.test.entities

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomEntityMapper

internal class TestDataMapperFactory :
    AbstractCustomEntityMapper.Factory<TestDataField, TestDataCursor, TestData> {

    override fun create(
        cursor: Cursor, includeFields: Set<TestDataField>
    ): AbstractCustomEntityMapper<TestDataField, TestDataCursor, TestData> =
        TestDataMapper(TestDataCursor(cursor, includeFields))
}

private class TestDataMapper(cursor: TestDataCursor) :
    AbstractCustomEntityMapper<TestDataField, TestDataCursor, TestData>(cursor) {

    override fun value(cursor: TestDataCursor) = TestData(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        value = cursor.value
    )
}