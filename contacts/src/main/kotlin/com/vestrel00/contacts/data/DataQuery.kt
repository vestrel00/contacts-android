package com.vestrel00.contacts.data

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.mapper.entityMapperFor
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.isEmpty
import com.vestrel00.contacts.util.query
import com.vestrel00.contacts.util.toRawContactsWhere

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
 * val addresses : List<Address> = dataQuery.
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
 *
 * ## Developer notes
 *
 * Phones, Emails, and Addresses have a CONTENT_URI that contains all rows consisting of only those
 * data kinds. Other data kinds do not have this content uri. These probably exists as an index /
 * for optimization since phones, emails, and addresses are the most commonly used data kinds. Using
 * these CONTENT_URIs probably results in shorter search times since it only has to look through a
 * subset of data instead of the entire data table.
 */
interface DataQuery {

    /**
     * Limits this query to only search for data associated with the given accounts.
     *
     * If no accounts are specified (this function is not called or called with no Accounts), then
     * all data from all accounts are searched.
     *
     * A null [Account] may be provided here, which results in data belonging to RawContacts with no
     * associated Account to be included in the search. RawContacts without an associated account
     * are considered local or device-only contacts, which are not synced.
     */
    fun accounts(vararg accounts: Account?): DataQuery

    /**
     * See [DataQuery.accounts]
     */
    fun accounts(accounts: Collection<Account?>): DataQuery

    /**
     * See [DataQuery.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): DataQuery

    /**
     * Includes the given set of [fields] from [Fields] in the resulting data object(s).
     *
     * If no fields are specified, then all fields are included. Otherwise, only the specified
     * fields will be included in addition to [Fields.Required], which are always included.
     *
     * Fields that are included will not guarantee non-null attributes in the returned entity
     * object instances.
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     */
    fun include(vararg fields: Field): DataQuery

    /**
     * See [DataQuery.include].
     */
    fun include(fields: Collection<Field>): DataQuery

    /**
     * See [DataQuery.include].
     */
    fun include(fields: Sequence<Field>): DataQuery

    /**
     * Filters the returned data matching the criteria defined by the [where].
     *
     * Be careful what fields are used in this where. Querying for all addresses where the phone
     * number starts with 555 will produce no results. Think about it =)
     *
     * If not specified or null, then all data is returned.
     *
     * Use [Fields] to construct the [where].
     */
    fun where(where: Where?): DataQuery

    /**
     * Orders the returned data using one or more [orderBy]s. If not specified, then data is ordered
     * by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     *
     * Use [Fields] to construct the [orderBy].
     */
    fun orderBy(vararg orderBy: OrderBy): DataQuery

