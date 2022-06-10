package contacts.core.blockednumbers

import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.BlockedNumber
import contacts.core.entities.mapper.blockedNumberMapper
import contacts.core.entities.table.Table
import contacts.core.util.isEmpty
import contacts.core.util.query
import contacts.core.util.unsafeLazy

/**
 * Queries on the Blocked Numbers table.
 *
 * ## Privileges
 *
 * Requires [BlockedNumbersPrivileges.canReadAndWrite]. All queries will return an empty result if
 * privileges are not acquired.
 *
 * ## Usage
 *
 * Here is an example query that returns the first 5 [BlockedNumber]s, skipping the first 2,
 * where the number contains "555", ordered by the number in ascending order.
 *
 * In Kotlin,
 *
 * ```kotlin
 * val blockedNumbers = blockedNumbersQuery
 *      .where { Number contains "555" }
 *      .orderBy(BlockedNumbersFields.Number.asc())
 *      .limit(5)
 *      .offset(2)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ```kotlin
 * import static contacts.core.BlockedNumbersFields.*;
 * import static contacts.core.WhereKt.*;
 * import static contacts.core.OrderByKt.*;
 *
 * val blockedNumbers = blockedNumbersQuery
 *      .where(contains(Number, "555"))
 *      .orderBy(asc(Number))
 *      .limit(5)
 *      .offset(2)
 *      .find()
 * ```
 */
interface BlockedNumbersQuery : CrudApi {

    /**
     * Filters the returned [BlockedNumber]s matching the criteria defined by the [where].
     * If not specified or null, then all [BlockedNumber]s are returned.
     *
     * Use [BlockedNumbersFields] to construct the [where].
     */
    fun where(where: Where<BlockedNumbersField>?): BlockedNumbersQuery

    /**
     * See [BlockedNumbersQuery.where]
     */
    fun where(where: BlockedNumbersFields.() -> Where<BlockedNumbersField>?): BlockedNumbersQuery

    /**
     * Orders the returned [BlockedNumber]s using one or more [orderBy]s. If not specified, then
     * blocked numbers are ordered by ID in ascending order.
     *
     * Use [BlockedNumbersFields] to construct the [orderBy].
     */
    @SafeVarargs
    fun orderBy(vararg orderBy: OrderBy<BlockedNumbersField>): BlockedNumbersQuery

    /**
     * See [BlockedNumbersQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<BlockedNumbersField>>): BlockedNumbersQuery

    /**
     * See [BlockedNumbersQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<BlockedNumbersField>>): BlockedNumbersQuery

    /**
     * See [BlockedNumbersQuery.orderBy].
     */
    fun orderBy(
        orderBy: BlockedNumbersFields.() -> Sequence<OrderBy<BlockedNumbersField>>
    ): BlockedNumbersQuery

