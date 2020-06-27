package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.data.resolveDataEntity
import com.vestrel00.contacts.entities.DataEntity
import com.vestrel00.contacts.entities.fields

/**
 * Returns the [DataEntity] with all of the latest data.
 *
 * This is useful for getting the latest data after performing an update. This may return null if
 * the [DataEntity] no longer exists or if permission is not granted.
 *
 * Returns itself if the [DataEntity.id] is null, indicating that this DataEntity instance has not
 * yet been inserted to the DB.
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
fun <T : DataEntity> T.refresh(context: Context, cancel: () -> Boolean = { false }): T? =
    if (id == null) {
        this
    } else if (!ContactsPermissions(context).canQuery()) {
        null
    } else {
        context.contentResolver.resolveDataEntity<T>(
            mimeType, null, Include(mimeType.fields() + Fields.Required),
            null, CompoundOrderBy(setOf(Fields.DataId.asc())), 1, 0, cancel
        ).firstOrNull()
    }