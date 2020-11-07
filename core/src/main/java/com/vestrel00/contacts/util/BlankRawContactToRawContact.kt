package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.entities.BlankRawContact
import com.vestrel00.contacts.entities.RawContact

/**
 * Returns the equivalent [RawContact] with all of the latest data.
 *
 * This may return null if the [RawContact] no longer exists or if [BlankRawContact.id] is null.
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
fun BlankRawContact.toRawContact(context: Context, cancel: () -> Boolean = { false }): RawContact? =
    id?.let { rawContactId ->
        context.findFirstRawContactWithId(rawContactId, cancel)
    }