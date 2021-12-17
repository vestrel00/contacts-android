package contacts.core

/**
 * Exception thrown for any contacts api errors.
 */
open class ContactsException @JvmOverloads constructor(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
