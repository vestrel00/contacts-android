package com.vestrel00.contacts.groups

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.mapper.groupMapper
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.isEmpty
import com.vestrel00.contacts.util.query
import com.vestrel00.contacts.util.toRawContactsWhere

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
 * Here is an example query that returns the first 5 [Groups]s, skipping the first 2, where the
 * group's title starts with "a", ordered by the group title in ascending order (ignoring case).
 *
 * ```kotlin
 * val groups = groupsQuery
 *      .accounts(account)
 *      .where(GroupsFields.Title startsWith "a")
 *      .orderBy(GroupsFields.Title.asc())
 *      .limit(5)
 *      .offset(2)
 *      .find()
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
     * Limits the group(s) returned by this query to groups belonging to the given [accounts].
     *
     * If no accounts are specified (this function is not called or called with no Accounts), then
     * all Groups are included in the search.
     *
     * A null Account may not be provided here because groups may only exist with an Account.
     *
     * You may also use [where] in conjunction with [GroupsFields.AccountName] and
     * [GroupsFields.AccountType] for a more flexible search.
     */
    fun accounts(vararg accounts: Account): GroupsQuery

    /**
     * See [GroupsQuery.accounts].
     */
    fun accounts(accounts: Collection<Account>): GroupsQuery

    /**
     * See [GroupsQuery.accounts].
     */
    fun accounts(accounts: Sequence<Account>): GroupsQuery

    /**
     * Filters the returned [Group]s matching the criteria defined by the [where]. If not specified
     * or null, then all [Group]s are returned, limited by [limit].
     *
     * Use [GroupsFields] to construct the [where].
     */
    fun where(where: Where?): GroupsQuery

    /**
     * Orders the returned [Group]s using one or more [orderBy]s. If not specified, then groups
     * are ordered by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase`
     * as an optional parameter.
     *
     * Use [GroupsFields] to construct the [orderBy].
     */
    fun orderBy(vararg orderBy: OrderBy): GroupsQuery

    /**
     * See [GroupsQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy>): GroupsQuery

    /**
     * See [GroupsQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy>): GroupsQuery

    /**
     * Limits the maximum number of returned [Group]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): GroupsQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): GroupsQuery

    /**
     * Returns the [GroupsList]s matching the preceding query options.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun find(): GroupsList

    /**
     * Returns the [GroupsList]s matching the preceding query options.
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
    fun find(cancel: () -> Boolean): GroupsList

    /**
     * The combined list of [Group]s from the specified Accounts ordered by [orderBy].
     *
     * The [offset] and [limit] functions applies to this list.
     *
     * Use [from], to get the list of Groups for a specific Account.
     *
     * ## Developer notes
     *
     * We technically did not need this custom interface. We could just use the generic List<Group>
     * and created extension functions for it. However, this is more Java-friendly. Furthermore, it
     * gives us the flexibility to add more stuff to it without any overhead.
     */
    interface GroupsList : List<Group> {

        /**
         * The list of [Group]s from the specified [account] ordered by [orderBy].
         *
         * The [offset] and [limit] functions DOES NOT apply to this list.
         */
        fun from(account: Account): List<Group>
    }
}

@Suppress("FunctionName")
internal fun GroupsQuery(context: Context): GroupsQuery = GroupsQueryImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class GroupsQueryImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,

    private var rawContactsWhere: Where? = DEFAULT_RAW_CONTACTS_WHERE,
    private var where: Where? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : GroupsQuery {

    override fun toString(): String {
        return """
            rawContactsWhere = $rawContactsWhere
            where = $where
            orderBy = $orderBy
            limit = $limit
            offset = $offset
        """.trimIndent()
    }

    override fun accounts(vararg accounts: Account): GroupsQuery = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account>): GroupsQuery =
        accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account>): GroupsQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()
    }

    override fun where(where: Where?): GroupsQuery = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = where ?: DEFAULT_WHERE
    }

    override fun orderBy(vararg orderBy: OrderBy): GroupsQuery = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy>): GroupsQuery = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy>): GroupsQuery = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun limit(limit: Int): GroupsQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw IllegalArgumentException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): GroupsQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw IllegalArgumentException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): GroupsQuery.GroupsList = find { false }

    override fun find(cancel: () -> Boolean): GroupsQuery.GroupsList =
        if (!permissions.canQuery()) {
            GroupsListImpl()
        } else {
            contentResolver.resolve(
                rawContactsWhere, INCLUDE, where, orderBy, limit, offset, cancel
            )
        }

    companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where? = null
        val INCLUDE = Include(GroupsFields)
        val DEFAULT_WHERE: Where? = null
        val DEFAULT_ORDER_BY = CompoundOrderBy(setOf(GroupsFields.Id.asc()))
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private fun ContentResolver.resolve(
    rawContactsWhere: Where?,
    include: Include,
    where: Where?,
    orderBy: CompoundOrderBy,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): GroupsQuery.GroupsList = query(
    Table.GROUPS,
    include,
    if (rawContactsWhere != null) {
        if (where != null) {
            rawContactsWhere and where
        } else {
            rawContactsWhere
        }
    } else {
        where
    },
    sortOrder = "$orderBy LIMIT $limit OFFSET $offset"
) {
    val groupsList = GroupsListImpl()
    val groupMapper = it.groupMapper()

    while (!cancel() && it.moveToNext()) {
        groupsList.add(groupMapper.value)
    }

    // Ensure incomplete data sets are not returned.
    if (cancel()) {
        groupsList.clear()
    }

    groupsList
} ?: GroupsListImpl()

private class GroupsListImpl : ArrayList<Group>(), GroupsQuery.GroupsList {

    override fun from(account: Account): List<Group> = filter { it.account == account }
}