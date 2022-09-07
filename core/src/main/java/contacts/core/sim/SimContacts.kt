package contacts.core.sim

import contacts.core.Contacts

/**
 * Provides new [SimContactsQuery], [SimContactsInsert], [SimContactsUpdate], and
 * [SimContactsDelete] instances.
 */
interface SimContacts {

    /**
     * Returns a new [SimContactsQuery] instance.
     */
    fun query(): SimContactsQuery

    /**
     * Returns a new [SimContactsInsert] instance.
     */
    fun insert(): SimContactsInsert

    /**
     * Returns a new [SimContactsInsert] instance.
     */
    fun update(): SimContactsUpdate

    /**
     * Returns a new [SimContactsDelete] instance.
     */
    fun delete(): SimContactsDelete

    /**
     * Returns a [SimCardInfo] instance, which provides functions for checking SIM card state,
     * max character limits, etc.
     */
    val cardInfo: SimCardInfo

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
 * Creates a new [SimContacts] instance.
 */
@Suppress("FunctionName")
internal fun SimContacts(contacts: Contacts): SimContacts =
    SimContactsImpl(SimCardInfo(contacts), contacts)

private class SimContactsImpl(
    override val cardInfo: SimCardInfo,
    override val contactsApi: Contacts
) : SimContacts {

    override fun query() = SimContactsQuery(contactsApi)

    override fun insert() = SimContactsInsert(contactsApi)

    override fun update() = SimContactsUpdate(contactsApi)

    override fun delete() = SimContactsDelete(contactsApi)
}