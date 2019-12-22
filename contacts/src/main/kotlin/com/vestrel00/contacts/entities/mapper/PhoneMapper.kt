package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutablePhone
import com.vestrel00.contacts.entities.cursor.PhoneCursor

internal class PhoneMapper(private val phoneCursor: PhoneCursor) {

    val phone: MutablePhone
        get() = MutablePhone(
            id = phoneCursor.id,
            rawContactId = phoneCursor.rawContactId,
            contactId = phoneCursor.contactId,

            type = phoneCursor.type,
            label = phoneCursor.label,

            number = phoneCursor.number,
            normalizedNumber = phoneCursor.normalizedNumber
        )
}
