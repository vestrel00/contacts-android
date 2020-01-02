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

fun Context.logDataTable() {
    if (!canLog()) {
        log("#### Data table - read contacts permission not granted")
        return
    }

    val cursor = contentResolver.query(
        Data.CONTENT_URI,
        arrayOf(
            Data._ID, Data.RAW_CONTACT_ID, Data.CONTACT_ID, Data.MIMETYPE,
            Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA13,
            Data.DATA7, Data.DATA8, Data.DATA9, Data.DATA10, Data.DATA11, Data.DATA12, Data.DATA14
        ),
        null,
        null,
        null
    )

    cursor ?: return

    log("#### Data table")
    cursor.moveToPosition(-1)
    while (cursor.moveToNext()) {
        val id = cursor.getString(0)
        val rawContactId = cursor.getString(1)
        val contactId = cursor.getString(2)
        val mimeType = cursor.getString(3)
        val data1 = cursor.getString(4)
        val data2 = cursor.getString(5)
        val data3 = cursor.getString(6)
        val data4 = cursor.getString(7)
        val data5 = cursor.getString(8)
        val data6 = cursor.getString(9)
        val data7 = cursor.getString(10)
        val data8 = cursor.getString(11)
        val data9 = cursor.getString(12)
        val data10 = cursor.getString(13)
        val data11 = cursor.getString(14)
        val data12 = cursor.getString(15)
        val data13 = cursor.getString(16)
        val data14 = cursor.getString(17)

        log(
            """
                Data id: $id, rawContactId: $rawContactId, contactId: $contactId,
                 mimeType: $mimeType,
                 data1: $data1, data2: $data2, data3: $data3, data4: $data4, data5: $data5,
                 data6: $data6, data7: $data7, data8: $data8, data9: $data9, data10: $data10,
                 data11: $data11, data12: $data12, data13: $data13, data14: $data14
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}

// Intentionally not reusing the ContactsPermission to avoid a dependency on the contacts module.
private fun Context.canLog(): Boolean = checkPermission(
    Manifest.permission.READ_CONTACTS, Process.myPid(), Process.myUid()
) == PackageManager.PERMISSION_GRANTED