package com.vestrel00.contacts

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.mapper.ContactsMapper
import com.vestrel00.contacts.entities.table.Table
import kotlin.math.min

/**
 * Queries the Contacts data table and returns one or more contacts matching the search criteria.
 *
 * To query specific types of data (e.g. emails, phones, etc), use [QueryData].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All queries will return an empty list or null result if the permission
 * is not granted.
 *
 * ## Usage
 *
 * Here is an example query that returns the first 10 [Contact]s, skipping the first 5, where the
 * contact's name starts with "john" and has an email ending with "gmail", ordered by the name in
 * ascending order (not ignoring case) and email (ignoring case) in descending order respectively.
 * Only Contacts with at least one RawContact belonging to the given account are included. Only the
 * full name and email address attributes of the [Contact] objects are included.
 *
 * In Kotlin,
 *
 * ```kotlin
 * import com.vestrel00.contacts.Fields.Name
 * import com.vestrel00.contacts.Fields.Address
 *
 * val contacts : List<Contact> = query.
 *      .accounts(account)
 *      .include(Name, Address)
 *      .where((Name.DisplayName startsWith "john") and (Email.Address endsWith "gmail"))
 *      .orderBy(Name.DisplayName.asc(), Email.Address.desc(true))
 *      .offset(5)
 *      .limit(10)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ```java
 * import static com.vestrel00.contacts.Fields.*;
 * import static com.vestrel00.contacts.WhereKt.*;
 * import static com.vestrel00.contacts.OrderByKt.*;
 *
 * List<Contact> contacts = query
 *      .accounts(account)
 *      .include(Name, Address)
 *      .where(startsWith(Name.DisplayName, "john").and(endsWith(Email.Address, "gmail")))
 *      .orderBy(asc(Name.DisplayName), desc(Email.Address, true))
 *      .offset(5)
 *      .limit(10)
 *      .find();
 * ```
 *
 * ## Note
 *
 * All functions here are safe to call in the Main / UI thread EXCEPT for the [find] and [findFirst]
 * functions, which should be called in a worker thread in order to prevent blocking the UI.
 *
 * ## Developer Notes
 *
 * Unlike [QueryData], this API is unable to use the ORDER BY, LIMIT, and OFFSET functions of a raw
 * database query. The Android contacts **data table** uses generic column names (e.g. data1, data2,
 * ...) using the column 'mimetype' to distinguish the type of data in that generic column. For
 * example, the column name of [NameFields.DisplayName] is the same as
 * [AddressFields.FormattedAddress], which is 'data1'. This means that if you order by the display
 * name, you are also ordering by the formatted address and all other columns whose value is
 * 'data1'. This API works around this limitation by performing the ordering, limiting, and
 * offsetting manually after the contacts have been retrieved before returning it to the consumer.
 * Note that there is no workaround for the [include] function because the [ContentResolver.query]
 * function only takes in an array of column names.
 *
 * Each row in the data table consists of a piece of contact data (e.g. a phone number), its
 * mimetype, and the associated contact id. A row does not contain all of the data for a contact.
 * A contact in the **data table** may have 1 or more entries. Combined with generic column names,
 * this makes using the ORDER BY, LIMIT, and OFFSET functions of a raw database query impossible.
 *
 * With that said, Kotlin's Flow or reactive frameworks like RxJava and Java 8 Streams cannot really
 * be supported. We cannot emit complete contact instances and still honor ORDER BY, LIMIT, and
 * OFFSET functions. Although, it is possible for [QueryData] as queries are limited to a single
 * mimetype.
 */
interface Query {

    /**
     * Limits the search to only those RawContacts associated with the given accounts. Contacts
     * returned may still contain data that belongs to other accounts not specified in [accounts].
     * This follows the native Contacts app behavior.
     *
     * If no accounts are specified, then all RawContacts of Contacts are included in the search.
     */
    fun accounts(vararg accounts: Account): Query

    /**
     * See [Query.accounts]
     */
    fun accounts(accounts: Collection<Account>): Query

    /**
     * See [Query.accounts]
     */
    fun accounts(accounts: Sequence<Account>): Query

    /**
     * Includes the given set of [fields] in the resulting contact object(s).
     *
     * If no fields are specified, then all fields are included. Otherwise, only the specified
     * fields will be included in addition to the required fields, which are always included.
     *
     * Fields that are included will not guarantee non-null attributes in the returned contact
     * object instances.
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     *
     * Note that the Android contacts **data table** uses generic column names (e.g. data1, data2,
     * ...) using the column 'mimetype' to distinguish the type of data in that generic column. For
     * example, the column name of [NameFields.DisplayName] is the same as
     * [AddressFields.FormattedAddress], which is 'data1'. This means that
     * [AddressFields.FormattedAddress] is also included when [NameFields.DisplayName] is included.
     * There is no workaround for this because the [ContentResolver.query] function only takes in
     * an array of column names.
     *
     * ## IMPORTANT!!!
     *
     * Do not perform updates on contacts returned by a query where all fields are not included as
     * it will result in data loss!!
     */
    fun include(vararg fields: Field): Query

