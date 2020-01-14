package com.vestrel00.contacts.util

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newUpdate
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.DataEntity
import com.vestrel00.contacts.entities.INVALID_ID
import com.vestrel00.contacts.entities.mimeType
import com.vestrel00.contacts.entities.operation.withValue
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo

/**
 * Returns the default data entity in the collection or null if not found.
 */
fun Collection<DataEntity>.default(): DataEntity? = firstOrNull { it.isDefault() }

/**
 * Returns the default data entity in the sequence or null if not found.
 */
fun Sequence<DataEntity>.default(): DataEntity? = firstOrNull { it.isDefault() }

/**
 * Sets this data as the default for the set of data of the same type (e.g. email) for the aggregate
 * Contact. If a default data already exist before this call, then it will no longer be the default.
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
fun DataEntity.setAsDefault(context: Context): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || id == INVALID_ID) {
        return false
    }

    val operations = arrayListOf(clearPrimary(), clearSuperPrimary(), setPrimaryAndSuperPrimary())

    // Perform this single operation in a batch to be consistent with the other CRUD functions.
    try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
    } catch (exception: Exception) {
        return false
    }

    return true
}

/**
 * Removes any default data of the same type (e.g. email), if any, for the aggregate Contact.
 *
 * For example, these emails belong to the same aggregate Contact;
 *
 * - x@x.com
 * - y@y.com (default)
 * - z@z.com
 *
 * Calling this function on the default (y@y.com) or any of the non-default emails (x@x.com or
 * z@z.com) will remove any default email set for the aggregate contact, which results in;
 *
 * - x@x.com
 * - y@y.com
 * - z@z.com
 *
 * See DEV_NOTES "Data Primary and Super Primary Rows" section for more info.
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
fun DataEntity.clearDefault(context: Context): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || id == INVALID_ID) {
        return false
    }

    val operations = arrayListOf(clearPrimary(), clearSuperPrimary())

    // Perform this single operation in a batch to be consistent with the other CRUD functions.
    try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
    } catch (exception: Exception) {
        return false
    }

    return true
}

/**
 * Provides the operation to set all primary data rows with the same [DataEntity.mimeType]
 * belonging to the same RawContact to false (0).
 *
 * See DEV_NOTES "Data Primary and Super Primary Rows" section for more info.
 */
private fun DataEntity.clearPrimary(): ContentProviderOperation = newUpdate(TABLE_URI)
    .withSelection(
        "${(Fields.RawContactId equalTo rawContactId) and (Fields.MimeType equalTo mimeType())}",
        null
    )
    .withValue(Fields.IsPrimary, 0)
    .build()

/**
 * Provides the operation to set all super primary data rows with the same [DataEntity.mimeType]
 * belonging to the same Contact to false (0).
 *
 * See DEV_NOTES "Data Primary and Super Primary Rows" section for more info.
 */
private fun DataEntity.clearSuperPrimary(): ContentProviderOperation = newUpdate(TABLE_URI)
    .withSelection(
        "${(Fields.Contact.Id equalTo contactId) and (Fields.MimeType equalTo mimeType())}",
        null
    )
    .withValue(Fields.IsSuperPrimary, 0)
    .build()

/**
 * Provides the operation to set this data row as the primary and super primary.
 *
 * See DEV_NOTES "Data Primary and Super Primary Rows" section for more info.
 */
private fun DataEntity.setPrimaryAndSuperPrimary(): ContentProviderOperation = newUpdate(TABLE_URI)
    .withSelection("${Fields.Id equalTo id}", null)
    .withValue(Fields.IsPrimary, 1)
    .withValue(Fields.IsSuperPrimary, 1)
    .build()

private val TABLE_URI = Table.DATA.uri