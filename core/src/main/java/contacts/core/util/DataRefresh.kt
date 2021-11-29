package contacts.core.util

import contacts.core.*
import contacts.core.data.resolveDataEntity
import contacts.core.entities.ImmutableData
import contacts.core.entities.fields

/**
 * Returns the [ImmutableData] [T] with all of the latest data.
 *
 * This is useful for getting the latest data after performing an update. This may return null if
 * the data entity no longer exists or if permission is not granted.
 *
 * Returns itself if the [ImmutableData.id] is null, indicating that this DataEntity instance has
 * not yet been inserted to the DB.
 *
 * Supports profile/non-profile native/custom data.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be
 * returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun <T : ImmutableData> T.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): T? =
    if (id == null) {
        this
    } else if (!contacts.permissions.canQuery()) {
        null
    } else {
        contacts.resolveDataEntity<T>(
            isProfile, mimeType, null,
            Include(fields(contacts.customDataRegistry) + Fields.Required.all),
            null, CompoundOrderBy(setOf(Fields.DataId.asc())), 1, 0, cancel
        ).firstOrNull()
    }

// TODO MutableData extension + async