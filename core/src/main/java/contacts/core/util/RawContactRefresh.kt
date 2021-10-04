package contacts.core.util

import android.content.Context
import contacts.core.Fields
import contacts.core.Query
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.custom.GlobalCustomDataRegistry
import contacts.core.equalTo
import contacts.core.profile.ProfileQuery

/**
 * Returns the [RawContact] with all of the latest data.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [RawContact] no longer exists or if permission is not granted.
 *
 * Returns itself if the [RawContact.id] is null, indicating that this RawContact instance has not
 * yet been inserted to the DB.
 *
 * Supports profile and non-profile RawContacts with native/custom data.
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
fun RawContact.refresh(
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    cancel: () -> Boolean = { false }
): RawContact? =
    if (id == null) {
        this
    } else {
        context.findFirstRawContactWithId(id, customDataRegistry, cancel)
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
    context: Context,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    cancel: () -> Boolean = { false }
): MutableRawContact? = if (id == null) {
    this
} else {
    context.findFirstRawContactWithId(id, customDataRegistry, cancel)?.toMutableRawContact()
}

internal fun Context.findFirstRawContactWithId(
    rawContactId: Long,
    customDataRegistry: CustomDataRegistry,
    cancel: () -> Boolean
): RawContact? = if (rawContactId.isProfileId) {
    ProfileQuery(this, customDataRegistry)
        .find(cancel)
        ?.rawContacts
        ?.find { it.id == rawContactId }
} else {
    Query(this, customDataRegistry)
        .where(Fields.RawContact.Id equalTo rawContactId)
        .find(cancel)
        .firstOrNull()
        ?.rawContacts
        ?.firstOrNull()
}