package contacts.core

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.entities.Contact
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.mapper.ContactsMapper
import contacts.core.entities.table.Table
import contacts.core.util.*

/**
 * Queries the Contacts Provider tables and returns a list of contacts matching a specific search
 * criteria. All RawContacts of matching Contacts are included in the resulting Contact instances.
 *
 * This provides a great deal of granularity and customizations when providing matching criteria
 * via [where]. For a broader, and more AOSP Contacts app like query, use [BroadQuery].
 *
 * To get RawContacts directly, use [RawContactsQuery].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. If not granted, the query will do nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example query that returns the first 10 [Contact]s, skipping the first 5, where the
 * contact's name starts with "john" and has an email ending with "gmail", order by favorite/starred
 * status such that favorite/starred contacts appear first in the list AND order by display name
 * primary in ascending order (from a to z ignoring case). Include only Contacts with at least one
 * RawContact belonging to the given account. Include only name and email properties of [Contact]s.
 *
 * In Kotlin,
 *
 * ```kotlin
 * val contacts = query
 *      .accounts(account)
 *      .include { Name.all + Address.all }
 *      .where { (Name.DisplayName startsWith "john") and (Email.Address endsWith "gmail") }
 *      .orderBy(
 *          ContactsFields.Options.Starred.desc(),
 *          ContactsFields.DisplayNamePrimary.asc()
 *      )
 *      .offset(5)
 *      .limit(10)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ```java
 * import static contacts.core.Fields.*;
 * import static contacts.core.WhereKt.*;
 * import static contacts.core.OrderByKt.*;
 *
 * List<Contact> contacts = query
 *      .accounts(account)
 *      .include(new ArrayList<>() {{
 *           addAll(Name.getAll());
 *           addAll(Address.getAll());
 *       }})
 *      .where(startsWith(Name.DisplayName, "john").and(endsWith(Email.Address, "gmail")))
 *      .orderBy(
 *          desc(ContactsFields.Options.Starred),
 *          asc(ContactsFields.DisplayNamePrimary.asc)
 *      )
 *      .offset(5)
 *      .limit(10)
 *      .find();
 * ```
 *
 * ## Groups Matching
 *
 * Unlike [BroadQuery.groups], this does not have a groups function. You may still match groups
 * (in a much flexible way) by using [Fields.GroupMembership] with [where].
 */
interface Query : CrudApi {

    /**
     * Limits the search to only those RawContacts associated with one of the given accounts.
     * Contacts returned may still contain RawContacts / data that belongs to other accounts not
     * specified in [accounts] because Contacts may be made up of more than one RawContact from
     * different Accounts. This is the same behavior as the AOSP Contacts app.
     *
     * If no accounts are specified (this function is not called or called with no Accounts), then
     * all RawContacts of Contacts are included in the search.
     *
     * A null [Account] may be provided here, which results in RawContacts with no associated
     * Account to be included in the search. RawContacts without an associated account are
     * considered local contacts or device-only contacts, which are not synced.
     *
     * ## Performance
     *
     * This may require one or more additional queries, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun accounts(vararg accounts: Account?): Query

    /**
     * See [Query.accounts]
     */
    fun accounts(accounts: Collection<Account?>): Query

    /**
     * See [Query.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): Query

    // No groups function here like in BroadQuery. Use [Fields.GroupMembership] with [where].

    /**
     * Includes only the given set of [fields] in each of the matching contacts.
     *
     * If no fields are specified, then all fields ([Fields.all]) are included. Otherwise, only the
     * specified fields will be included in addition to required API fields [Fields.Required]
     * (e.g. IDs), which are always included.
     *
     * When all fields are included in a query operation, all properties of Contacts and Data are
     * populated with values from the database. Properties of fields that are included are not
     * guaranteed to be non-null because the database may actually have no data for the
     * corresponding field.
     *
     * When only some fields are included, only those included properties of Contacts and Data are
     * populated with values from the database. Properties of fields that are not included are
     * guaranteed to be null.
     *
     * To include RawContact specific properties, use [Query.includeRawContactsFields].
     *
     * ## Performance
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     *
     * The most optimal queries only include fields from [Fields.Contact] because no Data table rows
     * need to be processed.
     */
    fun include(vararg fields: AbstractDataField): Query

