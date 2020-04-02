package com.vestrel00.contacts.debug

import android.content.Context
import android.provider.ContactsContract

fun Context.logContactsTable() {
    if (!hasReadPermission()) {
        log("#### Contacts table - read contacts permission not granted")
        return
    }

    val cursor = contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.STARRED,
            ContactsContract.Contacts.TIMES_CONTACTED,
            ContactsContract.Contacts.LAST_TIME_CONTACTED,
            ContactsContract.Contacts.CUSTOM_RINGTONE,
            ContactsContract.Contacts.SEND_TO_VOICEMAIL,
            ContactsContract.Contacts.PHOTO_FILE_ID,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        ),
        null,
        null,
        null
    )

    cursor ?: return

    log("#### Contacts table")
    cursor.moveToPosition(-1)
    while (cursor.moveToNext()) {
        val id = cursor.getString(0)
        val displayName = cursor.getString(1)

        val starred = cursor.getString(2)
        val timesContacted = cursor.getString(3)
        val lastTimeContacted = cursor.getString(4)
        val customRingtone = cursor.getString(5)
        val sendToVoicemail = cursor.getString(6)
        val photoFileId = cursor.getString(7)
        val photoUri = cursor.getString(8)
        val photoThumbnailUri = cursor.getString(9)

        log(
            """
                Contact id: $id, displayName: $displayName, starred: $starred,
                 timesContacted: $timesContacted, lastTimeContacted: $lastTimeContacted,
                 customRingtone: $customRingtone, sendToVoicemail: $sendToVoicemail,
                 photoFileId: $photoFileId, photoUri: $photoUri,
                 photoThumbnailUri: $photoThumbnailUri
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}