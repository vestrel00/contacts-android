package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableNickname
import com.vestrel00.contacts.entities.Nickname
import com.vestrel00.contacts.entities.cursor.NicknameCursor

internal class NicknameMapper(private val nicknameCursor: NicknameCursor) :
    EntityMapper<Nickname, MutableNickname> {

    override val toImmutable: Nickname
        get() = Nickname(
            id = nicknameCursor.id,
            rawContactId = nicknameCursor.rawContactId,
            contactId = nicknameCursor.contactId,

            isPrimary = nicknameCursor.isPrimary,
            isSuperPrimary = nicknameCursor.isSuperPrimary,

            name = nicknameCursor.name
        )

    override val toMutable: MutableNickname
        get() = MutableNickname(
            id = nicknameCursor.id,
            rawContactId = nicknameCursor.rawContactId,
            contactId = nicknameCursor.contactId,

            isPrimary = nicknameCursor.isPrimary,
            isSuperPrimary = nicknameCursor.isSuperPrimary,

            name = nicknameCursor.name
        )
}
