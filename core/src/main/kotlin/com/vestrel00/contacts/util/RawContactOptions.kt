package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.MutableOptions
import com.vestrel00.contacts.entities.Options
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.entities.cursor.getNextOrNull
import com.vestrel00.contacts.entities.mapper.optionsMapper
import com.vestrel00.contacts.entities.operation.OptionsOperation
import com.vestrel00.contacts.entities.table.Table

/**
 * Returns the [Options] of this [RawContactEntity].
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
fun RawContactEntity.options(context: Context): Options {
    val rawContactId = id

    if (!ContactsPermissions(context).canQuery() || rawContactId == null) {
        return Options()
    }

    return context.contentResolver.query(
        Table.RawContacts,
        Include(Fields.Options),
        RawContactsFields.Id equalTo rawContactId
    ) {
        it.getNextOrNull { it.optionsMapper().value }
    } ?: Options()
}

/**
 * Updates this [RawContactEntity.options] with the given [options].
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
fun RawContactEntity.setOptions(context: Context, options: MutableOptions): Boolean {
    val rawContactId = id

    if (!ContactsPermissions(context).canInsertUpdateDelete() || rawContactId == null) {
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
fun RawContactEntity.updateOptions(context: Context, update: MutableOptions.() -> Unit): Boolean {
    val mutableOptions = options(context).toMutableOptions()
    mutableOptions.update()
    return setOptions(context, mutableOptions)
}