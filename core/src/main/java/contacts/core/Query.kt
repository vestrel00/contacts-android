package contacts.core

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.entities.Contact
import contacts.core.entities.cursor.contactsCursor
import contacts.core.entities.cursor.dataContactsCursor
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.mapper.ContactsMapper
import contacts.core.entities.table.Table
import contacts.core.util.*

/**
 * Queries the Contacts Provider tables and returns a list of contacts matching a specific search
 * criteria. All RawContacts of matching Contacts are included in the resulting Contact instances.
 *
 * This provides a great deal of granularity and customizations when providing matching criteria
 * via [where]. For a broader, and more native Contacts app like query, use [BroadQuery].
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
 * val contacts : List<Contact> = query
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
     * If [includeBlanks] is set to true, then queries may include blank RawContacts or blank
     * Contacts ([Contact.isBlank]). Otherwise, blanks will not be included. This flag is set to
     * true by default, which results in more database queries so setting this to false will
     * increase performance, especially for large Contacts databases.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The native Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the native Contacts app shows them.
     *
     * There are two scenarios where blanks may not be returned if this flag is set to false.
     *
     * 1. Contact with RawContact(s) with no Data row(s).
     *     - In this case, the Contact is blank as well as its RawContact(s).
     * 2. Contact that has a RawContact with Data row(s) and a RawContact with no Data rows.
     *     - In this case, the Contact and the RawContact with Data row(s) are not blank but the
     *     RawContact with no Data row is blank.
     *
     * ## Performance
     *
     * This may require one or more additional queries, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun includeBlanks(includeBlanks: Boolean): Query

    /**
     * Limits the search to only those RawContacts associated with one of the given accounts.
     * Contacts returned may still contain RawContacts / data that belongs to other accounts not
     * specified in [accounts] because Contacts may be made up of more than one RawContact from
     * different Accounts. This is the same behavior as the native Contacts app.
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
     * Includes only the given set of [fields] (data) in each of the matching contacts.
     *
     * The matching contacts **may** have non-null data for each of the included fields. Fields
     * that are included will not guarantee non-null data in the returned contact instances because
     * some data may actually be null in the database.
     *
     * If no fields are specified, then all fields are included. Otherwise, only the specified
     * fields will be included in addition to required API fields [Fields.Required] (e.g. IDs),
     * which are always included.
     *
     * Note that this may affect performance. It is recommended to only include fields that will be
     * used to save CPU and memory.
     *
     * ## Performing updates on entities with partial includes
     *
     * When the query [include] function is used, only certain data will be included in the returned
     * entities. All other data are guaranteed to be null (except for those in [Fields.Required]).
     * When performing updates on entities that have only partial data included, make sure to use
     * the same included fields in the update operation as the included fields used in the query.
     * This will ensure that the set of data queried and updated are the same. For example, in order
     * to get and set only email addresses and leave everything the same in the database...
     *
     * ```kotlin
     * val contacts = query.include(Fields.Email.Address).find()
     * val mutableContacts = setEmailAddresses(contacts)
     * update.contacts(mutableContacts).include(Fields.Email.Address).commit()
     * ```
     *
     * On the other hand, you may intentionally include only some data and perform updates without
     * on all data (not just the included ones) to effectively delete all non-included data. This
     * is, currently, a feature- not a bug! For example, in order to get and set only email
     * addresses and set all other data to null (such as phone numbers, name, etc) in the database..
     *
     * ```kotlin
     * val contacts = query.include(Fields.Email.Address).find()
     * val mutableContacts = setEmailAddresses(contacts)
     * update.contacts(mutableContacts).include(Fields.all).commit()
     * ```
     *
     * This gives you the most flexibility when it comes to specifying what fields to
     * include/exclude in queries, inserts, and update, which will allow you to do things beyond
     * your wildest imagination!
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
     * Filters the [Contact]s matching the criteria defined by the [where]. If not specified or
     * null, then all [Contact]s are returned.
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
     * This where clause is only used to query the Data table. Some contacts do not have any Data
     * table rows (see [includeBlanks]). However, this library exposes some fields that belong to
     * other tables, accessible via the Data table with joins;
     *
     * - [Fields.Contact]
     * - [Fields.RawContact]
     *
     * Using these fields in the where clause does not have any effect in matching blank Contacts
     * or RawContacts simply because they have no Data rows containing these joined fields.
     *
     * See [includeBlanks] for more info about blank Contacts and RawContacts.
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
    interface Result : List<Contact>, CrudApi.Result {

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun Query(contacts: Contacts): Query = QueryImpl(contacts)

private class QueryImpl(
    override val contactsApi: Contacts,

    private var includeBlanks: Boolean = DEFAULT_INCLUDE_BLANKS,
    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include<AbstractDataField> = contactsApi.includeAllFields(),
    private var where: Where<AbstractDataField>? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy<ContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,

    override val isRedacted: Boolean = false
) : Query {

    override fun toString(): String =
        """
            Query {
                includeBlanks: $includeBlanks
                rawContactsWhere: $rawContactsWhere
                include: $include
                where: $where
                orderBy: $orderBy
                limit: $limit
                offset: $offset
                hasPermission: ${permissions.canQuery()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Query = QueryImpl(
        contactsApi,

        includeBlanks,
        // Redact Account information.
        rawContactsWhere?.redactedCopy(),
        include,
        // Redact search input.
        where?.redactedCopy(),
        orderBy,
        limit,
        offset,

        isRedacted = true
    )

    override fun includeBlanks(includeBlanks: Boolean): Query = apply {
        this.includeBlanks = includeBlanks
    }

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

    override fun find(): Query.Result = find { false }

    override fun find(cancel: () -> Boolean): Query.Result {
        onPreExecute()

        val contacts = if (!permissions.canQuery() || cancel()) {
            emptyList()
        } else {
            // Invoke the function to ensure that delegators (e.g. in tests) get access to the private
            // attributes even if the consumer does not call these functions. This allows delegators to
            // make necessary modifications to private attributes without having to make this class
            // open for inheritance or exposing unnecessary attributes to consumers.
            include(include.fields)
            where(where)

            contentResolver.resolve(
                customDataRegistry, includeBlanks,
                rawContactsWhere, include, where, orderBy, limit, offset, cancel
            )
        }

        return QueryResult(contacts)
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
    }

    private companion object {
        const val DEFAULT_INCLUDE_BLANKS = true
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val REQUIRED_INCLUDE_FIELDS by unsafeLazy { Fields.Required.all.asSequence() }
        val DEFAULT_WHERE: Where<AbstractDataField>? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(ContactsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private fun ContentResolver.resolve(
    customDataRegistry: CustomDataRegistry,
    includeBlanks: Boolean,
    rawContactsWhere: Where<RawContactsField>?,
    include: Include<AbstractDataField>,
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
            addAll(findContactIdsInDataTable(reduce(where, cancel), cancel))
        }

        // Get the Contacts Ids of blank RawContacts and blank Contacts matching the where from the
        // RawContacts and Contacts table respectively. Suppress DB exceptions because the where
        // clause may contain fields (columns) that are not in the respective tables.
        if (includeBlanks) {
            contactIds.addAll(
                findContactIdsInRawContactsTable(where.inRawContactsTable(), cancel, true)
            )
            contactIds.addAll(
                findContactIdsInContactsTable(where.inContactsTable(), cancel, true)
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
            addAll(findContactIdsInRawContactsTable(rawContactsTableWhere, cancel, false))
        }

        // If no match, return empty list.
        if (contactIds.isEmpty() || cancel()) {
            return emptyList()
        }
    }

    return resolve(
        customDataRegistry, contactIds, includeBlanks, include, orderBy, limit, offset, cancel
    )
}

internal fun ContentResolver.resolve(
    customDataRegistry: CustomDataRegistry,
    contactIds: MutableSet<Long>?,
    includeBlanks: Boolean,
    include: Include<AbstractDataField>,
    orderBy: CompoundOrderBy<ContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<Contact> {

    if (cancel() || (contactIds != null && contactIds.isEmpty())) {
        return emptyList()
    }

    // Collect Contacts, RawContacts, and Data with this mapper.
    val contactsMapper = ContactsMapper(customDataRegistry, cancel)

    // Collect Contacts (which may include blanks) that are in the given contactIds.
    // If contactIds is null, then all Contacts are collected.
    query(
        Table.Contacts, include.onlyContactsFields(), contactIds?.let {
            ContactsFields.Id `in` it
        },
        sortOrder = "$orderBy LIMIT $limit OFFSET $offset",
        processCursor = contactsMapper::processContactsCursor
    )

    if (cancel()) {
        return emptyList()
    }

    // Collect Data for non-blank RawContact and Contact in the given contactIds.
    // If contactIds is null, then all Data and non-blank RawContacts and Contacts are collected.
    query(
        Table.Data, include, contactIds?.let {
            Fields.Contact.Id `in` it
        },
        processCursor = contactsMapper::processDataCursor
    )

    if (cancel()) {
        return emptyList()
    }

    // Collect blank RawContacts.
    if (includeBlanks) {
        query(
            Table.RawContacts, include.onlyRawContactsFields(),
            (RawContactsFields.Deleted notEqualTo true) and
                    if (contactIds != null) {
                        // Note that we do not need to check for the DELETED flag here because RawContacts
                        // that are marked for deletion also have a null Contact ID reference.
                        RawContactsFields.ContactId `in` contactIds
                    } else {
                        // There may be RawContacts that are marked for deletion that have not yet been deleted.
                        RawContactsFields.Deleted notEqualTo true
                    },
            processCursor = contactsMapper::processRawContactsCursor
        )
    }

    // Output all collected Contacts, RawContacts, and Data.
    return if (cancel()) emptyList() else contactsMapper.map()
}

private fun ContentResolver.findContactIdsInContactsTable(
    contactsWhere: Where<ContactsField>?, cancel: () -> Boolean, suppressDbExceptions: Boolean
): Set<Long> = if (cancel()) emptySet() else {
    query(
        Table.Contacts, Include(ContactsFields.Id), contactsWhere,
        suppressDbExceptions = suppressDbExceptions
    ) {
        mutableSetOf<Long>().apply {
            val contactsCursor = it.contactsCursor()
            while (!cancel() && it.moveToNext()) {
                add(contactsCursor.contactId)
            }
        }
    } ?: emptySet()
}

internal fun ContentResolver.findContactIdsInRawContactsTable(
    rawContactsWhere: Where<RawContactsField>?, cancel: () -> Boolean, suppressDbExceptions: Boolean
): Set<Long> = if (cancel()) emptySet() else {
    query(
        Table.RawContacts,
        Include(RawContactsFields.ContactId),
        // There may be RawContacts that are marked for deletion that have not yet been deleted.
        (RawContactsFields.Deleted notEqualTo true) and rawContactsWhere,
        suppressDbExceptions = suppressDbExceptions
    ) {
        mutableSetOf<Long>().apply {
            val rawContactsCursor = it.rawContactsCursor()
            while (!cancel() && it.moveToNext()) {
                add(rawContactsCursor.contactId)
            }
        }
    } ?: emptySet()
}

internal fun ContentResolver.findContactIdsInDataTable(
    where: Where<AbstractDataField>?, cancel: () -> Boolean
): Set<Long> = if (cancel()) emptySet() else {
    query(Table.Data, Include(Fields.Contact.Id), where) {
        val contactIds = mutableSetOf<Long>()
        val contactsCursor = it.dataContactsCursor()
        while (!cancel() && it.moveToNext()) {
            contactIds.add(contactsCursor.contactId)
        }
        contactIds
    } ?: emptySet()
}

private class QueryResult private constructor(
    contacts: List<Contact>,
    override val isRedacted: Boolean
) : ArrayList<Contact>(contacts), Query.Result {

    constructor(contacts: List<Contact>) : this(contacts, false)

    override fun toString(): String =
        """
            Query.Result {
                Number of contacts found: $size
                First contact: ${firstOrNull()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Query.Result = QueryResult(
        redactedCopies(),
        isRedacted = true
    )
}