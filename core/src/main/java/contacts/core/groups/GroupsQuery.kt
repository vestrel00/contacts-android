package contacts.core.groups

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.Group
import contacts.core.entities.mapper.groupMapper
import contacts.core.entities.table.Table
import contacts.core.util.isEmpty
import contacts.core.util.query
import contacts.core.util.toGroupsWhere
import contacts.core.util.unsafeLazy

/**
 * Queries on the groups table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. If not granted, the query will do nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example query that returns the first 5 [Group]s in the account, skipping the first 2,
 * where the group's title starts with "a", ordered by the group title in ascending order
 * (ignoring case).
 *
 * In Kotlin,
 *
 * ```kotlin
 * val groups = groupsQuery
 *      .accounts(account)
 *      .where { Title startsWith "a" }
 *      .orderBy(GroupsFields.Title.asc())
 *      .limit(5)
 *      .offset(2)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ```kotlin
 * import static contacts.core.GroupsFields.*;
 * import static contacts.core.WhereKt.*;
 * import static contacts.core.OrderByKt.*;
 *
 * val groups = groupsQuery
 *      .accounts(account)
 *      .where(startsWith(Title, "a"))
 *      .orderBy(asc(GroupsFields.Title))
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
interface GroupsQuery : CrudApi {

    /**
     * Limits the group(s) returned by this query to groups belonging to one of the [accounts].
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
     * or null, then all [Group]s are returned.
     *
     * Use [GroupsFields] to construct the [where].
     */
    fun where(where: Where<GroupsField>?): GroupsQuery

    /**
     * See [GroupsQuery.where]
     */
    fun where(where: GroupsFields.() -> Where<GroupsField>?): GroupsQuery

    /**
     * Orders the returned [Group]s using one or more [orderBy]s. If not specified, then groups
     * are ordered by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     *
     * Use [GroupsFields] to construct the [orderBy].
     */
    @SafeVarargs
    fun orderBy(vararg orderBy: OrderBy<GroupsField>): GroupsQuery

    /**
     * See [GroupsQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<GroupsField>>): GroupsQuery

    /**
     * See [GroupsQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<GroupsField>>): GroupsQuery

    /**
     * See [GroupsQuery.orderBy].
     */
    fun orderBy(orderBy: GroupsFields.() -> Sequence<OrderBy<GroupsField>>): GroupsQuery

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
     * Returns the [Result] matching the preceding query options.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun find(): Result

    /**
     * Returns the [Result] matching the preceding query options.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION].
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
    // fun find(cancel: () -> Boolean = { false }): Result
    fun find(cancel: () -> Boolean): Result

    /**
     * Returns a redacted instance where all private user data are redacted.
     *
     * ## Redacted instances may produce invalid results!
     *
     * Redacted instance may have critical information redacted, which is required to make
     * the operation work properly.
     *
     * **Redacted operations should typically only be used for logging in production!**
     */
    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): GroupsQuery

    /**
     * The combined list of [Group]s from the specified Accounts ordered by [orderBy].
     *
     * Use [from], to get the list of Groups for a specific Account.
     *
     * ## The [toString] function
     *
     * The [toString] function of instances of this will not return the string representation of
     * every group in the list. It will instead return a summary of the groups in the list and
     * perhaps the first group only.
     *
     * This is done due to the potentially large quantities of groups, which could block the UI if
     * not logging in background threads.
     *
     * You may print individual groups in this list by iterating through it.
     */
    interface Result : List<Group>, CrudApi.Result {

        /**
         * The list of [Group]s from the specified [account] ordered by [orderBy].
         *
         * The [offset] and [limit] functions DOES NOT apply to this list.
         */
        fun from(account: Account): List<Group>

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun GroupsQuery(contacts: Contacts): GroupsQuery = GroupsQueryImpl(contacts)

private class GroupsQueryImpl(
    override val contactsApi: Contacts,

    // The Groups table has access to the same sync columns as the RawContacts table, which provides
    // the Account name and type.
    private var rawContactsWhere: Where<GroupsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var where: Where<GroupsField>? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy<GroupsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,

    override val isRedacted: Boolean = false
) : GroupsQuery {

    override fun toString(): String =
        """
            GroupsQuery {
                rawContactsWhere: $rawContactsWhere
                where: $where
                orderBy: $orderBy
                limit: $limit
                offset: $offset
                hasPermission: ${permissions.canQuery()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsQuery = GroupsQueryImpl(
        contactsApi,

        // Redact account info.
        rawContactsWhere?.redactedCopy(),
        // Redact search input.
        where?.redactedCopy(),
        orderBy,
        limit,
        offset,

        isRedacted = true
    )

    override fun accounts(vararg accounts: Account) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account>): GroupsQuery = apply {
        rawContactsWhere = accounts.toGroupsWhere()?.redactedCopyOrThis(isRedacted)
    }

    override fun where(where: Where<GroupsField>?): GroupsQuery = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = (where ?: DEFAULT_WHERE)?.redactedCopyOrThis(isRedacted)
    }

    override fun where(where: GroupsFields.() -> Where<GroupsField>?) = where(where(GroupsFields))

    override fun orderBy(vararg orderBy: OrderBy<GroupsField>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<GroupsField>>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<GroupsField>>): GroupsQuery = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun orderBy(orderBy: GroupsFields.() -> Sequence<OrderBy<GroupsField>>) =
        orderBy(orderBy(GroupsFields))

    override fun limit(limit: Int): GroupsQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): GroupsQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): GroupsQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): GroupsQuery.Result {
        onPreExecute()

        return if (!permissions.canQuery()) {
            GroupsQueryResult(emptyList())
        } else {
            contentResolver.resolve(
                rawContactsWhere, INCLUDE, where, orderBy, limit, offset, cancel
            )
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<GroupsField>? = null
        val INCLUDE by unsafeLazy { Include(GroupsFields) }
        val DEFAULT_WHERE: Where<GroupsField>? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(GroupsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private fun ContentResolver.resolve(
    rawContactsWhere: Where<GroupsField>?,
    include: Include<GroupsField>,
    where: Where<GroupsField>?,
    orderBy: CompoundOrderBy<GroupsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): GroupsQuery.Result = query(
    Table.Groups,
    include,
    // There may be Groups that are marked for deletion that have not yet been deleted.
    (GroupsFields.Deleted notEqualTo true)
        .and(
            if (rawContactsWhere != null) {
                if (where != null) {
                    rawContactsWhere and where
                } else {
                    rawContactsWhere
                }
            } else {
                where
            }
        ),
    sortOrder = "$orderBy LIMIT $limit OFFSET $offset"
) {
    val groupsList = mutableListOf<Group>()
    val groupMapper = it.groupMapper()

    while (!cancel() && it.moveToNext()) {
        groupsList.add(groupMapper.value)
    }

    // Ensure incomplete data sets are not returned.
    if (cancel()) {
        groupsList.clear()
    }

    GroupsQueryResult(groupsList)

} ?: GroupsQueryResult(emptyList())

private class GroupsQueryResult private constructor(
    groups: List<Group>,
    override val isRedacted: Boolean
) : ArrayList<Group>(groups), GroupsQuery.Result {

    constructor(groups: List<Group>) : this(groups, false)

    override fun toString(): String =
        """
            GroupsQuery.Result {
                Number of groups found: $size
                First group: ${firstOrNull()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsQuery.Result = GroupsQueryResult(
        redactedCopies(),
        isRedacted = true
    )

    override fun from(account: Account): List<Group> = filter { it.account == account }
}