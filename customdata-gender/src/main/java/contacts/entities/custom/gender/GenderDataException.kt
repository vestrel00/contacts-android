package contacts.entities.custom.gender

import contacts.entities.custom.CustomDataException

/**
 * Exception thrown for any gender data errors.
 */
class GenderDataException(message: String) : CustomDataException(message)