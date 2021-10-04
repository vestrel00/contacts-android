package contacts.core.entities.custom

import contacts.core.ContactsException

/**
 * Exception thrown for any custom data errors.
 */
open class CustomDataException(message: String) : ContactsException(message)