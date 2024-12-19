package contacts.test.entities

import contacts.core.entities.MimeType

internal data object TestDataMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.package/name"
    override val value: String = "vnd.contacts.test.entities/data"
}