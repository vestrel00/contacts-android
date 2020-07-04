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
 * Provides new query instances for specific types of data.
 */
interface DataQuery {

    /**
     * Queries for [Address]es.
     */
    fun addresses(): CommonDataQuery<AddressField, Address>

    /**
     * Queries for [Email]s.
     */
    fun emails(): CommonDataQuery<EmailField, Email>

    /**
     * Queries for [Event]s.
     */
    fun events(): CommonDataQuery<EventField, Event>

    /**
     * Queries for [GroupMembership]s.
     */
    fun groupMemberships(): CommonDataQuery<GroupMembershipField, GroupMembership>

    /**
     * Queries for [Im]s.
     */
    fun ims(): CommonDataQuery<ImField, Im>

    /**
     * Queries for [Name]s.
     */
    fun names(): CommonDataQuery<NameField, Name>

    /**
     * Queries for [Nickname]s.
     */
    fun nicknames(): CommonDataQuery<NicknameField, Nickname>

    /**
     * Queries for [Note]s.
     */
    fun notes(): CommonDataQuery<NoteField, Note>

    /**
     * Queries for [Organization]s.
     */
    fun organizations(): CommonDataQuery<OrganizationField, Organization>

    /**
     * Queries for [Phone]s.
     */
    fun phones(): CommonDataQuery<PhoneField, Phone>

    /**
     * Queries for [Relation]s.
     */
    fun relations(): CommonDataQuery<RelationField, Relation>

    /**
     * Queries for [SipAddress]es.
     */
    fun sipAddresses(): CommonDataQuery<SipAddressField, SipAddress>

    /**
     * Queries for [Website]s.
     */
    fun websites(): CommonDataQuery<WebsiteField, Website>
}

@Suppress("FunctionName")
internal fun DataQuery(context: Context): DataQuery = DataQueryImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class DataQueryImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions
) : DataQuery {

    override fun addresses(): CommonDataQuery<AddressField, Address> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Address, MimeType.ADDRESS
    )

    override fun emails(): CommonDataQuery<EmailField, Email> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Email, MimeType.EMAIL
    )

    override fun events(): CommonDataQuery<EventField, Event> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Event, MimeType.EVENT
    )

    override fun groupMemberships(): CommonDataQuery<GroupMembershipField, GroupMembership> =
        CommonDataQueryImpl(
            contentResolver, permissions, Fields.GroupMembership, MimeType.GROUP_MEMBERSHIP
        )

    override fun ims(): CommonDataQuery<ImField, Im> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Im, MimeType.IM
    )

    override fun names(): CommonDataQuery<NameField, Name> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Name, MimeType.NAME
    )

    override fun nicknames(): CommonDataQuery<NicknameField, Nickname> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Nickname, MimeType.NICKNAME
    )

    override fun notes(): CommonDataQuery<NoteField, Note> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Note, MimeType.NOTE
    )

    override fun organizations(): CommonDataQuery<OrganizationField, Organization> =
        CommonDataQueryImpl(
            contentResolver, permissions, Fields.Organization, MimeType.ORGANIZATION
        )

    override fun phones(): CommonDataQuery<PhoneField, Phone> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Phone, MimeType.PHONE
    )

    override fun relations(): CommonDataQuery<RelationField, Relation> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Relation, MimeType.RELATION
    )

    override fun sipAddresses(): CommonDataQuery<SipAddressField, SipAddress> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.SipAddress, MimeType.SIP_ADDRESS
    )

    override fun websites(): CommonDataQuery<WebsiteField, Website> = CommonDataQueryImpl(
        contentResolver, permissions, Fields.Website, MimeType.WEBSITE
    )
}

