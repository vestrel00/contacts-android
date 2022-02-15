package contacts.entities.custom.googlecontacts.userdefined

import contacts.core.entities.custom.CustomDataException

/**
 * Exception thrown for any user defined data errors.
 */
class UserDefinedDataException(message: String) : CustomDataException(message)