package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Query
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.equalTo

/**
 * Returns the contact with all of the latest data, including all
 * [com.vestrel00.contacts.entities.RawContact]s.
 *
 * This is useful for getting the latest contact data after performing an update. This may return
 * null if the [Contact] no longer exists.
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
fun Contact.refresh(context: Context, cancel: () -> Boolean = { false }): Contact? = Query(context)
    .where(Fields.Contact.Id equalTo id)
    .findFirst(cancel)

/**
 * This will return [this] same instance if it does not have a valid ID, which means it is a contact
 * that does not yet exist in the DB.
 *
 * See [Contact.refresh].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun MutableContact.refresh(context: Context, cancel: () -> Boolean = { false }): MutableContact? {
    if (!hasValidId()) {
        return this
    }

    return Query(context)
        .where(Fields.Contact.Id equalTo id)
        .findFirst(cancel)
        ?.toMutableContact()
}