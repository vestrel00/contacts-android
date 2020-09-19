package com.vestrel00.contacts.entities.table

import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Profile

internal object ProfileUris {

    val CONTACTS: Uri = Profile.CONTENT_URI

    val RAW_CONTACTS: Uri = Profile.CONTENT_RAW_CONTACTS_URI

    fun rawContact(rawContactId: Long): Uri = RAW_CONTACTS.buildUpon()
        .appendEncodedPath("$rawContactId")
        .build()

    fun dataForRawContact(rawContactId: Long): Uri = RAW_CONTACTS.buildUpon()
        .appendEncodedPath("$rawContactId")
        .appendEncodedPath(ContactsContract.RawContacts.Data.CONTENT_DIRECTORY)
        .build()
}