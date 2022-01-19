package contacts.entities.custom.gender

import contacts.core.entities.MimeType

internal object GenderMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.android.cursor.item/<package>.<mimetype>"
    override val value: String = "vnd.android.cursor.item/contacts.entities.custom.gender"
}