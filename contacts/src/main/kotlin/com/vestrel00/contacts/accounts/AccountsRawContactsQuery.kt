package com.vestrel00.contacts.accounts

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.BlankRawContact
import com.vestrel00.contacts.entities.cursor.account
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.mapper.blankRawContactMapper
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.isEmpty
import com.vestrel00.contacts.util.query
import com.vestrel00.contacts.util.toRawContactsWhere

/**
 * Queries on the RawContacts table.
 *
 * These queries return [BlankRawContact]s, which are RawContacts that contains no data (e.g. email,
 * phone) only containing critical information for performing RawContact operations such as
 * [AccountsRawContactsAssociationsUpdate].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All queries will return an empty list if the permission is not granted.
 *
 * ## Usage
 *
 * Here is an example query that returns the first 5 [BlankRawContact]s in the account, skipping the
 * first 2, where the RawContact's display name starts with "a", ordered by the display name in
 * ascending order (ignoring case).
 *
 * ```kotlin
 * val blankRawContacts = accountsRawContactsQuery
 *      .accounts(account)
 *      .where(RawContactsFields.DisplayName startsWith "a")
 *      .orderBy(RawContactsFields.DisplayName.asc())
 *      .limit(5)
 *      .offset(2)
 *      .find()
 * ```
 */
interface AccountsRawContactsQuery {

    /**
     * Limits the [BlankRawContact]s returned by this query to those belonging to the given
     * [accounts].
     *
     * If no accounts are specified (this function is not called or called with no Accounts), then
     * all [BlankRawContact]s are included in the search.
     *
     * A null [Account] may be provided here, which results in data belonging to RawContacts with no
     * associated Account to be included in the search. RawContacts without an associated account
     * are considered local or device-only contacts, which are not synced.
     *
     * You may also use [where] in conjunction with [RawContactsFields.AccountName] and
     * [RawContactsFields.AccountType] for a more flexible search.
     */
    fun accounts(vararg accounts: Account?): AccountsRawContactsQuery

    /**
     * See [AccountsRawContactsQuery.accounts].
     */
    fun accounts(accounts: Collection<Account?>): AccountsRawContactsQuery

    /**
     * See [AccountsRawContactsQuery.accounts].
     */
    fun accounts(accounts: Sequence<Account?>): AccountsRawContactsQuery

    /**
     * Filters the returned [BlankRawContact]s matching the criteria defined by the [where]. If not
     * specified or null, then all [BlankRawContact]s are returned.
     *
     * Use [RawContactsFields] to construct the [where].
     */
    fun where(where: Where?): AccountsRawContactsQuery

    /**
     * Orders the returned [BlankRawContact]s using one or more [orderBy]s. If not specified, then
     * data is ordered by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     *
     * Use [RawContactsFields] to construct the [orderBy].
     */
    fun orderBy(vararg orderBy: OrderBy): AccountsRawContactsQuery

    /**
     * See [AccountsRawContactsQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy>): AccountsRawContactsQuery

    /**
     * See [AccountsRawContactsQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy>): AccountsRawContactsQuery

    /**
     * Limits the maximum number of returned [BlankRawContact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): AccountsRawContactsQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): AccountsRawContactsQuery

    /**
     * Returns the [BlankRawContactsList] matching the preceding query options.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun find(): BlankRawContactsList

    /**
     * Returns the [BlankRawContactsList] matching the preceding query options.
     *
     * ## Cancellation
     *
     * The number of RawContacts processed may be large, which results in this operation to take a
     * while. Therefore, cancellation is supported while the RawContacts list is being built. To
     * cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun find(cancel: () -> Boolean = { false }): BlankRawContactsList
    fun find(cancel: () -> Boolean): BlankRawContactsList

    /**
     * The combined list of [BlankRawContact]s from the specified Accounts ordered by [orderBy].
     *
     * The [offset] and [limit] functions applies to this list.
     *
     * Use [rawContactsFor], to get the list of RawContacts for a specific Account.
     */
    interface BlankRawContactsList : List<BlankRawContact> {