    /**
     * See [DataQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy>): DataQuery

    /**
     * See [DataQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy>): DataQuery

    /**
     * Limits the maximum number of returned data to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): DataQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): DataQuery

    /**
     * The list of [Address]es.
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
    fun addresses(): List<Address>

    /**
     * The list of [Address]es.
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
    // fun addresses(cancel: () -> Boolean = { false }): List<Address>
    fun addresses(cancel: () -> Boolean): List<Address>

    /**
     * The list of [Email]s.
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
    fun emails(): List<Email>

    /**
     * The list of [Email]s.
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
    fun emails(cancel: () -> Boolean): List<Email>

    /**
     * The list of [Event]s.
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
    fun events(): List<Event>

    /**
     * The list of [Event]s.
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
    fun events(cancel: () -> Boolean): List<Event>

    /**
     * The list of [GroupMembership]s.
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
    fun groupMemberships(): List<GroupMembership>

    /**
     * The list of [GroupMembership]s.
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
    fun groupMemberships(cancel: () -> Boolean): List<GroupMembership>

    /**
     * The list of [Im]s.
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
    fun ims(): List<Im>

    /**
     * The list of [Im]s.
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
    fun ims(cancel: () -> Boolean): List<Im>

    /**
     * The list of [Name]s.
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
    fun names(): List<Name>

    /**
     * The list of [Name]s.
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
    fun names(cancel: () -> Boolean): List<Name>

    /**
     * The list of [Nickname]s.
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
    fun nicknames(): List<Nickname>

    /**
     * The list of [Nickname]s.
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
    fun nicknames(cancel: () -> Boolean): List<Nickname>

    /**
     * The list of [Note]s.
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
    fun notes(): List<Note>

    /**
     * The list of [Note]s.
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
    fun notes(cancel: () -> Boolean): List<Note>

    /**
     * The list of [Organization]s.
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
    fun organizations(): List<Organization>

    /**
     * The list of [Organization]s.
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
    fun organizations(cancel: () -> Boolean): List<Organization>

    /**
     * The list of [Phone]s.
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
    fun phones(): List<Phone>

    /**
     * The list of [Phone]s.
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
    fun phones(cancel: () -> Boolean): List<Phone>

    /**
     * The list of [Relation]s.
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
    fun relations(): List<Relation>

    /**
     * The list of [Relation]s.
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
    fun relations(cancel: () -> Boolean): List<Relation>

    /**
     * The list of [SipAddress]es.
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
    fun sipAddresses(): List<SipAddress>

    /**
     * The list of [SipAddress]s.
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
    fun sipAddresses(cancel: () -> Boolean): List<SipAddress>

    /**
     * The list of [Website]s.
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
    fun websites(): List<Website>

    /**
     * The list of [Website]s.
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
    fun websites(cancel: () -> Boolean): List<Website>
}

@Suppress("FunctionName")
internal fun DataQuery(context: Context): DataQuery = DataQueryImpl(
    ContactsPermissions(context),
    context.contentResolver
)

private class DataQueryImpl(
    private val permissions: ContactsPermissions,
    private val contentResolver: ContentResolver,

    private var rawContactsWhere: Where? = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include = DEFAULT_INCLUDE,
    private var where: Where? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : DataQuery {

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

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): DataQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()
    }

    override fun include(vararg fields: Field) = include(fields.asSequence())

    override fun include(fields: Collection<Field>) = include(fields.asSequence())

    override fun include(fields: Sequence<Field>): DataQuery = apply {
        include = if (fields.isEmpty()) {
            DEFAULT_INCLUDE
        } else {
            Include(fields + REQUIRED_INCLUDE_FIELDS)
        }
    }

    override fun where(where: Where?): DataQuery = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = where ?: DEFAULT_WHERE
    }

    override fun orderBy(vararg orderBy: OrderBy) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy>): DataQuery = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }

        /*
        There is no need for this check because custom ordering is not used. The orderBy simply
        gets passed to the query function as a string.

        if (!this.orderBy.allFieldsAreContainedIn(include.fields)) {
            throw IllegalArgumentException("Order by fields must be included in the query")
        }
         */
    }

    override fun limit(limit: Int): DataQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw IllegalArgumentException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): DataQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw IllegalArgumentException("Offset must be greater than or equal to 0")
        }
    }

    // Note that Phones, Emails, and Addresses have a CONTENT_URI that contains all rows
    // consisting of only those data kinds. Other data kinds do not have this content uri.
    // It probably exists as an index / optimization tool since phones, emails, and
    // addresses are the most commonly used data kinds.

    override fun addresses(): List<Address> = addresses { false }

    override fun addresses(cancel: () -> Boolean): List<Address> = resolve(MimeType.ADDRESS, cancel)

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

    override fun organizations(): List<Organization> = organizations { false }

    override fun organizations(cancel: () -> Boolean): List<Organization> =
        resolve(MimeType.ORGANIZATION, cancel)

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

        return contentResolver.resolve(
            mimeType, rawContactsWhere, include, where, orderBy, limit, offset, cancel
        )
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where? = null
        val DEFAULT_INCLUDE = Include(Fields.All)
        val REQUIRED_INCLUDE_FIELDS = Fields.Required.fields.asSequence()
        val DEFAULT_WHERE: Where? = null
        val DEFAULT_ORDER_BY = CompoundOrderBy(setOf(Fields.Id.asc()))
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private fun <T : DataEntity> ContentResolver.resolve(
    mimeType: MimeType,
    rawContactsWhere: Where?,
    include: Include,
    where: Where?,
    orderBy: CompoundOrderBy,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<T> {

    var dataWhere = mimeType.dataWhere()

    if (rawContactsWhere != null) {
        // Limit the data to the set associated with the RawContacts found in the RawContacts
        // table matching the rawContactsWhere.
        val rawContactIds = findRawContactIdsInRawContactsTable(rawContactsWhere, cancel)
        dataWhere = if (dataWhere != null) {
            dataWhere and (Fields.RawContact.Id `in` rawContactIds)
        } else {
            Fields.RawContact.Id `in` rawContactIds
        }
    }

    if (where != null) {
        dataWhere = if (dataWhere != null) dataWhere and where else where
    }

    return query(
        mimeType.contentUri(), include, dataWhere, "$orderBy LIMIT $limit OFFSET $offset"
    ) {
        mutableListOf<T>().apply {
            val entityMapper = it.entityMapperFor<T>(mimeType)
            while (!cancel() && it.moveToNext()) {
                add(entityMapper.value)
            }

            // Ensure only complete data sets are returned.
            if (cancel()) {
                clear()
            }
        }
    } ?: emptyList()
}

private fun ContentResolver.findRawContactIdsInRawContactsTable(
    rawContactsWhere: Where, cancel: () -> Boolean
): Set<Long> =
    query(
        Table.RAW_CONTACTS, Include(RawContactsFields.Id), rawContactsWhere
    ) {
        mutableSetOf<Long>().apply {
            val rawContactsCursor = it.rawContactsCursor()
            while (!cancel() && it.moveToNext()) {
                rawContactsCursor.rawContactId?.let(::add)
            }

            // Ensure only complete data sets are returned.
            if (cancel()) {
                clear()
            }
        }
    } ?: emptySet()

// See the developer notes in the DataQuery interface documentation.
private fun MimeType.dataWhere(): Where? = when (this) {
    MimeType.PHONE, MimeType.EMAIL, MimeType.ADDRESS -> null
    else -> Fields.MimeType equalTo this
}

private fun MimeType.contentUri(): Uri = when (this) {
    MimeType.PHONE -> ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    MimeType.EMAIL -> ContactsContract.CommonDataKinds.Email.CONTENT_URI
    MimeType.ADDRESS -> ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI
    else -> Table.DATA.uri
}