    /**
     * See [Query.include].
     */
    fun include(fields: Collection<Field>): Query

    /**
     * See [Query.include].
     */
    fun include(fields: Sequence<Field>): Query

    /**
     * Filters the returned [Contact]s matching the criteria defined by the [where].
     *
     * If not specified or null, then all [Contact]s are returned, limited by [limit].
     */
    fun where(where: Where?): Query

    /**
     * Orders the returned [Contact]s using one or more [orderBy]s.
     *
     * This will throw an [IllegalArgumentException] if ordering by a field that is not included in
     * the query. Read the **LIMITATIONS** section in the class doc to learn more.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase`
     * as an optional parameter.
     *
     * If not specified, then contacts are ordered by ID in ascending order.
     */
    fun orderBy(vararg orderBy: OrderBy): Query

    /**
     * See [Query.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy>): Query

    /**
     * See [Query.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy>): Query

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): Query

    /**
     * Limits the maximum number of returned [Contact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): Query

    /**
     * Returns a list of [Contact]s matching the preceding query options.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun find(): List<Contact>

    /**
     * Returns a list of [Contact]s matching the preceding query options.
     *
     * ## Cancellation
     *
     * The number of contacts and contact data found and processed may be large, which results
     * in this operation to take a while. Therefore, cancellation is supported while the contacts
     * list is being built. To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun find(cancel: () -> Boolean = { false }): List<Contact>
    fun find(cancel: () -> Boolean): List<Contact>

    /**
     * Returns the first [Contact] matching the preceding query options.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun findFirst(): Contact?

    /**
     * Returns the first [Contact] matching the preceding query options.
     *
     * ## Cancellation
     *
     * The number of contacts and contact data found and processed may be large, which results
     * in this operation to take a while. Therefore, cancellation is supported while the contacts
     * list is being built. To cancel at ay time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun findFirst(cancel: () -> Boolean = { false }): Contact?
    fun findFirst(cancel: () -> Boolean): Contact?
}

@Suppress("FunctionName")
internal fun Query(context: Context): Query = QueryImpl(
    ContactsPermissions(context),
    QueryResolverFactory(context.contentResolver)
)

private class QueryImpl(
    private val permissions: ContactsPermissions,
    private val queryResolverFactory: QueryResolverFactory,

    private var rawContactsWhere: Where = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include = DEFAULT_INCLUDE,
    private var where: Where = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : Query {

    override fun toString(): String {
        return """
            rawContactsWhere = $rawContactsWhere
            include = $include
            where = $where
            orderBy = $orderBy
            limit = $limit
            offset = $offset
        """.trimIndent()
    }

    override fun accounts(vararg accounts: Account): Query = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account>): Query = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account>): Query = apply {
        rawContactsWhere = if (accounts.count() == 0) {
            DEFAULT_RAW_CONTACTS_WHERE
        } else {
            accounts.whereOr { account ->
                (Fields.RawContacts.AccountName equalToIgnoreCase account.name)
                    .and(Fields.RawContacts.AccountType equalToIgnoreCase account.type)
            }
        }
    }

    override fun include(vararg fields: Field): Query = include(fields.asSequence())

    override fun include(fields: Collection<Field>): Query = include(fields.asSequence())

    override fun include(fields: Sequence<Field>): Query = apply {
        include = if (fields.count() == 0) {
            DEFAULT_INCLUDE
        } else {
            Include(fields + REQUIRED_INCLUDE_FIELDS)
        }
    }

    override fun where(where: Where?): Query = apply {
        this.where = where ?: DEFAULT_WHERE
    }

    override fun orderBy(vararg orderBy: OrderBy): Query = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy>): Query = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy>): Query = apply {
        this.orderBy = if (orderBy.count() == 0) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }

        if (!this.orderBy.allFieldsAreContainedIn(include.fields)) {
            throw IllegalArgumentException("Order by fields must be included in the query")
        }
    }

    override fun offset(offset: Int): Query = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw IllegalArgumentException("Offset must be greater than or equal to 0")
        }
    }

    override fun limit(limit: Int): Query = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw IllegalArgumentException("Limit must be greater than 0")
        }
    }

    override fun find(): List<Contact> = find { false }

    override fun find(cancel: () -> Boolean): List<Contact> {
        if (!permissions.canQuery()) {
            return emptyList()
        }

        return queryResolverFactory
            .resolver(cancel)
            .resolve(rawContactsWhere, include, where, orderBy, offset, limit)
    }

    override fun findFirst() = findFirst { false }

    override fun findFirst(cancel: () -> Boolean): Contact? = find(cancel).firstOrNull()

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE = NoWhere
        val DEFAULT_INCLUDE = Include(Fields.All)
        val REQUIRED_INCLUDE_FIELDS = Fields.Required.fields.asSequence()
        val DEFAULT_WHERE = NoWhere
        val DEFAULT_ORDER_BY = CompoundOrderBy(setOf(Fields.Contact.Id.asc()))
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private class QueryResolverFactory(private val contentResolver: ContentResolver) {
    fun resolver(cancel: () -> Boolean): QueryResolver = QueryResolver(contentResolver, cancel)
}

private class QueryResolver(
    private val contentResolver: ContentResolver,
    private val cancel: () -> Boolean
) {

    fun resolve(
        rawContactsWhere: Where,
        include: Include,
        where: Where,
        orderBy: CompoundOrderBy,
        offset: Int,
        limit: Int
    ): List<Contact> {

        var contactIds: Set<Long>? = null

        if (rawContactsWhere != NoWhere) {
            // Limit the contacts data to the set associated with the contacts found in the
            // RawContacts table matching the rawContactsWhere.
            contactIds = findContactIdsInRawContactsTable(rawContactsWhere)
        }

        if (where != NoWhere) {
            // Search for the contacts' ids in the data table.
            contactIds = findContactIdsInDataTable(
                if (contactIds != null) {
                    where and (Fields.Contact.Id `in` contactIds)
                } else {
                    where
                }
            )
        }

        // Search for all contacts with the matching contactIds. Note that contactIds will never be
        // null (though it may be empty) as long as at least one of rawContactsWhere or where is not
        // NoWhere. If contactIds is null, findContactsInDataTableWithIds will return all contacts
        // that have a row in the Data table.
        var contacts = findContactsInDataTableWithIds(contactIds, include)

        if (contactIds == null) {
            // TODO Update DEV_NOTES accordingly once all of the below TODOs have been answered.
            // TODO Fix RawContacts with no Data rows not showing up???

            // If contactIds is null, that means that rawContactsWhere and original parameter where
            // are both NoWhere. This means this query should include contacts that have no rows in
            // the Data table.
            //
            // Contacts that are not yet associated with an account may not have any
            // rows in the Data table. TODO Aren't these Contacts deleted???
            //
            // Contacts that are already associated with an account will
            // have at least one row in the Data table; a group membership row to the default group.
            // TODO can't users of this lib delete all data rows of Contacts with an account???
            val contactIdsWithDataRows = contacts.map { it.id }.toSet()
            val contactsWithNoDataRows =
                findAllContactsInRawContactsTableNotIn(contactIdsWithDataRows)

            contacts += contactsWithNoDataRows
        }

        return contacts
            .sortedWith(orderBy)
            .offsetAndLimit(offset, limit)
            .toList()
    }

    private fun findAllContactsInRawContactsTableNotIn(contactIds: Set<Long>): Sequence<Contact> =
        findContactsInTable(
            Table.RAW_CONTACTS,
            // There may be lingering RawContacts whose associated contact was already deleted.
            // Such RawContacts have contact id column value as null. We do not query the Contacts
            // table because our mappers only work with the RawContacts (some attr) and Data tables.
            Fields.RawContacts.ContactId.isNotNull()
                    and (Fields.RawContacts.ContactId notIn contactIds),
            Include(Fields.RawContacts.ContactId)
        )

    private fun findContactIdsInRawContactsTable(rawContactsWhere: Where): Set<Long> =
        findContactsInTable(
            Table.RAW_CONTACTS,
            // There may be lingering RawContacts whose associated contact was already deleted.
            // Such RawContacts have contact id column value as null. We do not query the Contacts
            // table because our mappers only work with the RawContacts (some attr) and Data tables.
            rawContactsWhere and Fields.RawContacts.ContactId.isNotNull(),
            Include(Fields.RawContacts.ContactId)
        )
            .map { it.id }
            .toSet()

    private fun findContactIdsInDataTable(where: Where): Set<Long> =
        findContactsInTable(Table.DATA, where, Include(Fields.Contact.Id))
            .map { it.id }
            .toSet()

    private fun findContactsInDataTableWithIds(
        contactIds: Set<Long>?, include: Include
    ): Sequence<Contact> {
        val where = if (contactIds != null) {
            Fields.Contact.Id `in` contactIds
        } else {
            null
        }

        return findContactsInTable(Table.DATA, where, include)
    }

    private fun findContactsInTable(
        table: Table, where: Where?, include: Include
    ): Sequence<Contact> {
        val cursor = contentResolver.query(
            table.uri,
            include.columnNames,
            if (where != null) "$where" else null,
            null,
            null
        )

        var contactsSequence: Sequence<Contact> = emptySequence()

        if (cursor != null) {
            // Direct data table queries do not return user profile data.
            contactsSequence = ContactsMapper(isProfile = false, cancel = cancel)
                .fromCursor(cursor)
                .map()
            cursor.close()
        }

        return contactsSequence
    }
}

private fun Sequence<Contact>.offsetAndLimit(offset: Int, limit: Int): Sequence<Contact> {
    // prevent index out of bounds by ensuring offset and limit are within bounds
    val size = count()
    val start = min(offset, size)
    val end = min(start + limit, size)

    return filterIndexed { index, _ -> index in start until end }
}