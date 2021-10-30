package contacts.core.util

import contacts.core.*
import contacts.core.entities.ContactEntity
import contacts.core.entities.MutableOptions
import contacts.core.entities.Options
import contacts.core.entities.mapper.optionsMapper
import contacts.core.entities.operation.OptionsOperation
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table

/**
 * Returns the most up-to-date [Options] of this [ContactEntity], which may be different from this
 * instance's options immutable member variable as it may be stale.
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.options(contacts: Contacts): Options {
    val contactId = id

    if (!contacts.permissions.canQuery || contactId == null) {
        return Options()
    }

    return contacts.applicationContext.contentResolver.query(
        if (isProfile) ProfileUris.CONTACTS.uri else Table.Contacts.uri,
        Include(ContactsFields.Options),
        ContactsFields.Id equalTo contactId
    ) {
        it.getNextOrNull { it.optionsMapper().value }
    } ?: Options()
}

/**
 * Updates this [ContactEntity.options] with the given [options].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * This will not change the value of this instance's options immutable member variable! You will
 * need to refresh this instance or use [ContactEntity.options] extension function to get the most
 * up-to-date options.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.setOptions(contacts: Contacts, options: MutableOptions): Boolean {
    val contactId = id

    if (!contacts.permissions.canUpdateDelete || contactId == null) {
        return false
    }

    return contacts.applicationContext.contentResolver.applyBatch(
        OptionsOperation().updateContactOptions(contactId, options)
    ) != null
}

/**
 * Updates this [ContactEntity.options] in [update]. If this contact has null options, a new blank
 * options will be used in [update].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * This will not change the value of this instance's options immutable member variable! You will
 * need to refresh this instance or use [ContactEntity.options] extension function to get the most
 * up-to-date options.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.updateOptions(contacts: Contacts, update: MutableOptions.() -> Unit): Boolean {
    val optionsToUse = options(contacts).toMutableOptions()
    optionsToUse.update()
    return setOptions(contacts, optionsToUse)
}