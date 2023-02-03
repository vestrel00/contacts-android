package contacts.core.entities.operation

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newDelete
import android.content.ContentProviderOperation.newInsert
import android.net.Uri
import contacts.core.RawContactsField
import contacts.core.RawContactsFields
import contacts.core.Where
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table

/**
 * Builds [ContentProviderOperation]s for [Table.RawContacts] and [ProfileUris.RAW_CONTACTS].
 */
internal class RawContactsOperation(private val isProfile: Boolean) {

    private val contentUri: Uri
        get() = if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri

    fun insert(rawContactAccount: Account?): ContentProviderOperation = newInsert(contentUri)
        /*
         * Passing in null account name and type is valid. It is the same behavior as the AOSP
         * Contacts app when creating contacts when there are no available accounts. When an account
         * becomes available (or is already available), Android will automatically update the
         * RawContact name and type to an existing Account.
         */
        .withValue(RawContactsFields.AccountName, rawContactAccount?.name)
        .withValue(RawContactsFields.AccountType, rawContactAccount?.type)
        .build()

    fun deleteRawContactsWhere(where: Where<RawContactsField>): ContentProviderOperation =
        newDelete(contentUri).withSelection(where).build()
}