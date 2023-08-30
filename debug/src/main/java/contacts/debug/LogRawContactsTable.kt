package contacts.debug

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
            ContactsContract.RawContacts.SOURCE_ID,
            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.RawContacts.STARRED,
            // Deprecated in API 29 - contains useless value for all Android versions in Play store.
            // ContactsContract.RawContacts.TIMES_CONTACTED,
            // ContactsContract.RawContacts.LAST_TIME_CONTACTED,
            ContactsContract.RawContacts.CUSTOM_RINGTONE,
            ContactsContract.RawContacts.SEND_TO_VOICEMAIL,
            ContactsContract.RawContacts.DELETED
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
        val sourceId = cursor.getString(2)
        val displayNamePrimary = cursor.getString(3)
        val displayNameAlt = cursor.getString(4)
        val name = cursor.getString(5)
        val type = cursor.getString(6)

        val starred = cursor.getString(7)
        val customRingtone = cursor.getString(8)
        val sendToVoicemail = cursor.getString(9)
        val deleted = cursor.getString(10)

        log(
            """
                RawContact id: $id, contactId: $contactId, sourceId:$sourceId,
                 displayNamePrimary: $displayNamePrimary, displayNameAlt: $displayNameAlt,
                 accountName: $name, accountType: $type,
                 starred: $starred, customRingtone: $customRingtone,
                 sendToVoicemail: $sendToVoicemail, deleted: $deleted
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}