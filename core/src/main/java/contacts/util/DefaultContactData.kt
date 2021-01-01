package contacts.util

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newUpdate
import android.content.Context
import contacts.ContactsPermissions
import contacts.Fields
import contacts.and
import contacts.entities.CommonDataEntity
import contacts.entities.operation.withSelection
import contacts.entities.operation.withValue
import contacts.entities.table.ProfileUris
import contacts.entities.table.Table
import contacts.equalTo

/**
 * Returns the default data entity in the collection or null if not found.
 */
fun Collection<CommonDataEntity>.default(): CommonDataEntity? = firstOrNull { it.isDefault }

/**
 * Returns the default data entity in the sequence or null if not found.
 */
fun Sequence<CommonDataEntity>.default(): CommonDataEntity? = firstOrNull { it.isDefault }

/**
 * Sets this data as the default for the set of data of the same type (e.g. email) for the aggregate
 * Contact. If a default data already exist before this call, then it will no longer be the default.
 *
 * Supports profile/non-profile native/custom data.
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
fun CommonDataEntity.setAsDefault(context: Context): Boolean {
    val dataId = id
    val rawContactId = rawContactId
    val contactId = contactId

    if (!ContactsPermissions(context).canUpdateDelete()
        || dataId == null
        || rawContactId == null
        || contactId == null
    ) {
        return false
    }

    return context.contentResolver.applyBatch(
        clearPrimary(rawContactId),
        clearSuperPrimary(contactId),
        setPrimaryAndSuperPrimary(dataId)
    ) != null
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
 * Supports profile/non-profile native/custom data.
 *
 * See DEV_NOTES "Data Primary and Super Primary Rows" section for more info.
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
fun CommonDataEntity.clearDefault(context: Context): Boolean {
    val rawContactId = rawContactId
    val contactId = contactId

    if (!ContactsPermissions(context).canUpdateDelete()
        || rawContactId == null
        || contactId == null
    ) {
        return false
    }

    return context.contentResolver.applyBatch(
        clearPrimary(rawContactId),
        clearSuperPrimary(contactId)
    ) != null
}

/**
 * Provides the operation to set all primary data rows with the same [CommonDataEntity.mimeType]
 * belonging to the same RawContact to false (0).
 *
 * Supports profile/non-profile native/custom data.
 *
 * See DEV_NOTES "Data Primary and Super Primary Rows" section for more info.
 */
private fun CommonDataEntity.clearPrimary(rawContactId: Long): ContentProviderOperation =
    newUpdate(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
        .withSelection(
            (Fields.RawContact.Id equalTo rawContactId)
                    and (Fields.MimeType equalTo mimeType)
        )
        .withValue(Fields.IsPrimary, 0)
        .build()

/**
 * Provides the operation to set all super primary data rows with the same [CommonDataEntity.mimeType]
 * belonging to the same Contact to false (0).
 *
 * Supports profile/non-profile native/custom data.
 *
 * See DEV_NOTES "Data Primary and Super Primary Rows" section for more info.
 */
private fun CommonDataEntity.clearSuperPrimary(contactId: Long): ContentProviderOperation =
    newUpdate(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
        .withSelection(
            (Fields.Contact.Id equalTo contactId)
                    and (Fields.MimeType equalTo mimeType)
        )
        .withValue(Fields.IsSuperPrimary, 0)
        .build()

/**
 * Provides the operation to set this data row as the primary and super primary.
 *
 * Supports profile/non-profile native/custom data.
 *
 * See DEV_NOTES "Data Primary and Super Primary Rows" section for more info.
 */
private fun CommonDataEntity.setPrimaryAndSuperPrimary(dataId: Long): ContentProviderOperation =
    newUpdate(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
        .withSelection(Fields.DataId equalTo dataId)
        .withValue(Fields.IsPrimary, 1)
        .withValue(Fields.IsSuperPrimary, 1)
        .build()