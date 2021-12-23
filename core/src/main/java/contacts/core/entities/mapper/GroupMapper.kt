package contacts.core.entities.mapper

import contacts.core.entities.Group
import contacts.core.entities.cursor.GroupsCursor

internal class GroupMapper(private val groupsCursor: GroupsCursor) : EntityMapper<Group> {

    override val value: Group
        get() = Group(
            id = groupsCursor.id,
            systemId = groupsCursor.systemId,

            title = groupsCursor.title,

            readOnly = groupsCursor.readOnly,
            favorites = groupsCursor.favorites,
            autoAdd = groupsCursor.autoAdd,

            account = groupsCursor.account,

            isRedacted = false
        )
}
