package com.vestrel00.contacts.entities.operation

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newDelete
import android.content.ContentProviderOperation.newInsert
import android.net.Uri
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo

/**
 * Builds [ContentProviderOperation]s for [Table.RAW_CONTACTS].
 */
internal class RawContactOperation(private val contentUri: Uri) {

    fun insert(rawContactAccount: Account?): ContentProviderOperation = newInsert(contentUri)
        /*
         * Passing in null account name and type is valid. It is the same behavior as the native
         * Android Contacts app when creating contacts when there are no available accounts. When an
         * account becomes available (or is already available), Android will automatically update
         * the RawContact name and type to an existing Account.
         */
        .withValue(Fields.RawContacts.AccountName, rawContactAccount?.name)
        .withValue(Fields.RawContacts.AccountType, rawContactAccount?.type)
        .build()

    fun deleteRawContact(rawContactId: Long): ContentProviderOperation = newDelete(contentUri)
        .withSelection("${Fields.RawContacts.Id equalTo rawContactId}", null)
        .build()

    /*
     * Deleting all of the RawContact rows matching the Contacts._ID will result in the automatic
     * deletion of the Contacts row and associated Data rows.
     */
    fun deleteRawContactsWithContactId(contactId: Long): ContentProviderOperation =
        newDelete(contentUri)
            .withSelection("${Fields.RawContacts.ContactId equalTo contactId}", null)
            .build()
}