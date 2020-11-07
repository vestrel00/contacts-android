package com.vestrel00.contacts.debug

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

fun Context.logRawContactsTable() {
    if (!hasReadPermission()) {
        log("#### RawContacts table - read contacts permission not granted")
        return
    }

    log("#### RawContacts table")

    logRawContactsTable(ContactsContract.RawContacts.CONTENT_URI)
}

internal fun Context.logRawContactsTable(contentUri: Uri) {
    val cursor = contentResolver.query(
        contentUri,
        arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.RawContacts.CONTACT_ID,
            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.RawContacts.STARRED,
            // Deprecated in API 29 - contains useless value for all Android versions in Play store.
            // ContactsContract.RawContacts.TIMES_CONTACTED,
            // ContactsContract.RawContacts.LAST_TIME_CONTACTED,
            ContactsContract.RawContacts.CUSTOM_RINGTONE,
            ContactsContract.RawContacts.SEND_TO_VOICEMAIL
        ),
        null,
        null,
        null
    )

    cursor ?: return

    while (cursor.moveToNext()) {
        // Use getString instead of getLong, getInt, etc so that the value could be null.
        val id = cursor.getString(0)
        val contactId = cursor.getString(1)
        val displayNamePrimary = cursor.getString(2)
        val displayNameAlt = cursor.getString(3)
        val name = cursor.getString(4)
        val type = cursor.getString(5)

        val starred = cursor.getString(6)
        // val timesContacted = cursor.getString(7)
        // val lastTimeContacted = cursor.getString(8)
        val customRingtone = cursor.getString(7)
        val sendToVoicemail = cursor.getString(8)

        log(
            """
                RawContact id: $id, contactId: $contactId, displayNamePrimary: $displayNamePrimary,
                 displayNameAlt: $displayNameAlt,  accountName: $name, accountType: $type,
                 starred: $starred, customRingtone: $customRingtone,
                 sendToVoicemail: $sendToVoicemail
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}