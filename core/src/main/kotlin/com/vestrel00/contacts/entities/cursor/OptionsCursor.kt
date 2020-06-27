package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import android.net.Uri
import com.vestrel00.contacts.Fields
import java.util.*

/**
 * Retrieves [Fields.Options] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class OptionsCursor(private val cursor: Cursor) {

    val id: Long?
        get() = cursor.getLong(Fields.Options.Id)

    val starred: Boolean?
        get() = cursor.getBoolean(Fields.Options.Starred)

    val timesContacted: Int?
        get() = cursor.getInt(Fields.Options.TimesContacted)

    val lastTimeContacted: Date?
        get() = cursor.getDate(Fields.Options.LastTimeContacted)

    val customRingtone: Uri?
        get() = cursor.getUri(Fields.Options.CustomRingtone)

    val sendToVoicemail: Boolean?
        get() = cursor.getBoolean(Fields.Options.SendToVoicemail)
}
