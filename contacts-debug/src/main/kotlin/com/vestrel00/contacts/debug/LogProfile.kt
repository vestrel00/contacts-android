package com.vestrel00.contacts.debug

import android.content.Context
import android.provider.ContactsContract

fun Context.logProfile() {
    if (!hasReadPermission()) {
        log("#### Profile - read contacts permission not granted")
        return
    }

    log("#### Profile")

    logContactsTable(ContactsContract.Profile.CONTENT_URI)
    logRawContactsTable(ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI)
    profileRawContactIds().forEach(::logDataTableFor)
}

private fun Context.logDataTableFor(profileRawContactId: String) {
    logDataTable(
        ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI.buildUpon()
            .appendEncodedPath(profileRawContactId)
            .appendEncodedPath(ContactsContract.RawContacts.Data.CONTENT_DIRECTORY)
            .build()
    )
}

private fun Context.profileRawContactIds(): Set<String> {
    val cursor = contentResolver.query(
        ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI,
        arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.RawContacts.CONTACT_ID
        ),
        null,
        null,
        null
    )

    cursor ?: return emptySet()

    return mutableSetOf<String>().apply {
        while (cursor.moveToNext()) {
            // Use getString instead of getLong so that the return could be null.
            val contactId = cursor.getString(1)

            if (contactId != null) {
                val rawContactId = cursor.getString(0)
                rawContactId?.let(::add)
            }
        }

        cursor.close()
    }
}