        /**
         * The list of [BlankRawContact]s from the specified [account] ordered by [orderBy].
         *
         * The [offset] and [limit] functions DOES NOT apply to this list.
         */
        fun rawContactsFor(account: Account?): List<BlankRawContact>
    }
}

@Suppress("FunctionName")
internal fun AccountsRawContactsQuery(context: Context): AccountsRawContactsQuery =
    AccountsRawContactsQueryImpl(
        context.contentResolver,
        AccountsPermissions(context)
    )

private class AccountsRawContactsQueryImpl(
    private val contentResolver: ContentResolver,
    private val permissions: AccountsPermissions,

    private var rawContactsWhere: Where? = DEFAULT_RAW_CONTACTS_WHERE,
    private var where: Where? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : AccountsRawContactsQuery {

    override fun toString(): String {
        return """
            rawContactsWhere = $rawContactsWhere
            where = $where
            orderBy = $orderBy
            limit = $limit
            offset = $offset
        """.trimIndent()
    }

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): AccountsRawContactsQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()
    }

    override fun where(where: Where?): AccountsRawContactsQuery = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = where ?: DEFAULT_WHERE
    }

    override fun orderBy(vararg orderBy: OrderBy) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy>): AccountsRawContactsQuery = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun limit(limit: Int): AccountsRawContactsQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw IllegalArgumentException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): AccountsRawContactsQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw IllegalArgumentException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): AccountsRawContactsQuery.BlankRawContactsList = find { false }

    override fun find(cancel: () -> Boolean): AccountsRawContactsQuery.BlankRawContactsList =
        if (!permissions.canQueryRawContacts()) {
            BlankRawContactsListImpl(emptyMap())
        } else {
            contentResolver.resolve(
                rawContactsWhere, INCLUDE, where, orderBy, limit, offset, cancel
            )
        }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where? = null
        val INCLUDE = Include(RawContactsFields)
        val DEFAULT_WHERE: Where? = null
        val DEFAULT_ORDER_BY = CompoundOrderBy(setOf(RawContactsFields.Id.asc()))
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
): AccountsRawContactsQuery.BlankRawContactsList = query(
    Table.RAW_CONTACTS,
    include,
    if (rawContactsWhere != null) {
        if (where != null) {
            rawContactsWhere and where
        } else {
            rawContactsWhere
        } and RawContactsFields.ContactId.isNotNull()
    } else {
        if (where != null) {
            where and RawContactsFields.ContactId.isNotNull()
        } else {
            RawContactsFields.ContactId.isNotNull()
        }
    },
    sortOrder = "$orderBy LIMIT $limit OFFSET $offset"
) {
    val accountRawContactsMap = mutableMapOf<Account?, MutableList<BlankRawContact>>()
    val rawContactsList = BlankRawContactsListImpl(accountRawContactsMap)

    val rawContactsCursor = it.rawContactsCursor()
    val blankRawContactMapper = it.blankRawContactMapper(rawContactsCursor, false)

    while (!cancel() && it.moveToNext()) {
        val account = rawContactsCursor.account()
        val rawContactsInMap = accountRawContactsMap.getOrPut(account) { mutableListOf() }

        val blankRawContact = blankRawContactMapper.value

        // The cursor is ordered in ascending order by the display name. Therefore, these lists are
        // already in order.
        rawContactsList.add(blankRawContact)
        rawContactsInMap.add(blankRawContact)
    }

    // Ensure incomplete data sets are not returned.
    if (cancel()) {
        accountRawContactsMap.clear()
    }

    rawContactsList

} ?: BlankRawContactsListImpl(emptyMap())

private class BlankRawContactsListImpl(
    private val accountRawContactsMap: Map<Account?, List<BlankRawContact>>
) : ArrayList<BlankRawContact>(), AccountsRawContactsQuery.BlankRawContactsList {

    override fun rawContactsFor(account: Account?): List<BlankRawContact> =
        accountRawContactsMap.getOrElse(account) { emptyList() }
}