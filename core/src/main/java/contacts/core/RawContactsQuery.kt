package contacts.core

import android.accounts.Account
import contacts.core.entities.RawContact
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.mapper.ContactsMapper
import contacts.core.entities.table.Table
import contacts.core.util.*

/**
 * Queries for Profile OR non-Profile (depending on instance) RawContacts.
 *
 * To get Contacts instead, use [Query].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All queries will return an empty list if the permission is not granted.
 *
 * ## Usage
 *
 * Here is an example query that returns the first 5 [RawContact]s in the given account that has at
 * least one email, skipping the first 2, where the RawContact's display name starts with "a",
 * ordered by the display name in ascending order (ignoring case).
 *
 * In Kotlin,
 *
 * ```kotlin
 * val rawContacts = rawContactsQuery
 *      .whereRawContact(setOf(account), { DisplayNamePrimary startsWith "a" })
 *      .where { Email.Address.isNotNullOrEmpty() }
 *      .orderBy(RawContactsFields.DisplayName.asc())
 *      .limit(5)
 *      .offset(2)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ``java
 * ArrayList accounts = new ArrayList<Account>();
 * accounts.add(account);
 *
 * List<RawContact> rawContacts = rawContactsQuery
 *      .whereRawContact(accounts, startsWith(DisplayNamePrimary, "a"))
 *      .where(isNotNullOrEmpty(Fields.Email.Address))
 *      .orderBy(asc(RawContactsFields.DisplayName))
 *      .limit(5)
 *      .offset(2)
 *      .find();
 * ``
 */
interface RawContactsQuery : CrudApi {

    /**
     * Includes only the given set of [fields] in each of the matching raw contacts' data.
     *
     * If no fields are specified (empty list), then all fields are included. Otherwise, only
     * the specified fields will be included.
     *
     * When all fields are included in a query operation, all data properties are populated with
     * values from the database. Properties of fields that are included are not guaranteed to be
     * non-null because the database may actually have no data for the corresponding field.
     *
     * When only some fields are included, only those included data properties are populated with
     * values from the database. Properties of fields that are not included are guaranteed to be
     * null.
     *
     * To include RawContact specific properties, use [RawContactsQuery.includeRawContactsFields].
     *
     * ## Fields from [Fields.Contact] are ignored
     *
     * This query returns [RawContact]s and therefore does not process any Contacts fields.
     *
     * ## Performance
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     *
     * ## Including all fields
     *
     * If you want to include all fields, including custom data fields, then passing in an empty
     * list or not invoking this function is the most performant way to do it because internal
     * checks will be disabled (less lines of code executed).
     *
     * ## Developer notes
     *
     * Passing in an empty list here should set the reference to the internal field set to null to
     * indicate that include field checks should be disabled when processing cursor data via
     * implementations of [contacts.core.entities.cursor.AbstractEntityCursor].
     *
     * When the internal field set is set to null, all fields should be included in the projection
     * list of the actual query, which is why the [allFieldsIfNull] functions exist. In order to
     * disable include field checks in the cursors, [contacts.core.util.query] provides a parameter
     * to set the cursor holder's include fields to null.
     */
    fun include(vararg fields: AbstractDataField): RawContactsQuery

    /**
     * See [RawContactsQuery.include].
     */
    fun include(fields: Collection<AbstractDataField>): RawContactsQuery

    /**
     * See [RawContactsQuery.include].
     */
    fun include(fields: Sequence<AbstractDataField>): RawContactsQuery

