package contacts.entities.custom.handlename

import contacts.entities.MimeType

internal object HandleNameMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.package/name"
    override val value: String = "vnd.contacts.entities.custom/handlename"
}