package contacts.core.entities.operation

import android.content.ContentProviderOperation
import contacts.core.SimContactsFields
import contacts.core.`in`
import contacts.core.entities.NewSimContact
import contacts.core.entities.table.Table
import contacts.core.equalTo

private val TABLE = Table.SimContacts

/**
 * Builds [ContentProviderOperation]s for [Table.SimContacts].
 */
// TODO Delete this if write operations are not supported!
internal class SimContactsOperation {

    fun insert(simContact: NewSimContact): ContentProviderOperation? =
        if (simContact.isBlank) {
            null
        } else {
            newInsert(TABLE)
                .withValue(SimContactsFields.Name, simContact.name)
                .withValue(SimContactsFields.Number, simContact.number)
                .withValue(SimContactsFields.Emails, simContact.emails)
                .build()
        }

    fun delete(simContactId: Long): ContentProviderOperation = newDelete(TABLE)
        .withSelection(SimContactsFields.Id equalTo simContactId)
        .build()

    fun delete(simContactIds: Collection<Long>): ContentProviderOperation = newDelete(TABLE)
        .withSelection(SimContactsFields.Id `in` simContactIds)
        .build()
}