package contacts.core.entities.mapper

import contacts.core.entities.Phone
import contacts.core.entities.cursor.PhoneCursor

internal class PhoneMapper(private val phoneCursor: PhoneCursor) : DataEntityMapper<Phone> {

    override val value: Phone
        get() = Phone(
            id = phoneCursor.dataId,
            rawContactId = phoneCursor.rawContactId,
            contactId = phoneCursor.contactId,

            isPrimary = phoneCursor.isPrimary,
            isSuperPrimary = phoneCursor.isSuperPrimary,

            type = phoneCursor.type,
            label = phoneCursor.label,

            number = phoneCursor.number,
            normalizedNumber = phoneCursor.normalizedNumber
        )
}