    /**
     * See [Query.include].
     */
    fun include(fields: Collection<AbstractDataField>): Query

    /**
     * See [Query.include].
     */
    fun include(fields: Sequence<AbstractDataField>): Query

    /**
     * See [Query.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): Query

    /**
     * Includes [fields] from the RawContacts table corresponding to the following RawContacts
     * properties;
     *
     * - [contacts.core.entities.RawContact.displayNamePrimary]
     * - [contacts.core.entities.RawContact.displayNameAlt]
     * - [contacts.core.entities.RawContact.account]
     * - [contacts.core.entities.RawContact.options]
     *
     * For all other fields/properties, use [include].
     *
     * If no fields are specified, then all RawContacts fields ([RawContactsFields.all]) are
     * included. Otherwise, only the specified fields will be included in addition to required API
     * fields [RawContactsFields.Required].
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
     * ## Developer notes
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
     */
    fun includeRawContactsFields(vararg fields: RawContactsField): Query

    /**
     * See [Query.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): Query

    /**
     * See [Query.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): Query

    /**
     * See [Query.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: RawContactsFields.() -> Collection<RawContactsField>): Query

    /**
     * Filters the [Contact]s matching the criteria defined by the [where] in the joined data table.
     * If not specified or null, then all [Contact]s are returned.
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
     * query such that it can actually provide matching Contacts.
     *
     * Using the `or` operator does not have this performance hit.
     *
     * ## Blank Contacts
     *
     * This where clause is only used to query the joined Data table. Some Contacts do not have any
     * Data table rows (they are blank). This library exposes some fields that belong to other
     * tables, accessible via the joined Data table;
     *
     * - [Fields.Contact]
     * - [Fields.RawContact]
     *
     * Using these fields in the where clause does not have any effect in matching blank Contacts
     * or RawContacts simply because they have no Data rows containing these joined fields.
     *
     * #### Limitations
     *
     * Blank RawContacts and blank Contacts do not have any rows in the Data table so a [where]
     * clause that uses any fields from the Data table [Fields] will **exclude** blanks in the
     * result (even if they are OR 'ed). There are some joined fields that can be used to match
     * blanks **as long as no other fields are in the where clause**;
     *
     * - [Fields.Contact] enables matching blank Contacts. The result will include all RawContact(s)
     *   belonging to the Contact(s), including blank(s). Examples;
     *      - `Fields.Contact.Id equalTo 5`
     *      - `Fields.Contact.Id in listOf(1,2,3) and Fields.Contact.DisplayNamePrimary contains "a"`
     *      - `Fields.Contact.Options.Starred equalTo true`
     *
     * - [Fields.RawContact] enables matching blank RawContacts. The result will include all
     *   Contact(s) these belong to, including sibling RawContacts (blank and not blank). Examples;
     *
     *   - `Fields.RawContact.Id equalTo 5`
     *   - `Fields.RawContact.Id notIn listOf(1,2,3)`
     *
     * Blanks will not be included in the results even if they technically should **if** joined
     * fields from other tables are in the [where]. In the below example, matching the `Contact.Id`
     * to an existing blank Contact with Id of 5 will yield no results because it is joined by
     * [Fields.Email], which is not a part of [Fields.Contact]. It should technically return the
     * blank Contact with Id of 5 because the OR operator is used. However, because we internally
     * need to query the Contacts table to match the blanks, a DB exception will be thrown by the
     * Contacts Provider because `Fields.Email.Address` ("data1" and "mimetype") are columns from
     * the Data table that do not exist in the Contacts table. The same applies to the
     * [Fields.RawContact].
     *
     * - `Fields.Contact.Id equalTo 5 OR (Fields.Email.Address.isNotNull())`
     * - `Fields.RawContact.Id ... OR (Fields.Phone.Number...)`
     */
    fun where(where: Where<AbstractDataField>?): Query

    /**
     * Same as [Query.where] except you have direct access to all properties of [Fields] in the
     * function parameter. Use this to shorten your code.
     */
    fun where(where: Fields.() -> Where<AbstractDataField>?): Query

