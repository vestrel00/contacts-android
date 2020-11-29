package contacts.debug

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

fun Context.logProfile() {
    if (!hasReadPermission()) {
        log("#### Profile - read contacts permission not granted")
        return
    }

    log("#### Profile")

    logContactsTable(ContactsContract.Profile.CONTENT_URI)
    logRawContactsTable(ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI)
    logDataTable(
        Uri.withAppendedPath(
            ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY
        )
    )
}