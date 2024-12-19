package contacts.entities.custom.googlecontacts

import contacts.core.entities.MimeType

// These mimetype values come from installing the Google Contacts app and logging the Data table
// for the custom data. https://play.google.com/store/apps/details?id=com.google.android.contacts
internal sealed class GoogleContactsMimeType : MimeType.Custom() {

    internal data object FileAs : GoogleContactsMimeType() {
        override val value: String = "vnd.com.google.cursor.item/contact_file_as"
    }

    internal data object UserDefined : GoogleContactsMimeType() {
        override val value: String = "vnd.com.google.cursor.item/contact_user_defined_field"
    }
}