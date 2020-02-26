package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Query
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.equalTo

/**
 * Returns the [Contact] that contains all of the successfully linked RawContacts or null if the
 * link operation failed.
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
fun ContactLinkResult.contact(context: Context, cancel: () -> Boolean = { false }): Contact? {

    val contactId = this.contactId ?: return null

    return Query(context).where(Fields.Contact.Id equalTo contactId).findFirst(cancel)
}