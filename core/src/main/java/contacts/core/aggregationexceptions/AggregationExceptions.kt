package contacts.core.aggregationexceptions

import contacts.core.Contacts

/**
 * Provides new [ContactLink] and [ContactUnlink] instances.
 */
interface AggregationExceptions {

    /**
     * Returns a new [ContactLink] instance.
     */
    fun link(): ContactLink

    /**
     * Returns a new [ContactUnlink] instance.
     */
    fun unlink(): ContactUnlink

    /**
     * A reference to the [Contacts] instance that constructed this. This is mostly used internally
     * to shorten internal code.
     *
     * Don't worry, [Contacts] does not keep references to instances of this. There are no circular
     * references that could cause leaks =). [Contacts] is just a factory.
     */
    val contactsApi: Contacts
}

/**
 * Creates a new [AggregationExceptions] instance.
 */
@Suppress("FunctionName")
internal fun AggregationExceptions(contacts: Contacts): AggregationExceptions =
    AggregationExceptionsImpl(contacts)

private class AggregationExceptionsImpl(override val contactsApi: Contacts) :
    AggregationExceptions {

    override fun link() = ContactLink(contactsApi)

    override fun unlink() = ContactUnlink(contactsApi)

}