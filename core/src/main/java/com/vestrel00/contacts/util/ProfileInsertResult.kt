package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.profile.ProfileInsert
import com.vestrel00.contacts.profile.ProfileQuery

/**
 * Returns the newly created [RawContact] or null if the insert operation failed.
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
fun ProfileInsert.Result.rawContact(context: Context, cancel: () -> Boolean = { false }):
        RawContact? = rawContactId?.let { rawContactId ->
    contact(context, cancel)
        ?.rawContacts
        ?.asSequence()
        ?.firstOrNull { it.id == rawContactId }
}

/**
 * Returns the Profile [Contact] containing the newly created [RawContact] or null if the insert
 * operation failed.
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
fun ProfileInsert.Result.contact(context: Context, cancel: () -> Boolean = { false }): Contact? =
    if (isSuccessful) ProfileQuery(context).find(cancel) else null