package contacts.core.entities.table

import android.net.Uri
import android.provider.ContactsContract
import contacts.core.util.forSyncAdapter

/**
 * Provides Uris to a special region of the Contacts, RawContacts, and Data tables where the _id of
 * each row in the respective tables returns true for [ContactsContract.isProfileId].
 */
internal enum class ProfileUris(private val uri: Uri) {

    /**
     * Uri to the single Profile Contact row in the Contacts table.
     */
    CONTACTS(ContactsContract.Profile.CONTENT_URI),

    /**
     * Uri to the Profile RawContacts rows in the RawContacts table.
     */
    RAW_CONTACTS(ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI),

    /**
     * Uri to the Profile Data rows in the Data table.
     */
    DATA(
        Uri.withAppendedPath(
            ContactsContract.Profile.CONTENT_URI,
            ContactsContract.Contacts.Data.CONTENT_DIRECTORY
        )
    );

    // Expose the private uri. We could just remove the protected modifier from uri but making
    // the refactors for https://github.com/vestrel00/contacts-android/issues/308 would have been
    // more difficult do without missing anything.
    fun uri(): Uri = uri

    fun uri(callerIsSyncAdapter: Boolean): Uri = uri.forSyncAdapter(callerIsSyncAdapter)
}

/* Not needed for now.
/**
 * Uri to the Profile RawContact row in the RawContacts table for the given [rawContactId].
 */
fun rawContact(rawContactId: Long): Uri = RAW_CONTACTS.uri.buildUpon()
    .appendEncodedPath("$rawContactId")
    .build()

/**
 * Uri to the Profile Data rows in the Data table for the given [rawContactId].
 *
 * **Important** This does not support deletion of data rows! It will throw an exception.
 */
fun dataForRawContact(rawContactId: Long): Uri = RAW_CONTACTS.uri.buildUpon()
    .appendEncodedPath("$rawContactId")
    .appendEncodedPath(ContactsContract.RawContacts.Data.CONTENT_DIRECTORY)
    .build()
 */