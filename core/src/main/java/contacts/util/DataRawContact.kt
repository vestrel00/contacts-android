package contacts.util

import android.content.Context
import contacts.entities.CommonDataEntity
import contacts.entities.RawContact

/**
 * Returns the [RawContact] with the [CommonDataEntity.rawContactId].
 *
 * This may return null if the [RawContact] no longer exists or if [CommonDataEntity.rawContactId] is null
 * (which is the case for manually constructed entities).
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## Permissions
 *
 * The [contacts.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be returned
 * if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun CommonDataEntity.rawContact(context: Context, cancel: () -> Boolean = { false }): RawContact? =
    rawContactId?.let { rawContactId ->
        context.findFirstRawContactWithId(rawContactId, cancel)
    }