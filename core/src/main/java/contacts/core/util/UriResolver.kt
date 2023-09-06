package contacts.core.util

import android.net.Uri
import android.provider.ContactsContract
import contacts.core.Contacts
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table

// This should only be used for ContactsContract URIs!
internal fun Uri.forSyncAdapter(callerIsSyncAdapter: Boolean): Uri = if (callerIsSyncAdapter) {
    buildUpon()
        .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "1")
        .build()
} else {
    this // Reduce risk of regression by only setting CALLER_IS_SYNCADAPTER parameter when true.
}

internal fun contactsUri(callerIsSyncAdapter: Boolean, isProfile: Boolean): Uri = if (isProfile) {
    ProfileUris.CONTACTS.uri(callerIsSyncAdapter)
} else {
    Table.Contacts.uri(callerIsSyncAdapter)
}

internal fun rawContactsUri(callerIsSyncAdapter: Boolean, isProfile: Boolean): Uri =
    if (isProfile) {
        ProfileUris.RAW_CONTACTS.uri(callerIsSyncAdapter)
    } else {
        Table.RawContacts.uri(callerIsSyncAdapter)
    }

internal fun dataUri(callerIsSyncAdapter: Boolean, isProfile: Boolean): Uri = if (isProfile) {
    ProfileUris.DATA.uri(callerIsSyncAdapter)
} else {
    Table.Data.uri(callerIsSyncAdapter)
}

internal fun Contacts.contactsUri(isProfile: Boolean): Uri = contactsUri(
    callerIsSyncAdapter = callerIsSyncAdapter, isProfile = isProfile
)

internal fun Contacts.rawContactsUri(isProfile: Boolean): Uri = rawContactsUri(
    callerIsSyncAdapter = callerIsSyncAdapter, isProfile = isProfile
)

internal fun Contacts.dataUri(isProfile: Boolean): Uri = dataUri(
    callerIsSyncAdapter = callerIsSyncAdapter, isProfile = isProfile
)