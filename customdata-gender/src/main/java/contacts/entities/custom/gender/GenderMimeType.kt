package contacts.entities.custom.gender

import contacts.core.entities.MimeType

internal object GenderMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.package.cursor.item/mimetype"
    override val value: String = "vnd.contacts.entities.custom.cursor.item/gender"
}