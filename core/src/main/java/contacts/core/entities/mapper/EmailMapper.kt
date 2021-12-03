package contacts.core.entities.mapper

import contacts.core.entities.Email
import contacts.core.entities.cursor.EmailCursor

internal class EmailMapper(private val emailCursor: EmailCursor) : DataEntityMapper<Email> {

    override val value: Email
        get() = Email(
            id = emailCursor.dataId,
            rawContactId = emailCursor.rawContactId,
            contactId = emailCursor.contactId,

            isPrimary = emailCursor.isPrimary,
            isSuperPrimary = emailCursor.isSuperPrimary,

            type = emailCursor.type,
            label = emailCursor.label,

            address = emailCursor.address
        )
}
