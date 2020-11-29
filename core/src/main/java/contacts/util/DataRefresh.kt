package contacts.util

import android.content.Context
import contacts.*
import contacts.data.resolveDataEntity
import contacts.entities.CommonDataEntity
import contacts.entities.fields

/**
 * Returns the [CommonDataEntity] with all of the latest data.
 *
 * This is useful for getting the latest data after performing an update. This may return null if
 * the [CommonDataEntity] no longer exists or if permission is not granted.
 *
 * Returns itself if the [CommonDataEntity.id] is null, indicating that this DataEntity instance has not
 * yet been inserted to the DB.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * The [com.vestrel00.contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null
 * will be returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun <T : CommonDataEntity> T.refresh(context: Context, cancel: () -> Boolean = { false }): T? =
    if (id == null) {
        this
    } else if (!ContactsPermissions(context).canQuery()) {
        null
    } else {
        context.contentResolver.resolveDataEntity<T>(
            isProfile, mimeType, null, Include(mimeType.fields + Fields.Required.all),
            null, CompoundOrderBy(setOf(Fields.DataId.asc())), 1, 0, cancel
        ).firstOrNull()
    }