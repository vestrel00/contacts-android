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
    ): ContentProviderOperation? =
        /*
         * There must at least be one key-value pair (even if the value is null) in the operation.
         * Otherwise, an exception is thrown...
         *
         * java.lang.NullPointerException: Attempt to invoke virtual method
         *     'java.util.Set android.content.ContentValues.keySet()' on a null object reference
         */
        if (includeFields != null && includeFields.intersect(POSSIBLE_INCLUDE_FIELDS_FOR_INSERT).isEmpty()) {
            null
        } else {
            newInsert(contentUri)
                /*
                 * Passing in null account name and type is valid. It is the same behavior as the AOSP
                 * Contacts app when creating contacts when there are no available accounts. When an account
                 * becomes available (or is already available), Android will automatically update the
                 * RawContact name and type to an existing Account.
                 */
                .withIncludedValue(includeFields, RawContactsFields.AccountName, rawContactAccount?.name)
                .withIncludedValue(includeFields, RawContactsFields.AccountType, rawContactAccount?.type)
                .withIncludedValue(includeFields, RawContactsFields.SourceId, sourceId)
                .build()
        }

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

private val POSSIBLE_INCLUDE_FIELDS_FOR_INSERT = buildSet {
    add(RawContactsFields.AccountName)
    add(RawContactsFields.AccountType)
    add(RawContactsFields.SourceId)
}