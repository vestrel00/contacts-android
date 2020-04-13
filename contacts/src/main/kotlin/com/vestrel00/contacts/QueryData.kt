package com.vestrel00.contacts

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.getLong
import com.vestrel00.contacts.entities.mapper.entityMapperFor
import com.vestrel00.contacts.entities.table.Table

/**
 * Queries the Contacts data table and returns one or more contact data matching the search
 * criteria.
 *
 * This returns a list of specific entities (e.g. emails, phones, etc). This is optimized and
 * useful for searching through and paginating one entity type.
 *
 * To query for Contacts instead of just Contact data, use [Query].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All queries will return an empty list or null result if the permission
 * is not granted.
 *
 * ## Accounts
 *
 * Unlike [Query], limiting queries to certain accounts only returns data belonging to those given
 * [accounts].
 *
 * ## Usage
 *
 * Here is an example query that returns the first 10 [Address]s, skipping the first 5, where the
 * [Address.country] is "US" and [Address.postcode] starting with "78", ordered by the
 * [Address.postcode] in ascending order. Only addresses in the given account are included. Only the
 * [Address.formattedAddress] attribute is included.
 *
 * In Kotlin,
 *
 * ```kotlin
 * val addresses : List<Address> = queryData.
 *      .accounts(account)
 *      .include(Fields.Address.FormattedAddress)
 *      .where((Fields.Address.Country equalTo "US") and (Fields.Address.PostCode startsWith "78"))
 *      .orderBy(Fields.Address.PostCode.asc())
 *      .offset(5)
 *      .limit(10)
 *      .addresses()
 * ```
 *
 * In Java,
 *
 * ```java
 * List<Address> addresses = query
 *      .accounts(account)
 *      .include(Address.FormattedAddress)
 *      .where(equalTo(Address.Country, "US").and(startsWith(Address.PostCode, "78")))
 *      .orderBy(Address.PostCode.asc())
 *      .offset(5)
 *      .limit(10)
 *      .addresses();
 * ```
 */
interface QueryData {

    /**
     * Limits this query to only search for data associated with the given accounts.
     *
     * If no accounts are specified, then all data from all accounts are searched.
     */
    fun accounts(vararg accounts: Account): QueryData

    /**
     * See [QueryData.accounts]
     */
    fun accounts(accounts: Collection<Account>): QueryData

    /**
     * See [QueryData.accounts]
     */
    fun accounts(accounts: Sequence<Account>): QueryData

    /**
     * Includes the given set of [fields] in the resulting data object(s).
     *
     * If no fields are specified, then all fields are included. Otherwise, only the specified
     * fields will be included in addition to the required fields, which are always included.
     *
     * Fields that are included will not guarantee non-null attributes in the returned entity
     * object instances.
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     */
    fun include(vararg fields: Field): QueryData

    /**
     * See [QueryData.include].
     */
    fun include(fields: Collection<Field>): QueryData

    /**
     * See [QueryData.include].
     */
    fun include(fields: Sequence<Field>): QueryData

    /**
     * Filters the returned data matching the criteria defined by the [where].
     *
     * Be careful what fields are used in this where. Querying for all addresses where the phone
     * number starts with 555 will produce no results. Think about it =)
     *
     * If not specified or null, then all data is returned, limited by [limit].
     */
    fun where(where: Where?): QueryData

    /**
     * Orders the returned data using one or more [orderBy]s.
     *
     * This will throw an [IllegalArgumentException] if ordering by a field that is not included in
     * the query. Read the **LIMITATIONS** section in the class doc to learn more.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase`
     * as an optional parameter.
     *
     * If not specified, then data is ordered by ID in ascending order.
     */
    fun orderBy(vararg orderBy: OrderBy): QueryData

