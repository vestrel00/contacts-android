package contacts.core.entities.operation

import android.content.ContentProviderOperation
import contacts.core.GroupsFields
import contacts.core.entities.ExistingGroupEntity
import contacts.core.entities.NewGroup
import contacts.core.entities.table.Table
import contacts.core.equalTo

private val TABLE = Table.Groups

/**
 * Builds [ContentProviderOperation]s for [Table.Groups].
 */
internal class GroupsOperation constructor(private val callerIsSyncAdapter: Boolean) {

    fun insert(group: NewGroup): ContentProviderOperation = newInsert(TABLE, callerIsSyncAdapter)
        .withValue(GroupsFields.Title, group.title)
        .withValue(GroupsFields.AccountName, group.account?.name)
        .withValue(GroupsFields.AccountType, group.account?.type)
        .withValue(GroupsFields.SourceId, group.sourceId)
        // Setting favorites and auto add has no effect. The Contacts Provider will routinely set
        // them to false for all user-created groups.
        // .withValue(Fields.Group.Favorites, it.favorites.toSqlValue())
        // .withValue(Fields.Group.AutoAdd, it.autoAdd.toSqlValue())
        .build()

    fun update(group: ExistingGroupEntity): ContentProviderOperation =
        newUpdate(TABLE, callerIsSyncAdapter)
            .withSelection(GroupsFields.Id equalTo group.id)
            .withValue(GroupsFields.Title, group.title)
            .withValue(GroupsFields.SourceId, group.sourceId)
            .build()
}