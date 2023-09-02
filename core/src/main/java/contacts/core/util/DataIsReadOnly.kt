package contacts.core.util

import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.Include
import contacts.core.and
import contacts.core.contentResolver
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.cursor.dataCursor
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.equalTo
import contacts.core.`in`

/**
 * Returns true if this [ExistingDataEntity] is read-only.
 *
 * Supports profile/non-profile native/custom data.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required.
 *
 * ## Cancellation
 *
 * To cancel this operation at any time, the [cancel] function should return true.
 *
 * This is useful when running this function in a background thread or coroutine.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 *
 * ## Dev notes
 *
 * The value of [android.provider.ContactsContract.DataColumns.IS_READ_ONLY] is not a property of
 * [ExistingDataEntity] because including that particular column in the query projection array
 * causes an exception. However, it is still possible to use the column in selection/WHERE clauses.
 * Thus, allowing us to create this extension function =)
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun ExistingDataEntity.isReadOnly(contacts: Contacts, cancel: () -> Boolean = { false }): Boolean =
    if (!contacts.permissions.canQuery()) {
        false
    } else {
        contacts.dataReadOnlyList(isProfile, setOf(id), cancel).isNotEmpty()
    }

/**
 * Returns a map of [ExistingDataEntity.id] to a [Boolean] that represents whether or not the data
 * is read-only.
 *
 * Supports profile/non-profile native/custom data.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required.
 *
 * ## Cancellation
 *
 * To cancel this operation at any time, the [cancel] function should return true.
 *
 * This is useful when running this function in a background thread or coroutine.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 *
 * ## Dev notes
 *
 * The value of [android.provider.ContactsContract.DataColumns.IS_READ_ONLY] is not a property of
 * [ExistingDataEntity] because including that particular column in the query projection array
 * causes an exception. However, it is still possible to use the column in selection/WHERE clauses.
 * Thus, allowing us to create this extension function =)
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Collection<ExistingDataEntity>.isReadOnlyMap(
    contacts: Contacts, cancel: () -> Boolean = { false }
): Map<Long, Boolean> = if (!contacts.permissions.canQuery()) {
    emptyMap()
} else {
    // Init the map with false values for all ids.
    val dataReadOnlyMap: MutableMap<Long, Boolean> =
        associateBy({ it.id }, { false }).toMutableMap()

    val nonProfileDataIds = filterNot { it.isProfile }.map { it.id }
    val profileDataIds = filter { it.isProfile }.map { it.id }

    if (nonProfileDataIds.isNotEmpty()) {
        contacts.dataReadOnlyList(false, nonProfileDataIds, cancel).forEach {
            dataReadOnlyMap[it] = true
        }
    }

    if (profileDataIds.isNotEmpty()) {
        contacts.dataReadOnlyList(true, profileDataIds, cancel).forEach {
            dataReadOnlyMap[it] = true
        }
    }

    dataReadOnlyMap
}

private fun Contacts.dataReadOnlyList(
    isProfile: Boolean,
    existingDataIds: Collection<Long>,
    cancel: () -> Boolean = { false }
): List<Long> = contentResolver.query(
    if (isProfile) ProfileUris.DATA.uri else Table.Data.uri,
    Include(Fields.DataId),
    (Fields.DataId `in` existingDataIds) and (Fields.DataIsReadOnly equalTo true)
) {
    mutableListOf<Long>().apply {
        val dataCursor = it.dataCursor()
        while (!cancel() && it.moveToNext()) {
            add(dataCursor.dataId)
        }
    }
} ?: emptyList()