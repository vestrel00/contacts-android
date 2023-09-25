package contacts.core.entities.operation

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newDelete
import android.content.ContentProviderOperation.newInsert
import android.net.Uri
import contacts.core.RawContactsField
import contacts.core.RawContactsFields
import contacts.core.Where
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.equalTo
import contacts.core.util.rawContactsUri

/**
 * Builds [ContentProviderOperation]s for [Table.RawContacts] and [ProfileUris.RAW_CONTACTS].
 */
internal class RawContactsOperation(
    callerIsSyncAdapter: Boolean, isProfile: Boolean
) {

    private val contentUri: Uri = rawContactsUri(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile
    )

    fun insert(
        rawContactAccount: Account?,
        sourceId: String?,
        includeFields: Set<RawContactsField>?
    ): ContentProviderOperation = newInsert(contentUri)
        /*
         * Passing in null account name and type is valid. It is the same behavior as the AOSP
         * Contacts app when creating contacts when there are no available accounts. When an account
         * becomes available (or is already available), Android will automatically update the
         * RawContact name and type to an existing Account.
         *
         * Also note that a new insert operation should still be created even if none of the
         * following fields are included.
         */
        .withIncludedValue(includeFields, RawContactsFields.AccountName, rawContactAccount?.name)
        .withIncludedValue(includeFields, RawContactsFields.AccountType, rawContactAccount?.type)
        .withIncludedValue(includeFields, RawContactsFields.SourceId, sourceId)
        .build()

    fun update(
        rawContact: ExistingRawContactEntity, includeFields: Set<RawContactsField>?
    ): ContentProviderOperation? = if (
        includeFields != null &&
        !includeFields.contains(RawContactsFields.SourceId)
    ) {
        null
    } else {
        ContentProviderOperation.newUpdate(contentUri)
            .withSelection(RawContactsFields.Id equalTo rawContact.id)
            .withValue(RawContactsFields.SourceId, rawContact.sourceId)
            .build()
    }

    fun deleteRawContactsWhere(where: Where<RawContactsField>): ContentProviderOperation =
        newDelete(contentUri).withSelection(where).build()
}