    /**
     * Limits the maximum number of returned [BlockedNumber]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): BlockedNumbersQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): BlockedNumbersQuery

    /**
     * Returns the [Result] matching the preceding query options.
     *
     * ## Privileges
     *
     * Requires [BlockedNumbersPrivileges.canReadAndWrite]. Returns an empty result otherwise.
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
     * ## Privileges
     *
     * Requires [BlockedNumbersPrivileges.canReadAndWrite]. Returns an empty result otherwise.
     *
     * ## Cancellation
     *
     * The number of blocked number data found may take more than a few milliseconds to process.
     * Therefore, cancellation is supported while the blocked numbers list is being built.
     * To cancel at any time, the [cancel] function should return true.
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
    override fun redactedCopy(): BlockedNumbersQuery

    /**
     * A list of [BlockedNumber]s.
     *
     * ## The [toString] function
     *
     * The [toString] function of instances of this will not return the string representation of
     * every blocked number in the list. It will instead return a summary of the blocked numbers in
     * the list and perhaps the first [BlockedNumber] only.
     *
     * This is done due to the potentially large quantities of blocked numbers, which could block
     * the UI if not logging in background threads.
     *
     * You may print individual blocked numbers in this list by iterating through it.
     */
    interface Result : List<BlockedNumber>, CrudApi.Result {

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun BlockedNumbersQuery(contacts: Contacts): BlockedNumbersQuery =
    BlockedNumbersQueryImpl(contacts, BlockedNumbersPrivileges(contacts.applicationContext))

private class BlockedNumbersQueryImpl(
    override val contactsApi: Contacts,
    private val privileges: BlockedNumbersPrivileges,

    private var where: Where<BlockedNumbersField>? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy<BlockedNumbersField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,

    override val isRedacted: Boolean = false
) : BlockedNumbersQuery {

    override fun toString(): String =
        """
            BlockedNumbersQuery {
                where: $where
                orderBy: $orderBy
                limit: $limit
                offset: $offset
                hasPrivileges: ${privileges.canReadAndWrite()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersQuery = BlockedNumbersQueryImpl(
        contactsApi,
        privileges,

        // Redact search input.
        where?.redactedCopy(),
        orderBy,
        limit,
        offset,

        isRedacted = true
    )

    override fun where(where: Where<BlockedNumbersField>?): BlockedNumbersQuery = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = (where ?: DEFAULT_WHERE)?.redactedCopyOrThis(isRedacted)
    }

    override fun where(where: BlockedNumbersFields.() -> Where<BlockedNumbersField>?) =
        where(where(BlockedNumbersFields))

    override fun orderBy(vararg orderBy: OrderBy<BlockedNumbersField>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<BlockedNumbersField>>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<BlockedNumbersField>>): BlockedNumbersQuery =
        apply {
            this.orderBy = if (orderBy.isEmpty()) {
                DEFAULT_ORDER_BY
            } else {
                CompoundOrderBy(orderBy.toSet())
            }
        }

    override fun orderBy(
        orderBy: BlockedNumbersFields.() -> Sequence<OrderBy<BlockedNumbersField>>
    ) = orderBy(orderBy(BlockedNumbersFields))

    override fun limit(limit: Int): BlockedNumbersQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): BlockedNumbersQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): BlockedNumbersQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): BlockedNumbersQuery.Result {
        onPreExecute()

        return if (!privileges.canReadAndWrite()) {
            BlockedNumbersQueryResult(emptyList())
        } else {
            contentResolver.resolve(INCLUDE, where, orderBy, limit, offset, cancel)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    companion object {
        val INCLUDE by unsafeLazy { Include(BlockedNumbersFields) }
        val DEFAULT_WHERE: Where<BlockedNumbersField>? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(BlockedNumbersFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private fun ContentResolver.resolve(
    include: Include<BlockedNumbersField>,
    where: Where<BlockedNumbersField>?,
    orderBy: CompoundOrderBy<BlockedNumbersField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): BlockedNumbersQuery.Result = query(
    Table.BlockedNumbers,
    include,
    where,
    sortOrder = "$orderBy LIMIT $limit OFFSET $offset",
) {
    val blockedNumbersList = mutableListOf<BlockedNumber>()
    val blockedNumberMapper = it.blockedNumberMapper()

    while (!cancel() && it.moveToNext()) {
        blockedNumbersList.add(blockedNumberMapper.value)
    }

    // Ensure incomplete data sets are not returned.
    if (cancel()) {
        blockedNumbersList.clear()
    }

    BlockedNumbersQueryResult(blockedNumbersList)

} ?: BlockedNumbersQueryResult(emptyList())

private class BlockedNumbersQueryResult private constructor(
    blockedNumbers: List<BlockedNumber>,
    override val isRedacted: Boolean
) : ArrayList<BlockedNumber>(blockedNumbers), BlockedNumbersQuery.Result {

    constructor(blockedNumbers: List<BlockedNumber>) : this(blockedNumbers, false)

    override fun toString(): String =
        """
            BlockedNumbersQuery.Result {
                Number of blocked numbers found: $size
                First blocked number: ${firstOrNull()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersQuery.Result = BlockedNumbersQueryResult(
        redactedCopies(),
        isRedacted = true
    )
}