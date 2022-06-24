package contacts.core.accounts

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.BlankRawContact
import contacts.core.entities.cursor.account
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.mapper.blankRawContactMapper
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.util.*

/**
 * Queries for Profile OR non-Profile (depending on instance) RawContacts.
 *
 * Thee queries return [BlankRawContact]s, which are RawContacts that contains no data (e.g. email,
 * phone). It only contains critical information required for performing RawContact operations such
 * as associating local RawContacts to an Account ([AccountsLocalRawContactsUpdate]).
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
 * In Kotlin,
 *
 * ```kotlin
 * val blankRawContacts = accountsRawContactsQuery
 *      .accounts(account)
 *      .where { DisplayNamePrimary startsWith "a" }
 *      .orderBy(RawContactsFields.DisplayName.asc())
 *      .limit(5)
 *      .offset(2)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ``java
 * List<BlankRawContact> blankRawContacts = accountsRawContactsQuery
 *      .accounts(account)
 *      .where(startsWith(DisplayNamePrimary, "a"))
 *      .orderBy(asc(RawContactsFields.DisplayName))
 *      .limit(5)
 *      .offset(2)
 *      .find();
 * ``
 */
interface AccountsRawContactsQuery : CrudApi {

    /**
     * Limits the [BlankRawContact]s returned by this query to those belonging to one of the given
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
    fun where(where: Where<RawContactsField>?): AccountsRawContactsQuery

    /**
     * See [AccountsRawContactsQuery.where].
     */
    fun where(where: RawContactsFields.() -> Where<RawContactsField>?): AccountsRawContactsQuery

    /**
     * Orders the returned [BlankRawContact]s using one or more [orderBy]s. If not specified, then
     * data is ordered by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     *
     * Use [RawContactsFields] to construct the [orderBy].
     */
    @SafeVarargs
    fun orderBy(vararg orderBy: OrderBy<RawContactsField>): AccountsRawContactsQuery

    /**
     * See [AccountsRawContactsQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<RawContactsField>>): AccountsRawContactsQuery

    /**
     * See [AccountsRawContactsQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<RawContactsField>>): AccountsRawContactsQuery

    /**
     * See [AccountsRawContactsQuery.orderBy].
     */
    fun orderBy(
        orderBy: RawContactsFields.() -> Collection<OrderBy<RawContactsField>>
    ): AccountsRawContactsQuery

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
    override fun redactedCopy(): AccountsRawContactsQuery

    /**
     * The combined list of [BlankRawContact]s from the specified Accounts ordered by [orderBy].
     *
     * The [offset] and [limit] functions applies to this list.
     *
     * Use [rawContactsFor], to get the list of RawContacts for a specific Account.
     *
     * ## The [toString] function
     *
     * The [toString] function of instances of this will not return the string representation of
     * every RawContact in the list. It will instead return a summary of the RawContacts in the
     * list and perhaps the first RawContact only.
     *
     * This is done due to the potentially large quantities of RawContact, which could block the UI
     * if not logging in background threads.
     *
     * You may print individual RawContacts in this list by iterating through it.
     */
    interface Result : List<BlankRawContact>, CrudApi.Result {

