package contacts.test.entities

import contacts.core.entities.custom.CustomDataException

/**
 * Exception thrown for any test data errors.
 */
internal class TestDataException(message: String) : CustomDataException(message)