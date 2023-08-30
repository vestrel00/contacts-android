package contacts.core.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newUpdate
import contacts.core.*
import contacts.core.entities.OptionsEntity
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.util.isProfileId

/*
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 */
internal class OptionsOperation {

    fun updateContactOptions(
        contactId: Long, options: OptionsEntity?, includeFields: Set<DataContactsField>
    ): ContentProviderOperation? = if (includeFields.isEmpty()) {
        null
    } else {
        newUpdate(if (contactId.isProfileId) ProfileUris.CONTACTS.uri else Table.Contacts.uri)
            .withSelection(ContactsFields.Id equalTo contactId)
            .withIncludedValue(
                includeFields,
                Fields.Contact.Options.Starred,
                options?.starred
            ) {
                it.toSqlValue()
            }
            .withIncludedValue(
                includeFields, Fields.Contact.Options.CustomRingtone, options?.customRingtone
            ) {
                it.toString()
            }
            .withIncludedValue(
                includeFields, Fields.Contact.Options.SendToVoicemail, options?.sendToVoicemail
            ) {
                it.toSqlValue()
            }
            .build()
    }

    fun updateRawContactOptions(
        options: OptionsEntity?, rawContactId: Long,  includeFields: Set<RawContactsField>
    ): ContentProviderOperation? = if (includeFields.isEmpty()) {
        null
    } else {
        newUpdate(
            if (rawContactId.isProfileId) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri
        )
            .withSelection(RawContactsFields.Id equalTo rawContactId)
            .withRawContactOptions(options, includeFields)
            .build()
    }

    fun updateNewRawContactOptions(
        options: OptionsEntity?,
        includeFields: Set<RawContactsField>,
        isProfile: Boolean
    ): ContentProviderOperation? = if (includeFields.isEmpty()) {
        null
    } else {
        newUpdate(
            if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri
        )
            // The actual value in arrayOf does not matter.
            // It will be replaced by withSelectionBackReference.
            .withSelection("${RawContactsFields.Id.columnName}=?", arrayOf("-1"))
            // Set the value of the ? (index 0) to the first result (fromIndex 0) of the batch
            // operation, which is assumed to be a new raw contact.
            .withSelectionBackReference(0, 0)
            .withRawContactOptions(options, includeFields)
            .build()
    }
}

private fun ContentProviderOperation.Builder.withRawContactOptions(
    options: OptionsEntity?,
    includeFields: Set<RawContactsField>
): ContentProviderOperation.Builder = this // this not needed but makes formatting look nice
    .withIncludedValue(
        includeFields,
        RawContactsFields.Options.Starred,
        options?.starred
    ) {
        it.toSqlValue()
    }
    .withIncludedValue(
        includeFields, RawContactsFields.Options.CustomRingtone, options?.customRingtone
    ) {
        it.toString()
    }
    .withIncludedValue(
        includeFields, RawContactsFields.Options.SendToVoicemail, options?.sendToVoicemail
    ) {
        it.toSqlValue()
    }

private fun Boolean?.toSqlValue(): Int {
    if (this != null) {
        return if (this) 1 else 0
    }
    return 0
}