    /**
     * See [RawContactsQuery.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): RawContactsQuery

    /**
     * Includes [fields] from the RawContacts table corresponding to
     * [contacts.core.entities.RawContact] properties.
     *
     * For all other fields/properties, use [include].
     *
     * If no RawContacts fields are specified (empty list), then all RawContacts fields are
     * included. Otherwise, only the specified fields will be included.
     *
     * When all fields are included in a query operation, all of the aforementioned properties of
     * RawContacts are populated with values from the database. Properties of fields that are
     * included are not guaranteed to be non-null because the database may actually have no data for
     * the corresponding field.
     *
     * When only some fields are included, only those included properties of RawContacts are
     * populated with values from the database. Properties of fields that are not included are
     * guaranteed to be null.
     *
     * ## Performance
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     *
     * #### Developer notes
     *
     * So, why not just add these fields to [DataRawContactsFields]?
     *
     * The reason is that [DataRawContactsFields], and everything in [Fields], are used for
     * **Data table** queries. Data table queries uses the
     * [android.provider.ContactsContract.DataColumnsWithJoins], which does not include
     * [android.provider.ContactsContract.SyncColumns] required to determine the
     * [contacts.core.entities.RawContact.account].
     *
     * Furthermore, the display name and options values in the returned rows reference the Contact's
     * values instead of the RawContact's values.
     *
     * Therefore, it made sense to make sure that [RawContactsFields] cannot be a part of a where
     * clause but can be included.
     *
     * ## Including all fields
     *
     * If you want to include all fields, including custom data fields, then passing in an empty
     * list or not invoking this function is the most performant way to do it because internal
     * checks will be disabled (less lines of code executed).
     *
     * #### Developer notes
     *
     * Passing in an empty list here should set the reference to the internal field set to null to
     * indicate that include field checks should be disabled when processing cursor data via
     * implementations of [contacts.core.entities.cursor.AbstractEntityCursor].
     *
     * When the internal field set is set to null, all fields should be included in the projection
     * list of the actual query, which is why the [allFieldsIfNull] functions exist. In order to
     * disable include field checks in the cursors, [contacts.core.util.query] provides a parameter
     * to set the cursor holder's include fields to null.
     */
    fun includeRawContactsFields(vararg fields: RawContactsField): RawContactsQuery

    /**
     * See [RawContactsQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): RawContactsQuery

    /**
     * See [RawContactsQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): RawContactsQuery

    /**
     * See [RawContactsQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ): RawContactsQuery

    /**
     * Filters the returned [RawContact]s belonging to the given [accounts] and matching the
     * criteria defined by the RawContacts table [where].
     *
     * If [accounts] is empty, then all accounts are included in the search. If [where] is null,
     * then all RawContacts in the specified [accounts] are returned. This is the default behavior
     * if this function is not invoked.
     *
     * Use [RawContactsFields] to construct the [where].
     */
    fun rawContactsWhere(
        accounts: Collection<Account?>,
        where: Where<RawContactsField>?
    ): RawContactsQuery

    /* JVM signature clash when passing null for second parameter. Comment this out to support
       Java users being able to provide accounts with null where.
    /**
     * Same as [RawContactsQuery.rawContactsWhere] except you have direct access to all properties
     * of [RawContactsFields] in the function parameter. Use this to shorten your code.
     */
    fun rawContactsWhere(
        accounts: Collection<Account?>,
        where: (RawContactsFields.() -> (Where<RawContactsField>?))
    ): RawContactsQuery
     */

    /**
     * Filters the [RawContact]s matching the criteria defined by the [where] in the joined
     * data table. If not specified or null, then all [RawContact]s are returned.
     *
     * Use [Fields] to construct the [where].
     *
     * ## Performance
     *
     * This may require one or more additional queries, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     *
     * For every usage of the `and` operator where the left-hand-side and right-hand-side are
     * different data kinds, an internal database query is performed. This is due to the way the
     * Data table is structured in relation to Contacts. For example,
     *
     * ```kotlin
     * Email.Address.isNotNull() and Phone.Number.isNotNull() and Address.FormattedAddress.isNotNull()
     * ```
     *
     * The above will require two additional internal database queries in order to simplify the
     * query such that it can actually provide matching RawContacts.
     *
     * Using the `or` operator does not have this performance hit.
     */
    fun where(where: Where<AbstractDataField>?): RawContactsQuery

    /**
     * Same as [RawContactsQuery.where] except you have direct access to all properties of [Fields]
     * in the function parameter. Use this to shorten your code.
     */
    fun where(where: Fields.() -> Where<AbstractDataField>?): RawContactsQuery

    /**
     * Orders the returned [RawContact]s using one or more [orderBy]s. If not specified, then data
     * is ordered by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     *
     * Use [RawContactsFields] to construct the [orderBy].
     */
    @SafeVarargs
    fun orderBy(vararg orderBy: OrderBy<RawContactsField>): RawContactsQuery

    /**
     * See [RawContactsQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<RawContactsField>>): RawContactsQuery

    /**
     * See [RawContactsQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<RawContactsField>>): RawContactsQuery

    /**
     * See [RawContactsQuery.orderBy].
     */
    fun orderBy(
        orderBy: RawContactsFields.() -> Collection<OrderBy<RawContactsField>>
    ): RawContactsQuery

