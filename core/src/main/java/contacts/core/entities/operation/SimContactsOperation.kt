package contacts.core.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentValues
import contacts.core.SimContactsFields
import contacts.core.entities.NewSimContact
import contacts.core.entities.SimContactEntity
import contacts.core.entities.table.Table

/**
 * Builds [ContentProviderOperation]s for [Table.SimContacts].
 */
internal class SimContactsOperation {

    fun insert(simContact: NewSimContact): ContentValues? = if (simContact.isBlank) {
        null
    } else {
        ContentValues().apply {
            put(SimContactsFields.Tag.columnName, simContact.name)
            put(SimContactsFields.Number.columnName, simContact.number)
        }
    }

    // The ID is not used here at all. Therefore, we can be very flexible by allowing any
    // implementations of SimContactEntity.
    fun update(
        originalSimContact: SimContactEntity, updatedSimContact: SimContactEntity
    ): ContentValues? = if (updatedSimContact.isBlank) {
        null
    } else {
        ContentValues().apply {
            put(SimContactsFields.Tag.columnName, originalSimContact.name)
            put(SimContactsFields.Number.columnName, originalSimContact.number)

            put(SimContactsFields.NewTag.columnName, updatedSimContact.name)
            put(SimContactsFields.NewNumber.columnName, updatedSimContact.number)
        }
    }
}