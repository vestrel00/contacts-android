package contacts.entities.mapper

import contacts.entities.Nickname
import contacts.entities.cursor.NicknameCursor

internal class NicknameMapper(private val nicknameCursor: NicknameCursor) : EntityMapper<Nickname> {

    override val value: Nickname
        get() = Nickname(
            id = nicknameCursor.dataId,
            rawContactId = nicknameCursor.rawContactId,
            contactId = nicknameCursor.contactId,

            isPrimary = nicknameCursor.isPrimary,
            isSuperPrimary = nicknameCursor.isSuperPrimary,

            name = nicknameCursor.name
        )
}
