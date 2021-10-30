package contacts.test.entities

import contacts.core.data.CommonDataQuery
import contacts.core.data.DataQuery

internal fun DataQuery.testData(): CommonDataQuery<TestDataField, TestData> =
    customData(TestDataMimeType)