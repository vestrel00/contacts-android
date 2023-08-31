package contacts.core.util

import contacts.core.*
import contacts.core.data.resolveDataEntity
import contacts.core.entities.*

/**
 * Returns the [ExistingDataEntity] [T] with all of the latest data.
 *
 * This is useful for getting the latest data after performing an update. This may return null if
 * the data entity no longer exists or if permission is not granted.
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
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun <T : ExistingDataEntity> T.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): T? {
    return if (!contacts.permissions.canQuery()) {
        null
    } else {
        val existingDataEntity: T? = fetchDataEntity(contacts, id, cancel)

        @Suppress("UNCHECKED_CAST")
        return if (
            existingDataEntity is ImmutableDataEntityWithMutableType<*> &&
            this is MutableDataEntity
        ) {
            existingDataEntity.mutableCopy() as T
        } else {
            existingDataEntity
        }
    }
}

private fun <T : ExistingDataEntity> T.fetchDataEntity(
    contacts: Contacts,
    dataId: Long,
    cancel: () -> Boolean = { false }
): T? = contacts.resolveDataEntity<T>(
    isProfile,
    mimeType,
    null,
    Include(fields(contacts.customDataRegistry) + Fields.Required.all),
    Fields.DataId equalTo dataId,
    CompoundOrderBy(setOf(Fields.DataId.asc())),
    1,
    0,
    cancel
).firstOrNull()