package contacts.test.entities

import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact
import contacts.core.entities.custom.CustomDataRegistry

/**
 * Returns the [TestData] of this RawContact. Null if not available (e.g. does not exist in the
 * database or was not an included field in the query).
 */
internal fun RawContact.testData(customDataRegistry: CustomDataRegistry): TestData? {
    val customDataEntities =
        customDataRegistry.customDataEntitiesFor<TestData>(this, TestDataMimeType)

    // We know that there can only be one test data so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Returns the [TestData] of this RawContact. Null if not available (e.g. does not exist in
 * the database or was not an included field in the query).
 */
internal fun MutableRawContact.testData(customDataRegistry: CustomDataRegistry): TestData? {
    val customDataEntities =
        customDataRegistry.customDataEntitiesFor<TestData>(this, TestDataMimeType)

    // We know that there can only be one test data so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the test data of this RawContact to mark it for tests only.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
internal fun MutableRawContact.setTestData(
    testData: TestData, // We should not allow setting test data to null in tests!
    customDataRegistry: CustomDataRegistry
) {
    customDataRegistry.putCustomDataEntityInto(this, testData)
}