    /**
     * Orders the returned [Contact]s using one or more [orderBy]s. If not specified, then contacts
     * are ordered by ID in ascending order.
     *
     * This will throw an [ContactsException] if ordering by a field that is not included in
     * the query.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     *
     * Use [ContactsFields] to construct the [orderBy].
     *
     * If you need to sort a collection of [Contact]s retrieved from this query using any field
     * from [Fields], use the ContactsComparator extension functions.
     *
     * ## Developer Notes
     *
     * This API DOES NOT support ordering by Data table columns ([Fields]). It used to support it
     * but it has been removed for optimization purposes. This now only supports ordering by
     * Contacts table fields ([ContactsFields]).
     *
     * **If this supported ordering by Data table fields**, then it would be unable to use the
     * ORDER BY, LIMIT, and OFFSET functions of a raw database query and custom ordering via
     * Comparators is required.
     *
     * The Data table uses generic column names (e.g. data1, data2, ...) using the column 'mimetype'
     * to distinguish the type of data in that generic column. For example, the column name of
     * [NameFields.DisplayName] is the same as [AddressFields.FormattedAddress], which is 'data1'.
     * This means that if you order by the display name, you are also ordering by the formatted
     * address and all other columns whose value is 'data1'. This API could work around this
     * limitation by performing the ordering, limiting, and offsetting manually after
     * **all matching** contacts have been retrieved before returning it to the consumer.
     *
     * Note that there is no workaround for the [include] function because the
     * [ContentResolver.query] function only takes in an array of column names. This means that
     * including [NameFields.DisplayName] ('data1') will also result in the inclusion of
     * [AddressFields.FormattedAddress] ('data1').
     */
    @SafeVarargs
    fun orderBy(vararg orderBy: OrderBy<ContactsField>): Query

