package contacts.permissions.sim

import contacts.core.ContactsPermissions
import contacts.core.sim.SimCardInfo
import contacts.core.sim.SimCardMaxCharacterLimits
import contacts.permissions.requestReadPermission
import contacts.permissions.requestWritePermission

/**
 * If [ContactsPermissions.READ_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION] is not yet
 * granted, suspends the current coroutine, requests for the permissions, and then returns a new
 * [SimCardMaxCharacterLimits] instance.
 *
 * If permission is already granted, then immediately returns a new [SimCardMaxCharacterLimits]
 * instance.
 */
suspend fun SimCardInfo.maxCharacterLimitsWithPermission(): SimCardMaxCharacterLimits {
    if (!contactsApi.permissions.canQuery()) {
        requestReadPermission()
    }

    if (!contactsApi.permissions.canInsertToSim() || !contactsApi.permissions.canUpdateDelete()) {
        requestWritePermission()
    }

    return maxCharacterLimits()
}