package contacts.entities.custom

import contacts.ContactsException

/**
 * Exception thrown for any custom data errors.
 */
open class CustomDataException(message: String) : ContactsException(message)