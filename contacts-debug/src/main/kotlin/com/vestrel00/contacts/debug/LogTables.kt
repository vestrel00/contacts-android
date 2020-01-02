package com.vestrel00.contacts.debug

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.provider.ContactsContract.*

fun Context.logContactsTable() {
    if (!canLog()) {
        log("#### Contacts table - read contacts permission not granted")
        return
    }

    val cursor = contentResolver.query(
        Contacts.CONTENT_URI,
        arrayOf(
            Contacts._ID, Contacts.DISPLAY_NAME,
            Contacts.STARRED, Contacts.TIMES_CONTACTED, Contacts.LAST_TIME_CONTACTED,
            Contacts.CUSTOM_RINGTONE, Contacts.SEND_TO_VOICEMAIL,
            Contacts.PHOTO_FILE_ID, Contacts.PHOTO_URI, Contacts.PHOTO_THUMBNAIL_URI
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

fun Context.logRawContactsTable() {
    if (!canLog()) {
        log("#### RawContacts table - read contacts permission not granted")
        return
    }

    val cursor = contentResolver.query(
        RawContacts.CONTENT_URI,
        arrayOf(
            RawContacts._ID, RawContacts.CONTACT_ID,
            RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE,
            RawContacts.STARRED, RawContacts.TIMES_CONTACTED, RawContacts.LAST_TIME_CONTACTED,
            RawContacts.CUSTOM_RINGTONE, RawContacts.SEND_TO_VOICEMAIL
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

// Intentionally not reusing the ContactsPermission to avoid a dependency on the contacts module.
private fun Context.canLog(): Boolean = checkPermission(
    Manifest.permission.READ_CONTACTS, Process.myPid(), Process.myUid()
) == PackageManager.PERMISSION_GRANTED