package contacts.entities.custom.gender

import contacts.entities.MimeType

internal object GenderMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.package/name"
    override val value: String = "vnd.contacts.entities.custom/gender"
}