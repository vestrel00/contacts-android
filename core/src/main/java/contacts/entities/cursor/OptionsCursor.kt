package contacts.entities.cursor

import android.database.Cursor
import android.net.Uri
import contacts.ContactsFields

/**
 * Retrieves Contact options data from the given [cursor].
 *
 * Even though this uses [ContactsFields] from Contacts table queries, this may also be used for
 * RawContacts and Data table queries because the underlying column names are the same.
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class OptionsCursor(private val cursor: Cursor) {

    val id: Long?
        get() = cursor.getLong(ContactsFields.Options.Id)

    val starred: Boolean?
        get() = cursor.getBoolean(ContactsFields.Options.Starred)

    /* Deprecated in API 29 - contains useless value for all Android versions from the Play store.
    val timesContacted: Int?
        get() = cursor.getInt(ContactsFields.Options.TimesContacted)

    val lastTimeContacted: Date?
        get() = cursor.getDate(ContactsFields.Options.LastTimeContacted)
     */

    val customRingtone: Uri?
        get() = cursor.getUri(ContactsFields.Options.CustomRingtone)

    val sendToVoicemail: Boolean?
        get() = cursor.getBoolean(ContactsFields.Options.SendToVoicemail)
}
