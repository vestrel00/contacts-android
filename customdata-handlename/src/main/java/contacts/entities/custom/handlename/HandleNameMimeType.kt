package contacts.entities.custom.handlename

import contacts.core.entities.MimeType

internal object HandleNameMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.package.cursor.item/mimetype"
    override val value: String = "vnd.contacts.entities.custom.cursor.item/handlename"
}