    /**
     * Limits the maximum number of returned [RawContact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): RawContactsQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): RawContactsQuery

    /**
     * If the [limit] and [offset] functions are not supported by the device's database query
     * operation, all entities will be returned. In such cases, the [Result.isLimitBreached] will
     * be true if the number of entities returned exceed the [limit].
     *
     * Setting [forceOffsetAndLimit] to true will ensure that the [offset] and [limit] will be
     * applied after performing the internal database query, before returning the result to the
     * caller (you).
     *
     * This defaults to true in order to seamlessly support pagination. However, it is recommended
     * to set this to false and handle such cases yourself to prevent performing more than one query
     * for devices that do not support pagination.
     *
     * For the full set of devices that do not support pagination, visit this discussion;
     * https://github.com/vestrel00/contacts-android/discussions/242#discussioncomment-3337613
     *
     * ### Limitation
     *
     * If the number of entities found do not exceed the [limit] but an [offset] is provided, this
     * is unable to detect/handle events where the [offset] is not supported. Sorry :P
     */
    fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): RawContactsQuery

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
    override fun redactedCopy(): RawContactsQuery

    /**
     * The combined list of [RawContact]s from the specified Accounts ordered by [orderBy].
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
    interface Result : List<RawContact>, CrudApi.QueryResultWithLimit {

        /**
         * The list of [RawContact]s from the specified [account] ordered by [orderBy].
         *
         * The [offset] and [limit] functions DOES NOT apply to this list.
         */
        fun rawContactsFor(account: Account?): List<RawContact>

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun RawContactsQuery(
    contacts: Contacts, isProfile: Boolean
): RawContactsQuery = RawContactsQueryImpl(contacts, isProfile)

