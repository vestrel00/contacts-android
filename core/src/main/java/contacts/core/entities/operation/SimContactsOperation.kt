package contacts.core.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentValues
import contacts.core.SimContactsFields
import contacts.core.entities.MutableSimContact
import contacts.core.entities.NewSimContact
import contacts.core.entities.SimContact
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

    fun update(
        originalSimContact: SimContact, updatedSimContact: MutableSimContact
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

    /**
     * Returns a where clause that uses the [SimContact.name] (tag) and [SimContact.number] to
     * select the contact to delete. This is the only form of selection that is supported. Selecting
     * by _id is not supported because they are not constant.
     */
    fun delete(simContact: SimContact): String =
    // We will not construct the where String using our own Where functions to avoid generating
        // parenthesis, which breaks the way this where clause is processed.
        "tag='${simContact.name}' AND number='${simContact.number}'"
}