package contacts.entities.custom.handlename

import contacts.core.entities.custom.CustomDataException

/**
 * Exception thrown for any handle name data errors.
 */
class HandleNameDataException(message: String) : CustomDataException(message)