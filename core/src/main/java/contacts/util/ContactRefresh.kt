package contacts.util

import android.content.Context
import contacts.Fields
import contacts.Query
import contacts.entities.Contact
import contacts.entities.MutableContact
import contacts.equalTo
import contacts.profile.ProfileQuery

/**
 * Returns the contact with all of the latest data, including all
 * [contacts.entities.RawContact]s.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [Contact] no longer exists or if permission is not granted.
 *
 * Returns itself if the [Contact.id] is null, indicating that this Contact instance has not yet
 * been inserted to the DB.
 *
 * Supports profile and non-profile Contacts.
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
fun Contact.refresh(context: Context, cancel: () -> Boolean = { false }): Contact? =
    if (id == null) {
        this
    } else {
        context.findFirstContactWithId(id, cancel)
    }

/**
 * See [Contact.refresh].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun MutableContact.refresh(context: Context, cancel: () -> Boolean = { false }): MutableContact? =
    if (id == null) {
        this
    } else {
        context.findFirstContactWithId(id, cancel)?.toMutableContact()
    }

internal fun Context.findFirstContactWithId(contactId: Long, cancel: () -> Boolean): Contact? =
    if (contactId.isProfileId) {
        ProfileQuery(this)
            .find(cancel)
    } else {
        Query(this)
            .where(Fields.Contact.Id equalTo contactId)
            .find(cancel)
            .firstOrNull()
    }