package contacts.entities.cursor

import android.database.Cursor
import android.net.Uri
import contacts.ContactsField
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
internal class OptionsCursor(cursor: Cursor) : AbstractEntityCursor<ContactsField>(cursor) {

    val id: Long? by long(ContactsFields.Options.Id)

    val starred: Boolean? by boolean(ContactsFields.Options.Starred)

    /* Deprecated in API 29 - contains useless value for all Android versions from the Play store.
    val timesContacted: Int? by int(ContactsFields.Options.TimesContacted)

    val lastTimeContacted: Date? by date(ContactsFields.Options.LastTimeContacted)
     */

    val customRingtone: Uri? by uri(ContactsFields.Options.CustomRingtone)

    val sendToVoicemail: Boolean? by boolean(ContactsFields.Options.SendToVoicemail)
}
