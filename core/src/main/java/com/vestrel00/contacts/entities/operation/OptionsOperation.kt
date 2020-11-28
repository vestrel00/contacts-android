package com.vestrel00.contacts.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newUpdate
import com.vestrel00.contacts.ContactsFields
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.RawContactsFields
import com.vestrel00.contacts.entities.MutableOptions
import com.vestrel00.contacts.entities.table.ProfileUris
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo
import com.vestrel00.contacts.util.isProfileId

/*
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 */
internal object OptionsOperation {

    fun updateContactOptions(contactId: Long, options: MutableOptions): ContentProviderOperation =
        newUpdate(if (contactId.isProfileId) ProfileUris.CONTACTS.uri else Table.Contacts.uri)
            .withSelection(ContactsFields.Id equalTo contactId)
            .withOptions(options)
            .build()

    fun updateRawContactOptions(
        rawContactId: Long, options: MutableOptions
    ): ContentProviderOperation = newUpdate(
        if (rawContactId.isProfileId) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri
    )
        .withSelection(RawContactsFields.Id equalTo rawContactId)
        .withOptions(options)
        .build()
}

private fun ContentProviderOperation.Builder.withOptions(options: MutableOptions)
        : ContentProviderOperation.Builder =
    withValue(Fields.Contact.Options.Starred, options.starred.toSqlValue())
        /* Deprecated in API 29 - contains useless value for all Android versions in Play store.
        .withValue(Fields.Contact.Options.TimesContacted, options.timesContacted ?: 0)
        .withValue(Fields.Contact.Options.LastTimeContacted, options.lastTimeContacted?.time ?: 0)
         */
        .withValue(Fields.Contact.Options.CustomRingtone, options.customRingtone.toString())
        .withValue(Fields.Contact.Options.SendToVoicemail, options.sendToVoicemail.toSqlValue())

private fun Boolean?.toSqlValue(): Int {
    if (this != null) {
        return if (this) 1 else 0
    }
    return 0
}