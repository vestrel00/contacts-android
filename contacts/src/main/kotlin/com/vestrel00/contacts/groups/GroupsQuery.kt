package com.vestrel00.contacts.groups

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.mapper.groupMapper
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.query

/**
 * Queries on the groups table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All queries will return an empty list if the permission is not granted.
 *
 * ## Usage
 *
 * To get all groups for a given account;
 *
 * ```kotlin
 * val groups = groupsQuery
 *      .account(account)
 *      .find()
 * ```
 *
 * ## Where, orderBy, offset, and limit
 *
 * Given the nature of groups, this library makes an assumption that there are not that many groups
 * **per Account**. Typical Accounts usually have less than 10. Even those in large companies, have
 * less than 100 (?) groups. This assumption means that the query function of groups need not be as
 * extensive (or at all) as other Queries. Where, orderBy, offset, and limit functions are left to
 * consumers to implement if they wish.
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
     * Returns the list of [Group]s belonging to the [Account] specified in [account] that match IDs
     * specified in [withIds].
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
     * Returns the list of [Group]s belonging to the [Account] specified in [account] that match IDs
     * specified in [withIds].
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
     * Returns the first [Group] belonging to the [Account] specified in [account] that match IDs
     * specified in [withIds].
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
     * Returns the first [Group] belonging to the [Account] specified in [account] that match IDs
     * specified in [withIds].
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

    private val where: Where?
        get() {
            var where: Where? = null

            account?.let {
                // Limit the query to the given account.
                where = (GroupsFields.AccountName equalTo it.name) and
                        (GroupsFields.AccountType equalTo it.type)
            }

            if (groupIds.isNotEmpty()) {
                // Limit the query to the given set of ids.
                val currentWhere = where // to avoid lint errors or force unwrapping
                where = if (currentWhere != null) {
                    currentWhere and (GroupsFields.Id `in` groupIds)
                } else {
                    GroupsFields.Id `in` groupIds
                }
            }

            return where
        }

    override fun account(account: Account): GroupsQuery = apply {
        this.account = account
    }

    override fun withIds(vararg groupIds: Long): GroupsQuery = withIds(groupIds.asSequence())

    override fun withIds(groupIds: Collection<Long>): GroupsQuery = withIds(groupIds.asSequence())

    override fun withIds(groupIds: Sequence<Long>): GroupsQuery = apply {
        this.groupIds = groupIds.toSet()
    }

    override fun find(): List<Group> = find { false }

    override fun find(cancel: () -> Boolean): List<Group> {
        if (!permissions.canQuery()) {
            return emptyList()
        }

        return contentResolver.query(Table.GROUPS, Include(GroupsFields), where) {
            mutableListOf<Group>().apply {
                val groupMapper = it.groupMapper()
                while (!cancel() && it.moveToNext()) {
                    add(groupMapper.value)
                }

                // Ensure only complete data set is returned.
                if (cancel()) {
                    clear()
                }
            }
        } ?: emptyList()
    }

    override fun findFirst(): Group? = findFirst { false }

    override fun findFirst(cancel: () -> Boolean): Group? = find(cancel).firstOrNull()
}
