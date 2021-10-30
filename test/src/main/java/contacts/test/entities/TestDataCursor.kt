package contacts.test.entities

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataCursor

internal class TestDataCursor(cursor: Cursor, includeFields: Set<TestDataField>) :
    AbstractCustomDataCursor<TestDataField>(cursor, includeFields) {

    val value: String by nonNullString(TestDataFields.Value, TestData.VALUE)
}