    /**
     * See [Query.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<ContactsField>>): Query

    /**
     * See [Query.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): Query

    /**
     * See [Query.orderBy].
     */
    fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>): Query

    /**
     * Limits the maximum number of returned [Contact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): Query

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): Query

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
    fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): Query

    /**
     * Returns a list of [Contact]s matching the preceding query options.
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
     * Returns a list of [Contact]s matching the preceding query options.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION].
     *
     * ## Cancellation
     *
     * The number of contacts and contact data found and processed may be large, which results
     * in this operation to take a while. Therefore, cancellation is supported while the contacts
     * list is being built. To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **An empty list will be returned if cancelled.**
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
    override fun redactedCopy(): Query

    /**
     * A list of [Contact]s.
     *
     * ## The [toString] function
     *
     * The [toString] function of instances of this will not return the string representation of
     * every contact in the list. It will instead return a summary of the contacts in the list and
     * perhaps the first contact only.
     *
     * This is done due to the potentially large quantities of contacts and entities within each
     * contact, which could block the UI if not logging in background threads.
     *
     * You may print individual contacts in this list by iterating through it.
     */
    // I know that this interface also exist in BroadQuery but I want each API to have its own
    // interface for the results in case we need to deviate implementation. Besides, this is the
    // only pair of APIs in the library that have the same name for its results interface.
    interface Result : List<Contact>, CrudApi.QueryResultWithLimit {

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun Query(contacts: Contacts): Query = QueryImpl(contacts)

private class QueryImpl(
    override val contactsApi: Contacts,

    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include<AbstractDataField> = contactsApi.includeAllFields(),
    private var includeRawContactsFields: Include<RawContactsField> = DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS,
    private var where: Where<AbstractDataField>? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy<ContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,
    private var forceOffsetAndLimit: Boolean = DEFAULT_FORCE_OFFSET_AND_LIMIT,

    override val isRedacted: Boolean = false
) : Query {

    override fun toString(): String =
        """
            Query {
                rawContactsWhere: $rawContactsWhere
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                where: $where
                orderBy: $orderBy
                limit: $limit
                offset: $offset
                forceOffsetAndLimit: $forceOffsetAndLimit
                hasPermission: ${permissions.canQuery()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Query = QueryImpl(
        contactsApi,

        // Redact Account information.
        rawContactsWhere?.redactedCopy(),
        include,
        includeRawContactsFields,
        // Redact search input.
        where?.redactedCopy(),
        orderBy,
        limit,
        offset,
        forceOffsetAndLimit,

        isRedacted = true
    )

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): Query = apply {
        rawContactsWhere = accounts.toRawContactsWhere()?.redactedCopyOrThis(isRedacted)
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): Query = apply {
        include = if (fields.isEmpty()) {
            contactsApi.includeAllFields()
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

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): Query = apply {
        includeRawContactsFields = if (fields.isEmpty()) {
            DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS
        } else {
            Include(fields + REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS)
        }
    }

    override fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ) = includeRawContactsFields(fields(RawContactsFields))

    override fun where(where: Where<AbstractDataField>?): Query = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = (where ?: DEFAULT_WHERE)?.redactedCopyOrThis(isRedacted)
    }

    override fun where(where: Fields.() -> Where<AbstractDataField>?) = where(where(Fields))

    override fun orderBy(vararg orderBy: OrderBy<ContactsField>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): Query = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy(ContactsFields))

    override fun limit(limit: Int): Query = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): Query = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): Query = apply {
        this.forceOffsetAndLimit = forceOffsetAndLimit
    }

    override fun find(): Query.Result = find { false }

    override fun find(cancel: () -> Boolean): Query.Result {
        onPreExecute()

        var contacts = if (!permissions.canQuery() || cancel()) {
            emptyList()
        } else {
            // Invoke the function to ensure that delegators (e.g. in tests) get access to the private
            // attributes even if the consumer does not call these functions. This allows delegators to
            // make necessary modifications to private attributes without having to make this class
            // open for inheritance or exposing unnecessary attributes to consumers.
            include(include.fields)
            where(where)

            contentResolver.resolve(
                customDataRegistry,
                include, includeRawContactsFields,
                rawContactsWhere, where,
                orderBy, limit, offset,
                cancel
            )
        }

        val isLimitBreached = contacts.size > limit
        if (isLimitBreached && forceOffsetAndLimit) {
            contacts = contacts.offsetAndLimit(offset, limit)
        }

        return QueryResult(contacts, isLimitBreached)
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS by unsafeLazy { Include(RawContactsFields.all) }
        val REQUIRED_INCLUDE_FIELDS by unsafeLazy { Fields.Required.all.asSequence() }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by unsafeLazy {
            RawContactsFields.Required.all.asSequence()
        }
        val DEFAULT_WHERE: Where<AbstractDataField>? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(ContactsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
        const val DEFAULT_FORCE_OFFSET_AND_LIMIT = true
    }
}

private fun ContentResolver.resolve(
    customDataRegistry: CustomDataRegistry,
    include: Include<AbstractDataField>,
    includeRawContactsFields: Include<RawContactsField>,
    rawContactsWhere: Where<RawContactsField>?,
    where: Where<AbstractDataField>?,
    orderBy: CompoundOrderBy<ContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<Contact> {

    var contactIds: MutableSet<Long>? = null

    // Get Contact Ids matching where from the Data table. If where is null, skip.
    if (where != null && !cancel()) {
        contactIds = mutableSetOf<Long>().apply {
            val reducedWhere = reduceDataTableWhereForMatchingContactIds(where, cancel)
            addAll(findContactIdsInDataTable(reducedWhere, cancel))
        }

        // Get the RawContacts Ids of blank RawContacts matching the where from the RawContacts
        // table.
        val rawContactsTableWhere = where.toRawContactsTableWhere()
        if (rawContactsTableWhere != null) {
            // We do not actually need to suppress DB exceptions anymore because we are making
            // sure that only RawContacts fields are in rawContactsTableWhere. However, it
            // does not hurt to be extra safe... though this will mask programming errors in
            // toRawContactsTableWhere by not crashing. Unit tests should cover this though!
            contactIds.addAll(
                findContactIdsInRawContactsTable(rawContactsTableWhere, true, cancel)
            )
        }

        // Get the Contacts Ids of blank Contacts matching the where from the Contacts table.
        val contactsTableWhere = where.toContactsTableWhere()
        if (contactsTableWhere != null) {
            // We do not actually need to suppress DB exceptions anymore because we are making
            // sure that only Contacts fields are in contactsTableWhere. However, it does not
            // hurt to be extra safe... though this will mask programming errors in
            // toContactsTableWhere by not crashing. Unit tests should cover this though!
            contactIds.addAll(
                findContactIdsInContactsTable(contactsTableWhere, true, cancel)
            )
        }

        // If no match, return empty list.
        if (contactIds.isEmpty() || cancel()) {
            return emptyList()
        }
    }

    // Get the Contact Ids matching rawContactsWhere and contained in the contactIds from the
    // RawContacts table. If rawContactsWhere is null, skip.
    if (rawContactsWhere != null && !cancel()) {
        val rawContactsTableWhere = rawContactsWhere and contactIds?.let {
            RawContactsFields.ContactId `in` it
        }

        // Intentionally replace the contactsIds instead of adding to it.
        contactIds = mutableSetOf<Long>().apply {
            addAll(findContactIdsInRawContactsTable(rawContactsTableWhere, false, cancel))
        }

        // If no match, return empty list.
        if (contactIds.isEmpty() || cancel()) {
            return emptyList()
        }
    }

    return resolve(
        customDataRegistry,
        contactIds,
        include, includeRawContactsFields,
        orderBy, limit, offset,
        cancel
    )
}

internal fun ContentResolver.resolve(
    customDataRegistry: CustomDataRegistry,
    contactIds: MutableSet<Long>?,
    include: Include<AbstractDataField>,
    includeRawContactsFields: Include<RawContactsField>,
    orderBy: CompoundOrderBy<ContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<Contact> {

    if (cancel() || (contactIds != null && contactIds.isEmpty())) {
        return emptyList()
    }

    var offsetAndLimitedContactIds: Collection<Long>? = contactIds

    // Collect Contacts, RawContacts, and Data with this mapper.
    val contactsMapper = ContactsMapper(customDataRegistry, cancel)

    // Collect Contacts. If contactIds is null, then all Contacts are collected.
    query(
        Table.Contacts, include.onlyContactsFields(), contactIds?.let {
            ContactsFields.Id `in` it
        },
        sortOrder = "$orderBy LIMIT $limit OFFSET $offset",
        processCursor = {
            contactsMapper.processContactsCursor(it)
            // We need to make sure we only use the contact ids after this call, which have been
            // trimmed by the offset and limit.
            offsetAndLimitedContactIds = contactsMapper.contactIds
        }
    )

    if (cancel()) {
        return emptyList()
    }

    // This redeclaration is just here to get rid of compiler not being able to smart cast
    // offsetAndLimitedContactIds as non-null because it is a var.
    val finalOffsetAndLimitedContactIds = offsetAndLimitedContactIds

    // Collect RawContacts.
    query(
        Table.RawContacts, includeRawContactsFields,
        // There may be RawContacts that are marked for deletion that have not yet been deleted.
        (RawContactsFields.Deleted notEqualTo true)
            .and(
                finalOffsetAndLimitedContactIds?.let {
                    RawContactsFields.ContactId `in` finalOffsetAndLimitedContactIds
                }
            ),
        processCursor = contactsMapper::processRawContactsCursor
    )

    if (cancel()) {
        return emptyList()
    }

    // Skip querying the Data table if there are no data fields included.
    if (include.containsAtLeastOneDataField) {
        // Collect Data. If finalOffsetAndLimitedContactIds is null, then all Data are collected.
        query(
            Table.Data, include, finalOffsetAndLimitedContactIds?.let {
                Fields.Contact.Id `in` it
            },
            processCursor = contactsMapper::processDataCursor
        )
    }

    // Output all collected Contacts, RawContacts, and Data.
    return if (cancel()) emptyList() else contactsMapper.mapContacts()
}

private class QueryResult private constructor(
    contacts: List<Contact>,
    override val isLimitBreached: Boolean,
    override val isRedacted: Boolean
) : ArrayList<Contact>(contacts), Query.Result {

    constructor(contacts: List<Contact>, isLimitBreached: Boolean) : this(
        contacts = contacts,
        isLimitBreached = isLimitBreached,
        isRedacted = false
    )

    override fun toString(): String =
        """
            Query.Result {
                Number of contacts found: $size
                First contact: ${firstOrNull()}
                isLimitBreached: $isLimitBreached
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Query.Result = QueryResult(
        contacts = redactedCopies(),
        isLimitBreached = isLimitBreached,
        isRedacted = true
    )
}