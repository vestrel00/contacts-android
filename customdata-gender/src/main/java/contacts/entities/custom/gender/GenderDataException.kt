package contacts.entities.custom.gender

import contacts.core.entities.custom.CustomDataException

/**
 * Exception thrown for any gender data errors.
 */
class GenderDataException(message: String) : CustomDataException(message)