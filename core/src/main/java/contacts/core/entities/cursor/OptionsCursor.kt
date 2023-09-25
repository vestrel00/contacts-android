package contacts.core.entities.cursor

import android.database.Cursor
import android.net.Uri
import contacts.core.*
import contacts.core.entities.Entity

/**
 * Retrieves Contact or RawContact options data from the given [cursor].
 */
internal sealed class OptionsCursor<T : Field>(cursor: Cursor, includeFields: Set<T>?) :
    AbstractEntityCursor<T>(cursor, includeFields) {

    abstract val id: Long

    abstract val starred: Boolean?

    /* Deprecated in API 29 - contains useless value for all Android versions from the Play store.
    abstract val timesContacted: Int?

    abstract val lastTimeContacted: Date?
     */

    abstract val customRingtone: Uri?

    abstract val sendToVoicemail: Boolean?
}

// Yes, code duplication for type safety. Hurray! Nice! Chill out...take in the sarcasm!

/**
 * The [OptionsCursor] for the Data table. Options data are specific to Contacts.
 */
internal class DataContactsOptionsCursor(cursor: Cursor, includeFields: Set<AbstractDataField>?) :
    OptionsCursor<AbstractDataField>(cursor, includeFields) {

    override val id: Long by nonNullLong(Fields.Contact.Options.Id, Entity.INVALID_ID)

    override val starred: Boolean? by boolean(Fields.Contact.Options.Starred)

    override val customRingtone: Uri? by uri(Fields.Contact.Options.CustomRingtone)

    override val sendToVoicemail: Boolean? by boolean(Fields.Contact.Options.SendToVoicemail)
}

/**
 * The [OptionsCursor] for the RawContacts table. Options data are specific to RawContacts.
 */
internal class RawContactsOptionsCursor(cursor: Cursor, includeFields: Set<RawContactsField>?) :
    OptionsCursor<RawContactsField>(cursor, includeFields) {

    override val id: Long by nonNullLong(RawContactsFields.Options.Id, Entity.INVALID_ID)

    override val starred: Boolean? by boolean(RawContactsFields.Options.Starred)

    override val customRingtone: Uri? by uri(RawContactsFields.Options.CustomRingtone)

    override val sendToVoicemail: Boolean? by boolean(RawContactsFields.Options.SendToVoicemail)
}

/**
 * The [OptionsCursor] for the Contacts table. Options data are specific to Contacts.
 */
internal class ContactsOptionsCursor(cursor: Cursor, includeFields: Set<ContactsField>?) :
    OptionsCursor<ContactsField>(cursor, includeFields) {

    override val id: Long by nonNullLong(ContactsFields.Options.Id, Entity.INVALID_ID)

    override val starred: Boolean? by boolean(ContactsFields.Options.Starred)

    override val customRingtone: Uri? by uri(ContactsFields.Options.CustomRingtone)

    override val sendToVoicemail: Boolean? by boolean(ContactsFields.Options.SendToVoicemail)
}