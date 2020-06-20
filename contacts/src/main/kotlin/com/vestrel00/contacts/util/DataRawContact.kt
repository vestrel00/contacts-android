package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.entities.DataEntity
import com.vestrel00.contacts.entities.RawContact

/**
 * Returns the [RawContact] with the [DataEntity.rawContactId].
 *
 * This may return null if the [RawContact] no longer exists or if [DataEntity.rawContactId] is null
 * (which is the case for manually constructed entities).
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
fun DataEntity.rawContact(context: Context, cancel: () -> Boolean = { false }): RawContact? =
    rawContactId?.let { rawContactId ->
        context.findFirstRawContactWithId(rawContactId, cancel)
    }