package com.vestrel00.contacts.debug

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

fun Context.logContactsTable() {
    if (!hasReadPermission()) {
        log("#### Contacts table - read contacts permission not granted")
        return
    }

    log("#### Contacts table")

    logContactsTable(ContactsContract.Contacts.CONTENT_URI)
}

internal fun Context.logContactsTable(contentUri: Uri) {
    val cursor = contentResolver.query(
        contentUri,
        arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,
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

    while (cursor.moveToNext()) {
        // Use getString instead of getLong, getInt, etc so that the value could be null.
        val id = cursor.getString(0)
        val displayNamePrimary = cursor.getString(1)
        val displayNameAlt = cursor.getString(2)

        val starred = cursor.getString(3)
        val timesContacted = cursor.getString(4)
        val lastTimeContacted = cursor.getString(5)
        val customRingtone = cursor.getString(6)
        val sendToVoicemail = cursor.getString(7)
        val photoFileId = cursor.getString(8)
        val photoUri = cursor.getString(9)
        val photoThumbnailUri = cursor.getString(10)

        log(
            """
                Contact id: $id, displayNamePrimary: $displayNamePrimary,
                 displayNameAlt: $displayNameAlt, starred: $starred,
                 timesContacted: $timesContacted, lastTimeContacted: $lastTimeContacted,
                 customRingtone: $customRingtone, sendToVoicemail: $sendToVoicemail,
                 photoFileId: $photoFileId, photoUri: $photoUri,
                 photoThumbnailUri: $photoThumbnailUri
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}