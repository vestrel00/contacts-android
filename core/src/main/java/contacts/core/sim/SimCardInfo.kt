package contacts.core.sim

import contacts.core.Contacts
import contacts.core.isSimCardReady

/**
 * Provides functions for checking SIM card state, max character limits, etc.
 */
interface SimCardInfo {

    /**
     * Returns true if the default/active SIM card is ready for use.
     */
    val isReady: Boolean

    /**
     * Returns a new instance of [SimCardMaxCharacterLimits].
     */
    fun maxCharacterLimits(): SimCardMaxCharacterLimits

    /**
     * A reference to the [Contacts] instance that constructed this. This is mostly used internally
     * to shorten internal code.
     *
     * Don't worry, [Contacts] does not keep references to instances of this. There are no circular
     * references that could cause leaks =). [Contacts] is just a factory.
     */
    val contactsApi: Contacts
}

internal fun SimCardInfo(contactsApi: Contacts): SimCardInfo = SimCardInfoImpl(contactsApi)

private class SimCardInfoImpl(override val contactsApi: Contacts) : SimCardInfo {

    override val isReady: Boolean
        get() = contactsApi.isSimCardReady

    override fun maxCharacterLimits() = SimCardMaxCharacterLimits(contactsApi)
}