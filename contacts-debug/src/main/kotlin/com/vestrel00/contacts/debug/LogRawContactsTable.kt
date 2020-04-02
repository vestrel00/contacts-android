package com.vestrel00.contacts.debug

import android.content.Context
import android.provider.ContactsContract

fun Context.logRawContactsTable() {
    if (!hasReadPermission()) {
        log("#### RawContacts table - read contacts permission not granted")
        return
    }

    val cursor = contentResolver.query(
        ContactsContract.RawContacts.CONTENT_URI,
        arrayOf(
            ContactsContract.RawContacts._ID,
            ContactsContract.RawContacts.CONTACT_ID,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.RawContacts.STARRED,
            ContactsContract.RawContacts.TIMES_CONTACTED,
            ContactsContract.RawContacts.LAST_TIME_CONTACTED,
            ContactsContract.RawContacts.CUSTOM_RINGTONE,
            ContactsContract.RawContacts.SEND_TO_VOICEMAIL
        ),
        null,
        null,
        null
    )

    cursor ?: return

    log("#### RawContacts table")
    cursor.moveToPosition(-1)
    while (cursor.moveToNext()) {
        val id = cursor.getString(0)
        val contactId = cursor.getString(1)
        val name = cursor.getString(2)
        val type = cursor.getString(3)

        val starred = cursor.getString(4)
        val timesContacted = cursor.getString(5)
        val lastTimeContacted = cursor.getString(6)
        val customRingtone = cursor.getString(7)
        val sendToVoicemail = cursor.getString(8)

        log(
            """
                RawContact id: $id, contactId: $contactId, accountName: $name, accountType: $type,
                 starred: $starred, timesContacted: $timesContacted, 
                 lastTimeContacted: $lastTimeContacted, customRingtone: $customRingtone, 
                 sendToVoicemail: $sendToVoicemail
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}