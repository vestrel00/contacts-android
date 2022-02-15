package contacts.entities.custom.googlecontacts.fileas

import contacts.core.entities.custom.CustomDataException

/**
 * Exception thrown for any File As data errors.
 */
class FileAsDataException(message: String) : CustomDataException(message)