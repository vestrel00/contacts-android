package com.vestrel00.contacts.util

import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.operation.OptionsOperation

/**
 * Updates this [Contact.options] with the given [options].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun Contact.setOptions(context: Context, options: MutableOptions): Boolean =
    setOptions(id, options, context)

/**
 * Updates this [Contact.options] in [update]. If this contact has null options, a new blank options
 * will be used in [update].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun Contact.updateOptions(context: Context, update: MutableOptions.() -> Unit): Boolean =
    updateOptions(id, options, update, context)

/**
 * See [Contact.setOptions].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.setOptions(context: Context, options: MutableOptions): Boolean =
    setOptions(id, options, context)

/**
 * See [Contact.updateOptions].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableContact.updateOptions(
    context: Context, update: MutableOptions.() -> Unit
): Boolean = updateOptions(id, options, update, context)

private fun setOptions(contactId: Long, options: MutableOptions, context: Context): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || contactId == INVALID_ID) {
        return false
    }

    val operation = OptionsOperation().updateContactOptions(contactId, options)

    /*
     * Update the ContactOptionsColumns of the Contact row matching the Contacts._ID.
     *
     * Perform this single operation in a batch to be consistent with the other CRUD functions.
     */
    try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    } catch (exception: Exception) {
        return false
    }

    return true
}

private fun updateOptions(
    contactId: Long,
    optionsOptional: Options?,
    update: MutableOptions.() -> Unit,
    context: Context
): Boolean {
    val options = optionsOptional?.toMutableOptions() ?: MutableOptions()
    options.update()
    return setOptions(contactId, options, context)
}