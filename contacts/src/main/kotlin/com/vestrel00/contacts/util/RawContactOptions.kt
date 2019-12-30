package com.vestrel00.contacts.util

import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.OptionsCursor
import com.vestrel00.contacts.entities.mapper.OptionsMapper
import com.vestrel00.contacts.entities.operation.OptionsOperation
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo

/**
 * Returns the [Options] of this [RawContact].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
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
fun RawContact.options(context: Context): Options = rawContactOptions(id, context)

/**
 * See [RawContact.options].
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
fun MutableRawContact.options(context: Context): Options = rawContactOptions(id, context)

private fun rawContactOptions(rawContactId: Long, context: Context): Options {
    if (!ContactsPermissions(context).canQuery() || rawContactId == INVALID_ID) {
        return MutableOptions().toOptions()
    }

    val cursor = context.contentResolver.query(
        Table.RAW_CONTACTS.uri,
        Include(Fields.Options).columnNames,
        "${(Fields.RawContact.Id equalTo rawContactId)}",
        null,
        null
    )

    if (cursor != null && cursor.moveToNext()) {
        val options = OptionsMapper(OptionsCursor(cursor)).options

        cursor.close()

        return options
    }

    return MutableOptions().toOptions()
}

/**
 * Updates this [RawContact.options] with the given [options].
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
fun RawContact.setOptions(context: Context, options: MutableOptions): Boolean =
    setOptions(id, options, context)

/**
 * Updates this [RawContact.options] in [update]. If this contact has null options, a new blank
 * options will be used in [update].
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
fun RawContact.updateOptions(context: Context, update: MutableOptions.() -> Unit): Boolean =
    updateOptions(id, options(context), update, context)

/**
 * See [RawContact.setOptions].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.setOptions(context: Context, options: MutableOptions): Boolean =
    setOptions(id, options, context)

/**
 * See [RawContact.updateOptions].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun MutableRawContact.updateOptions(
    context: Context, update: MutableOptions.() -> Unit
): Boolean = updateOptions(id, options(context), update, context)

private fun setOptions(rawContactId: Long, options: MutableOptions, context: Context): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || rawContactId == INVALID_ID) {
        return false
    }

    val operation = OptionsOperation().updateRawContactOptions(rawContactId, options)

    /*
     * Update the ContactOptionsColumns of the RawContact row matching the RawContacts._ID.
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
    rawContactId: Long,
    options: Options,
    update: MutableOptions.() -> Unit,
    context: Context
): Boolean {
    val mutableOptions = options.toMutableOptions()
    mutableOptions.update()
    return setOptions(rawContactId, mutableOptions, context)
}