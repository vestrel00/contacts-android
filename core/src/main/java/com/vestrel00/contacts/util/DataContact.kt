package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.CommonDataEntity

/**
 * Returns the [Contact] with the [CommonDataEntity.contactId].
 *
 * This may return null if the [Contact] no longer exists or if [CommonDataEntity.contactId] is null
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
fun CommonDataEntity.contact(context: Context, cancel: () -> Boolean = { false }): Contact? =
    contactId?.let { contactId ->
        context.findFirstContactWithId(contactId, cancel)
    }