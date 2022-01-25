package contacts.debug

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
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,
            ContactsContract.Contacts.STARRED,
            // Deprecated in API 29 - contains useless value for all Android versions in Play store.
            // ContactsContract.Contacts.TIMES_CONTACTED,
            // ContactsContract.Contacts.LAST_TIME_CONTACTED,
            ContactsContract.Contacts.CUSTOM_RINGTONE,
            ContactsContract.Contacts.SEND_TO_VOICEMAIL,
            ContactsContract.Contacts.PHOTO_FILE_ID,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        ),
        null,
        null,
        null
    )

    cursor ?: return

    while (cursor.moveToNext()) {
        // Use getString instead of getLong, getInt, etc so that the value could be null.
        val id = cursor.getString(0)
        val lookupKey = cursor.getString(1)
        val displayNamePrimary = cursor.getString(2)
        val displayNameAlt = cursor.getString(3)

        val starred = cursor.getString(4)
        val customRingtone = cursor.getString(5)
        val sendToVoicemail = cursor.getString(6)
        val photoFileId = cursor.getString(7)
        val photoUri = cursor.getString(8)
        val photoThumbnailUri = cursor.getString(9)
        val hasPhoneNumber = cursor.getString(10)

        log(
            """
                Contact id: $id, lookupKey: $lookupKey,
                 displayNamePrimary: $displayNamePrimary, displayNameAlt: $displayNameAlt, 
                 starred: $starred, customRingtone: $customRingtone, sendToVoicemail: $sendToVoicemail,
                 photoFileId: $photoFileId, photoUri: $photoUri, photoThumbnailUri: $photoThumbnailUri,
                 hasPhoneNumber: $hasPhoneNumber
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}