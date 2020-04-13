package com.vestrel00.contacts.entities.table

import android.net.Uri
import android.provider.ContactsContract

/**
 * Defines all of the main [ContactsContract] tables.
 */
internal enum class Table(val uri: Uri) {

    /**
     * See [ContactsContract.Contacts].
     */
    CONTACTS(ContactsContract.Contacts.CONTENT_URI),

    /**
     * See [ContactsContract.RawContacts].
     */
    RAW_CONTACTS(ContactsContract.RawContacts.CONTENT_URI),

    /**
     * See [ContactsContract.Data].
     */
    DATA(ContactsContract.Data.CONTENT_URI),

    /**
     * See [ContactsContract.Groups].
     */
    GROUPS(ContactsContract.Groups.CONTENT_URI),

    /**
     * See [ContactsContract.AggregationExceptions].
     */
    AGGREGATION_EXCEPTIONS(ContactsContract.AggregationExceptions.CONTENT_URI)
}