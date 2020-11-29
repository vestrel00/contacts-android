package contacts.entities.mapper

import contacts.entities.Email
import contacts.entities.cursor.EmailCursor

internal class EmailMapper(private val emailCursor: EmailCursor) : EntityMapper<Email> {

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
