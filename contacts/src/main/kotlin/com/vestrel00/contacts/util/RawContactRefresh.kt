package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Query
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.equalTo

/**
 * Returns the [RawContact] with all of the latest data.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [RawContact] no longer exists.
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
    Query(context)
        .where(Fields.RawContactId equalTo id)
        .findFirst(cancel)
        ?.rawContacts
        ?.firstOrNull()

/**
 * This will return [this] same instance if it does not have a valid ID, which means it is a raw
 * contact that does not yet exist in the DB.
 *
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
): MutableRawContact? {
    if (!hasValidId()) {
        return this
    }

    return Query(context)
        .where(Fields.RawContactId equalTo id)
        .findFirst(cancel)
        ?.rawContacts
        ?.firstOrNull()
        ?.toMutableRawContact()
}