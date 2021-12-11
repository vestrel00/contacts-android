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

        val existingDataEntityIsImmutable = existingDataEntity is ImmutableDataEntity
        val resultShouldBeMutable = this is MutableDataEntity

        @Suppress("UNCHECKED_CAST")
        return if (existingDataEntityIsImmutable && resultShouldBeMutable) {
            // Need to cast to mutable entity
            when (existingDataEntity) {
                null -> null
                is ImmutableDataEntityWithMutableType<*> -> existingDataEntity.mutableCopy() as T
                is ImmutableDataEntityWithNullableMutableType<*> -> existingDataEntity.mutableCopy() as T?
                else -> existingDataEntity
            }
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