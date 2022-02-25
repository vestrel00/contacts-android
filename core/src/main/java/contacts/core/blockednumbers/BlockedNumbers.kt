package contacts.core.blockednumbers

import contacts.core.Contacts

interface BlockedNumbers {

    /**
     * Returns a [BlockedNumbersPrivileges] instance, which provides functions for checking required
     * privileges for blocked number operations.
     */
    val privileges: BlockedNumbersPrivileges
}

/**
 * Creates a new [BlockedNumbers] instance.
 */
@Suppress("FunctionName")
internal fun BlockedNumbers(contacts: Contacts): BlockedNumbers =
    BlockedNumbersImpl(
        BlockedNumberPrivileges(contacts.applicationContext)
    )

private class BlockedNumbersImpl(
    override val privileges: BlockedNumbersPrivileges
) : BlockedNumbers