private class RawContactsQueryImpl(
    override val contactsApi: Contacts,
    private val isProfile: Boolean,

    private var include: Include<AbstractDataField>? = null,
    private var includeRawContactsFields: Include<RawContactsField>? = null,
    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var where: Where<AbstractDataField>? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy<RawContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,
    private var forceOffsetAndLimit: Boolean = DEFAULT_FORCE_OFFSET_AND_LIMIT,

    override val isRedacted: Boolean = false
) : RawContactsQuery {

    override fun toString(): String =
        """
            RawContactsQuery {
                isProfile: $isProfile
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                rawContactsWhere: $rawContactsWhere
                where: $where
                orderBy: $orderBy
                limit: $limit
                offset: $offset
                forceOffsetAndLimit: $forceOffsetAndLimit
                hasPermission: ${permissions.canQuery()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): RawContactsQuery = RawContactsQueryImpl(
        contactsApi, isProfile,
        include,
        includeRawContactsFields,
        // Redact account info.
        rawContactsWhere?.redactedCopy(),
        // Redact search input
        where?.redactedCopy(),
        orderBy,
        limit,
        offset,
        forceOffsetAndLimit,

        isRedacted = true
    )

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): RawContactsQuery = apply {
        include = if (fields.isEmpty()) {
            null // Set to null to disable include field checks, for optimization purposes.
        } else {
            Include(fields + REQUIRED_INCLUDE_FIELDS)
        }
    }

    override fun include(fields: Fields.() -> Collection<AbstractDataField>) =
        include(fields(Fields))

    override fun includeRawContactsFields(vararg fields: RawContactsField) =
        includeRawContactsFields(fields.asSequence())

    override fun includeRawContactsFields(fields: Collection<RawContactsField>) =
        includeRawContactsFields(fields.asSequence())

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): RawContactsQuery =
        apply {
            includeRawContactsFields = if (fields.isEmpty()) {
                null // Set to null to disable include field checks, for optimization purposes.
            } else {
                Include(fields + REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS)
            }
        }

    override fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ) = includeRawContactsFields(fields(RawContactsFields))

    override fun rawContactsWhere(
        accounts: Collection<Account?>,
        where: Where<RawContactsField>?
    ): RawContactsQuery = apply {
        val accountsWhere = accounts.toRawContactsWhere()

        // I know static analysis checks here detect "Condition 'xxx' is always true when reached.
        // However, this is more readable and explicit IMO so we'll keep it this way =)
        rawContactsWhere = if (accountsWhere != null && where != null) {
            accountsWhere and where
        } else if (accountsWhere != null && where == null) {
            accountsWhere
        } else if (accountsWhere == null && where != null) {
            where
        } else {
            // Yes, I know DEFAULT_RAW_CONTACTS_WHERE is null. This reads better though.
            DEFAULT_RAW_CONTACTS_WHERE
        }
            ?.redactedCopyOrThis(isRedacted)
    }

    /*
    override fun rawContactsWhere(
        accounts: Collection<Account?>,
        where: RawContactsFields.() -> Where<RawContactsField>?
    ) = rawContactsWhere(accounts, where(RawContactsFields))
     */

    override fun where(where: Where<AbstractDataField>?): RawContactsQuery = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = (where ?: DEFAULT_WHERE)?.redactedCopyOrThis(isRedacted)
    }

    override fun where(where: Fields.() -> Where<AbstractDataField>?) = where(where(Fields))

    override fun orderBy(vararg orderBy: OrderBy<RawContactsField>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<RawContactsField>>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<RawContactsField>>): RawContactsQuery =
        apply {
            this.orderBy = if (orderBy.isEmpty()) {
                DEFAULT_ORDER_BY
            } else {
                CompoundOrderBy(orderBy.toSet())
            }
        }

    override fun orderBy(orderBy: RawContactsFields.() -> Collection<OrderBy<RawContactsField>>) =
        orderBy(orderBy(RawContactsFields))

    override fun limit(limit: Int): RawContactsQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): RawContactsQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): RawContactsQuery =
        apply {
            this.forceOffsetAndLimit = forceOffsetAndLimit
        }

    override fun find(): RawContactsQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): RawContactsQuery.Result {
        onPreExecute()

        var rawContacts = if (!permissions.canQuery()) {
            RawContactsQueryResult(emptyList(), isLimitBreached = false)
        } else {
            contactsApi.resolve(
                isProfile,
                customDataRegistry,
                include, includeRawContactsFields,
                rawContactsWhere, where,
                orderBy, limit, offset,
                cancel
            )
        }

        val isLimitBreached = rawContacts.size > limit
        if (isLimitBreached && forceOffsetAndLimit) {
            rawContacts = rawContacts.offsetAndLimit(offset, limit)
        }

        return RawContactsQueryResult(rawContacts, isLimitBreached)
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val REQUIRED_INCLUDE_FIELDS by lazy { Fields.Required.all.asSequence() }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by lazy {
            RawContactsFields.Required.all.asSequence()
        }
        val DEFAULT_WHERE: Where<AbstractDataField>? = null
        val DEFAULT_ORDER_BY by lazy { CompoundOrderBy(setOf(RawContactsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
        const val DEFAULT_FORCE_OFFSET_AND_LIMIT = true
    }
}

private fun Contacts.resolve(
    isProfile: Boolean,
    customDataRegistry: CustomDataRegistry,
    include: Include<AbstractDataField>?,
    includeRawContactsFields: Include<RawContactsField>?,
    rawContactsWhere: Where<RawContactsField>?,
    where: Where<AbstractDataField>?,
    orderBy: CompoundOrderBy<RawContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<RawContact> {

    var rawContactIds: MutableSet<Long>? = null

    // Get RawContact Ids matching where from the Data table. If where is null, skip.
    if (where != null && !cancel()) {
        rawContactIds = mutableSetOf<Long>().apply {
            val reducedWhere = reduceDataTableWhereForMatchingRawContactIds(where, cancel)
            addAll(findRawContactIdsInDataTable(reducedWhere, cancel))
        }

        // Get the RawContacts Ids of blank RawContacts matching the where from the RawContacts
        // table.
        val rawContactsTableWhere = where.toRawContactsTableWhere()
        if (rawContactsTableWhere != null) {
            // We do not actually need to suppress DB exceptions anymore because we are making
            // sure that only RawContacts fields are in rawContactsTableWhere. However, it
            // does not hurt to be extra safe... though this will mask programming errors in
            // toRawContactsTableWhere by not crashing. Unit tests should cover this though!
            rawContactIds.addAll(
                findRawContactIdsInRawContactsTable(rawContactsTableWhere, true, cancel)
            )
        }

        // If no match, return empty list.
        if (rawContactIds.isEmpty() || cancel()) {
            return emptyList()
        }
    }

    // Get the RawContact Ids matching rawContactsWhere and contained in the rawContactIds from the
    // RawContacts table. If rawContactsWhere is null, skip.
    if (rawContactsWhere != null && !cancel()) {
        val rawContactsTableWhere = rawContactsWhere and rawContactIds?.let {
            RawContactsFields.Id `in` it
        }

        // Intentionally replace the rawContactsIds instead of adding to it.
        rawContactIds = mutableSetOf<Long>().apply {
            addAll(findRawContactIdsInRawContactsTable(rawContactsTableWhere, false, cancel))
        }

        // If no match, return empty list.
        if (rawContactIds.isEmpty() || cancel()) {
            return emptyList()
        }
    }

    return resolve(
        isProfile,
        customDataRegistry,
        rawContactIds,
        include, includeRawContactsFields,
        orderBy, limit, offset,
        cancel
    )
}

private fun Contacts.resolve(
    isProfile: Boolean,
    customDataRegistry: CustomDataRegistry,
    rawContactIds: MutableSet<Long>?,
    include: Include<AbstractDataField>?,
    includeRawContactsFields: Include<RawContactsField>?,
    orderBy: CompoundOrderBy<RawContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<RawContact> {
    if (cancel() || (rawContactIds != null && rawContactIds.isEmpty())) {
        return emptyList()
    }

    var offsetAndLimitedRawContactIds: Collection<Long>? = rawContactIds

    // Collect RawContacts and Data with this mapper.
    val contactsMapper = ContactsMapper(customDataRegistry, cancel)

    // Collect RawContacts. If rawContactIds is null, then all RawContacts are collected.
    contentResolver.query(
        rawContactsUri(isProfile),
        includeRawContactsFields.allFieldsIfNull(),
        (RawContactsFields.Deleted notEqualTo true) and rawContactIds?.let {
            RawContactsFields.Id `in` it
        },
        sortOrder = "$orderBy LIMIT $limit OFFSET $offset",
        // Ignore include field checks if includeRawContactsFields is null.
        setCursorHolderIncludeFieldsToNull = includeRawContactsFields == null,
        processCursor = {
            contactsMapper.processRawContactsCursor(it)
            // We need to make sure we only use the raw contact ids after this call, which have been
            // trimmed by the offset and limit.
            offsetAndLimitedRawContactIds = contactsMapper.rawContactIds
        }
    )

    if (cancel()) {
        return emptyList()
    }

    // This redeclaration is just here to get rid of compiler not being able to smart cast
    // offsetAndLimitedRawContactIds as non-null because it is a var.
    val finalOffsetAndLimitedRawContactIds = offsetAndLimitedRawContactIds

    val finalInclude = include.allFieldsIfNull(this)

    // Skip querying the Data table if there are no data fields included.
    if (finalInclude.containsAtLeastOneDataField) {
        // Collect Data. If finalOffsetAndLimitedRawContactIds is null, then all Data are collected.
        query(
            Table.Data, finalInclude, finalOffsetAndLimitedRawContactIds?.let {
                Fields.RawContact.Id `in` it
            },
            // Ignore include field checks if include is null.
            setCursorHolderIncludeFieldsToNull = include == null,
            processCursor = contactsMapper::processDataCursor
        )
    }

    // Output all collected RawContacts and Data.
    return if (cancel()) emptyList() else contactsMapper.mapRawContacts()
}

private class RawContactsQueryResult private constructor(
    rawContacts: List<RawContact>,
    override val isLimitBreached: Boolean,
    override val isRedacted: Boolean
) : ArrayList<RawContact>(rawContacts), RawContactsQuery.Result {

    constructor(rawContacts: List<RawContact>, isLimitBreached: Boolean) : this(
        rawContacts = rawContacts,
        isLimitBreached = isLimitBreached,
        isRedacted = false
    )

    override fun toString(): String =
        """
            RawContactsQuery.Result {
                Number of raw contacts found: $size
                First raw contact: ${firstOrNull()}
                isLimitBreached: $isLimitBreached
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): RawContactsQuery.Result =
        RawContactsQueryResult(
            rawContacts = redactedCopies(),
            isLimitBreached = isLimitBreached,
            isRedacted = true
        )

    override fun rawContactsFor(account: Account?): List<RawContact> =
        filter { it.account == account }
}