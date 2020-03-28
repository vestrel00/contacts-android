package com.vestrel00.contacts.groups

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.mapper.groupMapper
import com.vestrel00.contacts.entities.table.Table

/**
 * Queries on the groups table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All queries will return an empty list if the permission is not granted.
 *
 * ## Filtering
 *
 * Given the nature of groups, this library makes an assumption that there are not that many groups.
 * Typical users usually have less than 10. Even those in large companies, have less than 100 (?)
 * groups. This assumption means that the query function of groups need not be as extensive (or at
 * all) as Contacts Query. Filter, order, offset, and limit functions are left to consumers to
 * implement if they wish.
 *
 * ## Usage
 *
 * To get all groups for a given account;
 *
 * In Kotlin and Java,
 *
 * ```kotlin
 * groupsQuery.fromAccount(account)
 * ```
 *
 * ## Developer notes
 *
 * Groups are inextricably linked to Accounts. A group must be assigned to an account, if an account
 * is available. The native Contacts app only shows groups that belong to the selected account. When
 * there are no available accounts, the native Contacts app does not show the groups field because
 * there are no rows in the groups table.
 */
interface GroupsQuery {

    /**
     * Limits the group(s) returned by this query to groups belonging to the given [account].
     */
    fun account(account: Account): GroupsQuery

    /**
     * Limits the group(s) returned by this query to groups that match the given [groupIds].
     */
    fun withIds(vararg groupIds: Long): GroupsQuery

    /**
     * See [GroupsQuery.withIds].
     */
    fun withIds(groupIds: Collection<Long>): GroupsQuery

    /**
     * See [GroupsQuery.withIds].
     */
    fun withIds(groupIds: Sequence<Long>): GroupsQuery

    /**
     * Returns the list of [Group]s belonging to the [Account] specified in [fromAccount] that match
     * IDs specified in [withIds].
     *
     * If no [Account] or ID is provided, then this will return all [Groups] from all available
     * [Account]s.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun find(): List<Group>

    /**
     * Returns the list of [Group]s belonging to the [Account] specified in [fromAccount] that match
     * IDs specified in [withIds].
     *
     * If no [Account] or ID is provided, then this will return all [Groups] from all available
     * [Account]s.
     *
     * ## Cancellation
     *
     * The number of group data found may take more than a few milliseconds to process. Therefore,
     * cancellation is supported while the groups list is being built. To cancel at any time, the
     * [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun find(cancel: () -> Boolean = { false }): List<Group>
    fun find(cancel: () -> Boolean): List<Group>

    /**
     * Returns the first [Group] belonging to the [Account] specified in [fromAccount] that match
     * IDs specified in [withIds].
     *
     * If no [Account] or ID is provided, then this will return all [Groups] from all available
     * [Account]s.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    fun findFirst(): Group?

    /**
     * Returns the first [Group] belonging to the [Account] specified in [fromAccount] that match
     * IDs specified in [withIds].
     *
     * If no [Account] or ID is provided, then this will return all [Groups] from all available
     * [Account]s.
     *
     * ## Cancellation
     *
     * The number of group data found may take more than a few milliseconds to process. Therefore,
     * cancellation is supported while the groups list is being built. To cancel at any time, the
     * [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun findFirst(cancel: () -> Boolean = { false }): Group?
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
            val groupMapper = cursor.groupMapper()

            while (cursor.moveToNext()) {
                val group = groupMapper.value
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
