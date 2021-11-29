package contacts.test.entities

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory

internal fun DataQueryFactory.testData(): DataQuery<TestDataField, TestData> =
    customData(TestDataMimeType)