        /**
         * The list of [BlankRawContact]s from the specified [account] ordered by [orderBy].
         *
         * The [offset] and [limit] functions DOES NOT apply to this list.
         */
        fun rawContactsFor(account: Account?): List<BlankRawContact>

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun AccountsRawContactsQuery(
    contacts: Contacts, isProfile: Boolean
): AccountsRawContactsQuery = AccountsRawContactsQueryImpl(contacts, isProfile)

private class AccountsRawContactsQueryImpl(
    override val contactsApi: Contacts,
    private val isProfile: Boolean,

    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var where: Where<RawContactsField>? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy<RawContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,

    override val isRedacted: Boolean = false
) : AccountsRawContactsQuery {

    override fun toString(): String =
        """
            AccountsRawContactsQuery {
                isProfile: $isProfile
                rawContactsWhere: $rawContactsWhere
                where: $where
                orderBy: $orderBy
                limit: $limit
                offset: $offset
                hasPermission: ${accountsPermissions.canQueryRawContacts()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsRawContactsQuery = AccountsRawContactsQueryImpl(
        contactsApi, isProfile,

        // Redact account info.
        rawContactsWhere?.redactedCopy(),
        // Redact search input
        where?.redactedCopy(),
        orderBy,
        limit,
        offset,

        isRedacted = true
    )

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): AccountsRawContactsQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()?.redactedCopyOrThis(isRedacted)
    }

    override fun where(where: Where<RawContactsField>?): AccountsRawContactsQuery = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = (where ?: DEFAULT_WHERE)?.redactedCopyOrThis(isRedacted)
    }

    override fun where(where: RawContactsFields.() -> Where<RawContactsField>?) =
        where(where(RawContactsFields))

    override fun orderBy(vararg orderBy: OrderBy<RawContactsField>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<RawContactsField>>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<RawContactsField>>): AccountsRawContactsQuery =
        apply {
            this.orderBy = if (orderBy.isEmpty()) {
                DEFAULT_ORDER_BY
            } else {
                CompoundOrderBy(orderBy.toSet())
            }
        }

    override fun orderBy(orderBy: RawContactsFields.() -> Collection<OrderBy<RawContactsField>>) =
        orderBy(orderBy(RawContactsFields))

    override fun limit(limit: Int): AccountsRawContactsQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): AccountsRawContactsQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): AccountsRawContactsQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): AccountsRawContactsQuery.Result {
        onPreExecute()

        return if (!accountsPermissions.canQueryRawContacts()) {
            AccountsRawContactsQueryResult(emptyList(), emptyMap())
        } else {
            contentResolver.resolve(
                isProfile, rawContactsWhere, INCLUDE, where, orderBy, limit, offset, cancel
            )
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val INCLUDE by unsafeLazy { Include(RawContactsFields) }
        val DEFAULT_WHERE: Where<RawContactsField>? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(RawContactsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private fun ContentResolver.resolve(
    isProfile: Boolean,
    rawContactsWhere: Where<RawContactsField>?,
    include: Include<RawContactsField>,
    where: Where<RawContactsField>?,
    orderBy: CompoundOrderBy<RawContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): AccountsRawContactsQuery.Result = query(
    if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
    include,
    // There may be RawContacts that are marked for deletion that have not yet been deleted.
    (RawContactsFields.Deleted notEqualTo true) and rawContactsWhere and where,
    sortOrder = "$orderBy LIMIT $limit OFFSET $offset"
) {
    val accountRawContactsMap = mutableMapOf<Account?, MutableList<BlankRawContact>>()
    val rawContactsList = mutableListOf<BlankRawContact>()

    val blankRawContactMapper = it.blankRawContactMapper()
    val rawContactsCursor = it.rawContactsCursor()

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

    AccountsRawContactsQueryResult(rawContactsList, accountRawContactsMap)

} ?: AccountsRawContactsQueryResult(emptyList(), emptyMap())

private class AccountsRawContactsQueryResult private constructor(
    rawContacts: List<BlankRawContact>,
    private val accountRawContactsMap: Map<Account?, List<BlankRawContact>>,
    override val isRedacted: Boolean
) : ArrayList<BlankRawContact>(rawContacts), AccountsRawContactsQuery.Result {

    constructor(
        rawContacts: List<BlankRawContact>,
        accountRawContactsMap: Map<Account?, List<BlankRawContact>>
    ) : this(rawContacts, accountRawContactsMap, false)

    override fun toString(): String =
        """
            AccountsRawContactsQuery.Result {
                Number of raw contacts found: $size
                First raw contact: ${firstOrNull()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): AccountsRawContactsQuery.Result =
        AccountsRawContactsQueryResult(
            redactedCopies(),
            accountRawContactsMap.entries.associate {
                it.key?.redactedCopy() to it.value.redactedCopies()
            },
            isRedacted = true
        )

    override fun rawContactsFor(account: Account?): List<BlankRawContact> =
        accountRawContactsMap.getOrElse(account) { emptyList() }
}