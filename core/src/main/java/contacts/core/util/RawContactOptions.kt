package contacts.core.util

import android.content.Context
import contacts.core.ContactsPermissions
import contacts.core.Include
import contacts.core.RawContactsFields
import contacts.core.entities.MutableOptions
import contacts.core.entities.Options
import contacts.core.entities.RawContactEntity
import contacts.core.entities.mapper.rawContactsOptionsMapper
import contacts.core.entities.operation.OptionsOperation
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.equalTo

/**
 * Returns the [Options] of this [RawContactEntity].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * Supports profile and non-profile RawContacts.
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
fun RawContactEntity.options(context: Context): Options {
    val rawContactId = id

    if (!ContactsPermissions(context).canQuery() || rawContactId == null) {
        return Options()
    }

    return context.contentResolver.query(
        if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
        Include(RawContactsFields.Options),
        RawContactsFields.Id equalTo rawContactId
    ) {
        it.getNextOrNull { it.rawContactsOptionsMapper().value }
    } ?: Options()
}

/**
 * Updates this [RawContactEntity.options] with the given [options].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * Supports profile and non-profile RawContacts.
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
fun RawContactEntity.setOptions(context: Context, options: MutableOptions): Boolean {
    val rawContactId = id

    if (!ContactsPermissions(context).canUpdateDelete() || rawContactId == null) {
        return false
    }

    return context.contentResolver.applyBatch(
        OptionsOperation().updateRawContactOptions(rawContactId, options)
    ) != null
}

/**
 * Updates this [RawContactEntity.options] in [update]. If this contact has null options, a new
 * blank options will be used in [update].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * Supports profile and non-profile RawContacts.
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
fun RawContactEntity.updateOptions(context: Context, update: MutableOptions.() -> Unit): Boolean {
    val mutableOptions = options(context).toMutableOptions()
    mutableOptions.update()
    return setOptions(context, mutableOptions)
}