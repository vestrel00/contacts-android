package contacts.entities.custom.multiplenotes

import contacts.core.entities.custom.CustomDataException

/**
 * Exception thrown for any multiple notes data errors.
 */
class MultipleNotesDataException(message: String) : CustomDataException(message)