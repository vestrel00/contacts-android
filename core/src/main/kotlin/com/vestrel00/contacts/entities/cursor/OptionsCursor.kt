package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import android.net.Uri
import com.vestrel00.contacts.Fields

/**
 * Retrieves Contact options data from the given [cursor].
 *
 * Even though this uses [Fields.Contact] from Data table queries, this may also be used for
 * Contacts and RawContacts tables because the underlying column names are the same.
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class OptionsCursor(private val cursor: Cursor) {

    val id: Long?
        get() = cursor.getLong(Fields.Contact.Options.Id)

    val starred: Boolean?
        get() = cursor.getBoolean(Fields.Contact.Options.Starred)

    /* Deprecated in API 29 - contains useless value for all Android versions from the Play store.
    val timesContacted: Int?
        get() = cursor.getInt(Fields.Contact.Options.TimesContacted)

    val lastTimeContacted: Date?
        get() = cursor.getDate(Fields.Contact.Options.LastTimeContacted)
     */

    val customRingtone: Uri?
        get() = cursor.getUri(Fields.Contact.Options.CustomRingtone)

    val sendToVoicemail: Boolean?
        get() = cursor.getBoolean(Fields.Contact.Options.SendToVoicemail)
}
