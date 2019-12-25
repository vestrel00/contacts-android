package com.vestrel00.contacts.groups

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.cursor.GroupCursor
import com.vestrel00.contacts.entities.mapper.GroupMapper
import com.vestrel00.contacts.entities.table.Table

interface GroupsQuery {

    fun account(account: Account): GroupsQuery

    fun withIds(vararg groupIds: Long): GroupsQuery

    fun withIds(groupIds: Collection<Long>): GroupsQuery

    fun withIds(groupIds: Sequence<Long>): GroupsQuery

    fun find(): List<Group>

    fun find(cancel: () -> Boolean): List<Group>

    fun findFirst(): Group?
    
    fun findFirst(cancel: () -> Boolean): Group?
}

@Suppress("FunctionName")
internal fun GroupsQuery(context: Context): GroupsQuery = GroupsQueryImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class GroupsQueryImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,

    private var account: Account? = null,
    private var groupIds: Set<Long> = emptySet()
) : GroupsQuery {

    private val where: Where
        get() {
            var where: Where = NoWhere

            account?.let {
                // Limit the query to the given account.
                where = (Fields.Group.AccountName equalTo it.name) and
                        (Fields.Group.AccountType equalTo it.type)
            }

            if (groupIds.isNotEmpty()) {
                // Limit the query to the given set of ids.
                where = if (where != NoWhere) {
                    where and (Fields.Group.Id `in` groupIds)
                } else {
                    Fields.Group.Id `in` groupIds
                }
            }

            return where
        }

    override fun account(account: Account): GroupsQuery = apply {
        this.account = account
    }

    override fun withIds(vararg groupIds: Long): GroupsQuery = apply {
        this.groupIds = groupIds.toSet()
    }

    override fun withIds(groupIds: Collection<Long>): GroupsQuery = apply {
        this.groupIds = groupIds.toSet()
    }

    override fun withIds(groupIds: Sequence<Long>): GroupsQuery = apply {
        this.groupIds = groupIds.toSet()
    }

    override fun find(): List<Group> = find { false }

    override fun find(cancel: () -> Boolean): List<Group> {
        if (!permissions.canQuery()) {
            return emptyList()
        }

        val groups = mutableListOf<Group>()

        val where = where
        val cursor = contentResolver.query(
            Table.GROUPS.uri,
            Include(Fields.Group).columnNames,
            if (where == NoWhere) null else "$where",
            null,
            null
        )

        if (cursor != null) {
            val groupMapper = GroupMapper(GroupCursor(cursor))

            while (cursor.moveToNext()) {
                val group = groupMapper.group.toGroup()
                groups.add(group)

                if (cancel()) {
                    // Return empty list if cancelled to ensure only correct data set is returned.
                    return emptyList()
                }
            }

            cursor.close()
        }

        return groups
    }

    override fun findFirst(): Group? = findFirst { false }

    override fun findFirst(cancel: () -> Boolean): Group? = find(cancel).firstOrNull()
}
