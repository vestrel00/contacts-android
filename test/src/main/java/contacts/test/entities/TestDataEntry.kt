package contacts.test.entities

import contacts.core.entities.custom.CustomDataRegistry.Entry

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class TestDataEntry : Entry<TestDataField, TestDataCursor, TestData> {

    override val mimeType = TestDataMimeType

    override val fieldSet = TestDataFields

    override val fieldMapper = TestDataFieldMapper()

    override val countRestriction = TEST_DATA_COUNT_RESTRICTION

    override val mapperFactory = TestDataMapperFactory()

    override val operationFactory = TestDataOperationFactory()
}