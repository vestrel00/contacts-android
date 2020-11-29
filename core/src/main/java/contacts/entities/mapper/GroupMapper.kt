package contacts.entities.mapper

import contacts.entities.Group
import contacts.entities.cursor.GroupsCursor

internal class GroupMapper(private val groupsCursor: GroupsCursor) : EntityMapper<Group> {

    override val value: Group
        get() = Group(
            id = groupsCursor.id,

            title = groupsCursor.title,

            readOnly = groupsCursor.readOnly,
            favorites = groupsCursor.favorites,
            autoAdd = groupsCursor.autoAdd,

            account = groupsCursor.account
        )
}
