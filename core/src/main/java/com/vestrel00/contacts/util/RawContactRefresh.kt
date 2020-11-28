package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Query
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.equalTo
import com.vestrel00.contacts.profile.ProfileQuery

/**
 * Returns the [RawContact] with all of the latest data.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [RawContact] no longer exists or if permission is not granted.
 *
 * Returns itself if the [RawContact.id] is null, indicating that this RawContact instance has not
 * yet been inserted to the DB.
 *
 * Supports profile and non-profile RawContacts.
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
fun RawContact.refresh(context: Context, cancel: () -> Boolean = { false }): RawContact? =
    if (id == null) {
        this
    } else {
        context.findFirstRawContactWithId(id, cancel)
    }

/**
 * See [RawContact.refresh].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun MutableRawContact.refresh(
    context: Context, cancel: () -> Boolean = { false }
): MutableRawContact? = if (id == null) {
    this
} else {
    context.findFirstRawContactWithId(id, cancel)?.toMutableRawContact()
}

internal fun Context.findFirstRawContactWithId(
    rawContactId: Long, cancel: () -> Boolean
): RawContact? = if (rawContactId.isProfileId) {
    ProfileQuery(this)
        .includeBlanks(true)
        .find(cancel)
        ?.rawContacts
        ?.find { it.id == rawContactId }
} else {
    Query(this)
        .includeBlanks(true)
        .where(Fields.RawContact.Id equalTo rawContactId)
        .find(cancel)
        .firstOrNull()
        ?.rawContacts
        ?.firstOrNull()
}