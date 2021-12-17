package contacts.test.entities

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContact
import contacts.core.entities.custom.CustomDataRegistry

// region RawContact

/**
 * Returns the [TestData] of this RawContact.
 */
internal fun RawContact.testData(contacts: Contacts): TestData? {
    val customDataEntities =
        contacts.customDataRegistry.customDataEntitiesFor<TestData>(this, TestDataMimeType)

    // We know that there can only be one test data so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

// endregion

// region MutableRawContact

/**
 * Returns the [MutableTestDataEntity] of this RawContact.
 */
internal fun MutableRawContact.testData(contacts: Contacts): MutableTestDataEntity? {
    val customDataEntities =
        contacts.customDataRegistry.customDataEntitiesFor<MutableTestDataEntity>(
            this,
            TestDataMimeType
        )

    // We know that there can only be one test data so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the test data of this RawContact to the given [testData].
 */
internal fun MutableRawContact.setTestData(contacts: Contacts, testData: MutableTestDataEntity?) {
    if (testData != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, testData)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, TestDataMimeType)
    }
}

/**
 * Sets the test data of this RawContact to a [NewTestData] configured by [configureTestData].
 */
internal fun MutableRawContact.setTestData(
    contacts: Contacts,
    configureTestData: NewTestData.() -> Unit
) {
    setTestData(contacts, NewTestData().apply(configureTestData))
}

// endregion

// region NewRawContact

/**
 * Returns the [NewTestData] of this RawContact.
 */
internal fun NewRawContact.testData(contacts: Contacts): NewTestData? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewTestData>(this, TestDataMimeType)

    // We know that there can only be one testData so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the testData of this RawContact to the given [testData].
 */
internal fun NewRawContact.setTestData(contacts: Contacts, testData: NewTestData?) {
    if (testData != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, testData)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, TestDataMimeType)
    }
}

/**
 * Sets the testData of this RawContact to a [NewTestData] configured by [configureTestData].
 */
internal fun NewRawContact.setTestData(
    contacts: Contacts,
    configureTestData: NewTestData.() -> Unit
) {
    setTestData(contacts, NewTestData().apply(configureTestData))
}

// endregion