package com.vestrel00.contacts

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.mapper.ContactsMapper
import com.vestrel00.contacts.entities.table.Table
import kotlin.math.min

interface Query {

    fun accounts(vararg accounts: Account): Query

    fun accounts(accounts: Collection<Account>): Query

    fun include(vararg fields: Field): Query

    fun include(fields: Collection<Field>): Query

    fun include(fields: Sequence<Field>): Query

    fun where(where: Where?): Query

    fun orderBy(vararg orderBy: OrderBy): Query

    fun orderBy(orderBy: Collection<OrderBy>): Query

    fun orderBy(orderBy: Sequence<OrderBy>): Query

    fun offset(offset: Int): Query

    fun limit(limit: Int): Query

    fun find(): List<Contact>

    fun find(cancel: () -> Boolean): List<Contact>

    fun findFirst(): Contact?

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

    override fun accounts(vararg accounts: Account): Query = accounts(accounts.asList())

    override fun accounts(accounts: Collection<Account>): Query = apply {
        rawContactsWhere = if (accounts.isEmpty()) {
            DEFAULT_RAW_CONTACTS_WHERE
        } else {
            accounts.toSet().whereOr { account ->
                (Fields.RawContact.AccountName equalToIgnoreCase account.name)
                    .and(Fields.RawContact.AccountType equalToIgnoreCase account.type)
            }
        }
    }

    override fun include(vararg fields: Field): Query = include(fields.asSequence())

    override fun include(fields: Collection<Field>): Query = include(fields.asSequence())

    override fun include(fields: Sequence<Field>): Query = apply {
        include = if (fields.count() == 0) {
            DEFAULT_INCLUDE
        } else {
            Include(fields + REQUIRED_INCLUDES.asSequence())
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
        val REQUIRED_INCLUDES = setOf<Field>(
            Fields.Id,
            Fields.Contact.Id,
            Fields.RawContactId,
            Fields.MimeType
        )

        val DEFAULT_RAW_CONTACTS_WHERE = NoWhere
        val DEFAULT_INCLUDE = Include(Fields.All)
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

    /*
     * Note. It's necessary to make at least 2 separate queries because we cannot retrieve all data
     * rows for a contact with just 1 query.
     *
     * For example, if the where clause is "email = 'a@gmail.com'" then it would only return the
     * contact objects with email of "a@gmail.com". Other contact fields would not be included
     * (e.g. phone, address, etc).
     */
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
            // If contactIds is null, that means that rawContactsWhere and original parameter where
            // are both NoWhere. This means this query should include contacts that have no rows in
            // the Data table. Contacts that are not yet associated with an account may not have any
            // rows in the Data table. Contacts that are already associated with an account will
            // have at least one row in the Data table; a group membership row to the default group.
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
            // table because our mappers only work with the RawContacts and Data tables.
            Fields.Contact.Id.isNotNull() and (Fields.Contact.Id notIn contactIds),
            Include(Fields.Contact.Id)
        )

    private fun findContactIdsInRawContactsTable(rawContactsWhere: Where): Set<Long> =
        findContactsInTable(Table.RAW_CONTACTS, rawContactsWhere, Include(Fields.Contact.Id))
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
            contactsSequence = ContactsMapper().fromCursor(cursor, cancel)
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