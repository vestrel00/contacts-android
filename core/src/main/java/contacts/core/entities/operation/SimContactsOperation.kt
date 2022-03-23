package contacts.core.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentValues
import contacts.core.SimContactsFields
import contacts.core.`in`
import contacts.core.entities.NewSimContact
import contacts.core.entities.table.Table
import contacts.core.equalTo

private val TABLE = Table.SimContacts

/**
 * Builds [ContentProviderOperation]s for [Table.SimContacts].
 */
internal class SimContactsOperation {

    fun insert(simContact: NewSimContact): ContentValues? =
        if (simContact.isBlank) {
            null
        } else {
            ContentValues().apply {
                // Populates the name
                put(SimContactsFields.Tag.columnName, simContact.name)
                put(SimContactsFields.Number.columnName, simContact.number)

                // FIXME Inserting email does not work. I already tried string values seen in the
                // IccProvider such as "emails", "newEmails", "anrs", "newAnrs", "newTag"
                put(SimContactsFields.Emails.columnName, simContact.emails)
            }
        }

    fun delete(simContactId: Long): ContentProviderOperation = newDelete(TABLE)
        .withSelection(SimContactsFields.Id equalTo simContactId)
        .build()

    fun delete(simContactIds: Collection<Long>): ContentProviderOperation = newDelete(TABLE)
        .withSelection(SimContactsFields.Id `in` simContactIds)
        .build()
}