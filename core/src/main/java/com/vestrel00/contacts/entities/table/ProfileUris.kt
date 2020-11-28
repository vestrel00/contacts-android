package com.vestrel00.contacts.entities.table

import android.net.Uri
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Profile

/**
 * Provides Uris to a special region of the Contacts, RawContacts, and Data tables where the _id of
 * each row in the respective tables returns true for ContactsContract.isProfileId.
 */
internal enum class ProfileUris(val uri: Uri) {

    /**
     * Uri to the single Profile Contact row in the Contacts table.
     */
    CONTACTS(Profile.CONTENT_URI),

    /**
     * Uri to the Profile RawContacts rows in the RawContacts table.
     */
    RAW_CONTACTS(Profile.CONTENT_RAW_CONTACTS_URI),

    /**
     * Uri to the Profile Data rows in the Data table.
     */
    DATA(Uri.withAppendedPath(Profile.CONTENT_URI, Contacts.Data.CONTENT_DIRECTORY))

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
    .appendEncodedPath(RawContacts.Data.CONTENT_DIRECTORY)
    .build()
 */