    /**
     * See [QueryData.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy>): QueryData

    /**
     * See [QueryData.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy>): QueryData

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): QueryData

    /**
     * Limits the maximum number of returned data to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): QueryData

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun addresses(): List<Address>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun addresses(cancel: () -> Boolean = { false }): List<Address>
    fun addresses(cancel: () -> Boolean): List<Address>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun companies(): List<Company>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun companies(cancel: () -> Boolean): List<Company>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun emails(): List<Email>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun emails(cancel: () -> Boolean): List<Email>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun events(): List<Event>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun events(cancel: () -> Boolean): List<Event>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun groupMemberships(): List<GroupMembership>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun groupMemberships(cancel: () -> Boolean): List<GroupMembership>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun ims(): List<Im>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun ims(cancel: () -> Boolean): List<Im>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun names(): List<Name>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun names(cancel: () -> Boolean): List<Name>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun nicknames(): List<Nickname>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun nicknames(cancel: () -> Boolean): List<Nickname>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun notes(): List<Note>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun notes(cancel: () -> Boolean): List<Note>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun phones(): List<Phone>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun phones(cancel: () -> Boolean): List<Phone>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun relations(): List<Relation>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun relations(cancel: () -> Boolean): List<Relation>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun sipAddresses(): List<SipAddress>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun sipAddresses(cancel: () -> Boolean): List<SipAddress>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun websites(): List<Website>

    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun websites(cancel: () -> Boolean): List<Website>
}

@Suppress("FunctionName")
internal fun QueryData(context: Context): QueryData = QueryDataImpl(
    ContactsPermissions(context),
    QueryDataResolverFactory(context.contentResolver)
)

private class QueryDataImpl(
    private val permissions: ContactsPermissions,
    private val queryResolverFactory: QueryDataResolverFactory,

    private var rawContactsWhere: Where = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include = DEFAULT_INCLUDE,
    private var where: Where = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : QueryData {

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

    override fun accounts(vararg accounts: Account): QueryData = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account>): QueryData =
        accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account>): QueryData = apply {
        rawContactsWhere = if (accounts.count() == 0) {
            DEFAULT_RAW_CONTACTS_WHERE
        } else {
            accounts.whereOr { account ->
                (Fields.RawContacts.AccountName equalToIgnoreCase account.name)
                    .and(Fields.RawContacts.AccountType equalToIgnoreCase account.type)
            }
        }
    }

    override fun include(vararg fields: Field): QueryData = include(fields.asSequence())

    override fun include(fields: Collection<Field>): QueryData = include(fields.asSequence())

    override fun include(fields: Sequence<Field>): QueryData = apply {
        include = if (fields.count() == 0) {
            DEFAULT_INCLUDE
        } else {
            Include(fields + Fields.Required.fields.asSequence())
        }
    }

    override fun where(where: Where?): QueryData = apply {
        this.where = where ?: DEFAULT_WHERE
    }

    override fun orderBy(vararg orderBy: OrderBy): QueryData = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy>): QueryData = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy>): QueryData = apply {
        this.orderBy = if (orderBy.count() == 0) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }

        if (!this.orderBy.allFieldsAreContainedIn(include.fields)) {
            throw IllegalArgumentException("Order by fields must be included in the query")
        }
    }

    override fun offset(offset: Int): QueryData = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw IllegalArgumentException("Offset must be greater than or equal to 0")
        }
    }

    override fun limit(limit: Int): QueryData = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw IllegalArgumentException("Limit must be greater than 0")
        }
    }

    override fun addresses(): List<Address> = addresses { false }

    override fun addresses(cancel: () -> Boolean): List<Address> = resolve(MimeType.ADDRESS, cancel)

    override fun companies(): List<Company> = companies { false }

    override fun companies(cancel: () -> Boolean): List<Company> = resolve(MimeType.COMPANY, cancel)

    override fun emails(): List<Email> = emails { false }

    override fun emails(cancel: () -> Boolean): List<Email> = resolve(MimeType.EMAIL, cancel)

    override fun events(): List<Event> = events { false }

    override fun events(cancel: () -> Boolean): List<Event> = resolve(MimeType.EVENT, cancel)

    override fun groupMemberships(): List<GroupMembership> = groupMemberships { false }

    override fun groupMemberships(cancel: () -> Boolean): List<GroupMembership> =
        resolve(MimeType.GROUP_MEMBERSHIP, cancel)

    override fun ims(): List<Im> = ims { false }

    override fun ims(cancel: () -> Boolean): List<Im> = resolve(MimeType.IM, cancel)

    override fun names(): List<Name> = names { false }

    override fun names(cancel: () -> Boolean): List<Name> = resolve(MimeType.NAME, cancel)

    override fun nicknames(): List<Nickname> = nicknames { false }

    override fun nicknames(cancel: () -> Boolean): List<Nickname> =
        resolve(MimeType.NICKNAME, cancel)

    override fun notes(): List<Note> = notes { false }

    override fun notes(cancel: () -> Boolean): List<Note> = resolve(MimeType.NOTE, cancel)

    override fun phones(): List<Phone> = phones { false }

    override fun phones(cancel: () -> Boolean): List<Phone> = resolve(MimeType.PHONE, cancel)

    override fun relations(): List<Relation> = relations { false }

    override fun relations(cancel: () -> Boolean): List<Relation> =
        resolve(MimeType.RELATION, cancel)

    override fun sipAddresses(): List<SipAddress> = sipAddresses { false }

    override fun sipAddresses(cancel: () -> Boolean): List<SipAddress> =
        resolve(MimeType.SIP_ADDRESS, cancel)

    override fun websites(): List<Website> = websites { false }

    override fun websites(cancel: () -> Boolean): List<Website> = resolve(MimeType.WEBSITE, cancel)

    private fun <T : DataEntity> resolve(mimeType: MimeType, cancel: () -> Boolean): List<T> {
        if (!permissions.canQuery()) {
            return emptyList()
        }

        return queryResolverFactory
            .resolver(cancel)
            .resolveEntities(mimeType, rawContactsWhere, include, where, orderBy, offset, limit)
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE = NoWhere
        val DEFAULT_INCLUDE = Include(Fields.All)
        val DEFAULT_WHERE = NoWhere
        val DEFAULT_ORDER_BY = CompoundOrderBy(setOf(Fields.Id.asc()))
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private class QueryDataResolverFactory(private val contentResolver: ContentResolver) {
    fun resolver(cancel: () -> Boolean): QueryDataResolver =
        QueryDataResolver(contentResolver, cancel)
}

private class QueryDataResolver(
    private val contentResolver: ContentResolver,
    private val cancel: () -> Boolean
) {

    fun <T : DataEntity> resolveEntities(
        mimeType: MimeType,
        rawContactsWhere: Where,
        include: Include,
        where: Where,
        orderBy: CompoundOrderBy,
        offset: Int,
        limit: Int
    ): List<T> {

        var dataWhere = Fields.MimeType equalTo mimeType

        if (rawContactsWhere != NoWhere) {
            // Limit the data to the set associated with the RawContacts found in the RawContacts
            // table matching the rawContactsWhere.
            val rawContactIds = findRawContactIdsInRawContactsTable(rawContactsWhere)
            val inRawContactIds = Fields.RawContact.Id `in` rawContactIds
            dataWhere = dataWhere and inRawContactIds
        }

        if (where != NoWhere) {
            dataWhere = dataWhere and where
        }

        val cursor = contentResolver.query(
            Table.DATA.uri,
            include.columnNames,
            "$dataWhere",
            null,
            "$orderBy LIMIT $limit OFFSET $offset"
        )

        return mutableListOf<T>().apply {
            if (cursor != null) {
                val entityMapper = cursor.entityMapperFor<T>(mimeType)
                while (cursor.moveToNext()) {
                    add(entityMapper.value)

                    if (cancel()) {
                        // Return empty list if cancelled to ensure only correct data set is returned.
                        clear()
                        break
                    }
                }
                cursor.close()
            }
        }
    }

    private fun findRawContactIdsInRawContactsTable(rawContactsWhere: Where): Set<Long> {
        val cursor = contentResolver.query(
            Table.RAW_CONTACTS.uri,
            arrayOf(Fields.RawContacts.Id.columnName),
            "$rawContactsWhere",
            null,
            null
        )

        val rawContactIds = mutableSetOf<Long>()

        if (cursor != null) {
            while (cursor.moveToNext()) {
                cursor.getLong(Fields.RawContacts.Id)?.let(rawContactIds::add)
            }
            cursor.close()
        }

        return rawContactIds
    }
}