/**
 * Queries the Data table and returns one or more data of type [R] matching the search criteria.
 *
 * This returns a list of specific data type (e.g. emails, phones, etc). This is optimized and
 * useful for searching through and paginating one data type.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All queries will return an empty list or null result if the permission
 * is not granted.
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
 * val addresses : List<Address> = addressesQuery
 *      .accounts(account)
 *      .include(Fields.Address.FormattedAddress)
 *      .where((Fields.Address.Country equalTo "US") and (Fields.Address.PostCode startsWith "78"))
 *      .orderBy(Fields.Address.PostCode.asc())
 *      .offset(5)
 *      .limit(10)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ```java
 * List<Address> addresses = addressesQuery
 *      .accounts(account)
 *      .include(Address.FormattedAddress)
 *      .where(equalTo(Address.Country, "US").and(startsWith(Address.PostCode, "78")))
 *      .orderBy(Address.PostCode.asc())
 *      .offset(5)
 *      .limit(10)
 *      .find();
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
interface CommonDataQuery<T : CommonDataField, R : CommonDataEntity> {

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
    fun accounts(vararg accounts: Account?): CommonDataQuery<T, R>

    /**
     * See [CommonDataQuery.accounts]
     */
    fun accounts(accounts: Collection<Account?>): CommonDataQuery<T, R>

    /**
     * See [CommonDataQuery.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): CommonDataQuery<T, R>

    /**
     * Includes the given set of [fields] of type [T] in the resulting data objects of type [R].
     *
     * If no fields are specified, then all fields are included. Otherwise, only the specified
     * fields will be included in addition to [Fields.Required], which are always included.
     *
     * Fields that are included will not guarantee non-null attributes in the returned entity
     * object instances.
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     */
    fun include(vararg fields: T): CommonDataQuery<T, R>

    /**
     * See [CommonDataQuery.include].
     */
    fun include(fields: Collection<T>): CommonDataQuery<T, R>

    /**
     * See [CommonDataQuery.include].
     */
    fun include(fields: Sequence<T>): CommonDataQuery<T, R>

    /**
     * Filters the returned data matching the criteria defined by the [where].
     *
     * Be careful what fields are used in this where. Querying for all addresses where the phone
     * number starts with whatever will produce no results. Think about it =)
     *
     * If not specified or null, then all data of type [R] is returned.
     *
     * ## Developer notes
     *
     * The Field type of the Where is not constrained to [T] because consumers need to be able to
     * use other fields such as [Fields.Contact] (perhaps to get all data of type [R] of a Contact),
     * [Fields.RawContact], [Fields.IsSuperPrimary], etc...
     *
     * This allows consumers to make a mistake about trying to match addresses using phone fields.
     * At this point, I'll say we are consenting adults (Python motto if you don't know) and we need
     * apply some common sense =)
     */
    fun where(where: Where<AbstractDataField>?): CommonDataQuery<T, R>

    /**
     * Orders the returned data using one or more [orderBy]s. If not specified, then data is ordered
     * by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     */
    fun orderBy(vararg orderBy: OrderBy<T>): CommonDataQuery<T, R>

    /**
     * See [CommonDataQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<T>>): CommonDataQuery<T, R>

    /**
     * See [CommonDataQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<T>>): CommonDataQuery<T, R>

    /**
     * Limits the maximum number of returned data to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): CommonDataQuery<T, R>

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): CommonDataQuery<T, R>

    /**
     * The list of [R]s.
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
    fun find(): List<R>

    /**
     * The list of [R]s.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION].
     *
     * ## Cancellation
     *
     * The number of data processed may be large, which results in this operation to take a while.
     * Therefore, cancellation is supported while the data list is being built. To cancel at any
     * time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun find(cancel: () -> Boolean = { false }): List<R>
    fun find(cancel: () -> Boolean): List<R>
}

private class CommonDataQueryImpl<T : CommonDataField, R : CommonDataEntity>(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,

    private val defaultIncludeFields: FieldSet<T>,
    private val mimeType: MimeType,

    // Yes, the Include, Where, and OrderBy Field types are all AbstractDataField, not T.
    // The type T is mainly used to constrict consumers, not implementors (us).
    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include<AbstractDataField> =
        Include(defaultIncludeFields.all + REQUIRED_INCLUDE_FIELDS),
    private var where: Where<AbstractDataField>? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy<AbstractDataField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : CommonDataQuery<T, R> {

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

    override fun accounts(accounts: Sequence<Account?>): CommonDataQuery<T, R> = apply {
        rawContactsWhere = accounts.toRawContactsWhere()
    }

    override fun include(vararg fields: T) = include(fields.asSequence())

    override fun include(fields: Collection<T>) = include(fields.asSequence())

    override fun include(fields: Sequence<T>): CommonDataQuery<T, R> = apply {
        val includeFields = if (fields.isEmpty()) {
            defaultIncludeFields.all.asSequence()
        } else {
            fields
        }

        include = Include(includeFields + REQUIRED_INCLUDE_FIELDS)
    }

    override fun where(where: Where<AbstractDataField>?): CommonDataQuery<T, R> = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = where ?: DEFAULT_WHERE
    }

    override fun orderBy(vararg orderBy: OrderBy<T>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<T>>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<T>>): CommonDataQuery<T, R> = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun limit(limit: Int): CommonDataQuery<T, R> = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw IllegalArgumentException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): CommonDataQuery<T, R> = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw IllegalArgumentException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): List<R> = find { false }

    override fun find(cancel: () -> Boolean): List<R> {
        if (!permissions.canQuery()) {
            return emptyList()
        }

        return contentResolver.resolveDataEntity(
            mimeType, rawContactsWhere, include, where, orderBy, limit, offset, cancel
        )
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val REQUIRED_INCLUDE_FIELDS = Fields.Required.all.asSequence()
        val DEFAULT_WHERE: Where<AbstractDataField>? = null
        val DEFAULT_ORDER_BY = CompoundOrderBy(setOf(Fields.DataId.asc()))
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

internal fun <T : CommonDataEntity> ContentResolver.resolveDataEntity(
    mimeType: MimeType,
    rawContactsWhere: Where<RawContactsField>?,
    include: Include<AbstractDataField>,
    where: Where<AbstractDataField>?,
    orderBy: CompoundOrderBy<AbstractDataField>,
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
    rawContactsWhere: Where<RawContactsField>, cancel: () -> Boolean
): Set<Long> =
    query(
        Table.RawContacts, Include(RawContactsFields.Id), rawContactsWhere
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
private fun MimeType.dataWhere(): Where<AbstractDataField>? = when (this) {
    MimeType.PHONE, MimeType.EMAIL, MimeType.ADDRESS -> null
    else -> Fields.MimeType equalTo this
}

private fun MimeType.contentUri(): Uri = when (this) {
    // I'm aware that CONTENT_FILTER_URI exist for PHONE and EMAIL. We are not using that here
    // because it does not support a selection (where) clause.
    MimeType.PHONE -> ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    MimeType.EMAIL -> ContactsContract.CommonDataKinds.Email.CONTENT_URI
    MimeType.ADDRESS -> ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI
    else -> Table.Data.uri
}