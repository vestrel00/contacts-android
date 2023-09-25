package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.GroupsField
import contacts.core.GroupsFields
import contacts.core.entities.Entity

/**
 * Retrieves [GroupsFields] data from the given [cursor].
 */
internal class GroupsCursor(cursor: Cursor, includeFields: Set<GroupsField>?) :
    AbstractEntityCursor<GroupsField>(cursor, includeFields), AccountCursor {

    override val accountName: String? by string(GroupsFields.AccountName)

    override val accountType: String? by string(GroupsFields.AccountType)

    val sourceId: String? by string(GroupsFields.SourceId)

    val id: Long by nonNullLong(GroupsFields.Id, Entity.INVALID_ID)

    val systemId: String? by string(GroupsFields.SystemId)

    val title: String by nonNullString(GroupsFields.Title, "null")

    val isReadOnly: Boolean by nonNullBoolean(GroupsFields.GroupIsReadOnly)

    val favorites: Boolean by nonNullBoolean(GroupsFields.Favorites)

    val autoAdd: Boolean by nonNullBoolean(GroupsFields.AutoAdd)
}