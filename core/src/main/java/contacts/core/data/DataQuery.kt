package contacts.core.data

import android.accounts.Account
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.*
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.mapper.dataEntityMapperFor
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.util.isEmpty
import contacts.core.util.query
import contacts.core.util.toRawContactsWhere
import contacts.core.util.unsafeLazy

/**
 * Provides new query instances for specific types of Profile OR non-Profile (depending on instance)
 * data.
 */
interface DataQueryFactory {

    /**
     * Queries for [Address]es.
     */
    fun addresses(): DataQuery<AddressField, Address>

    /**
     * Queries for [Email]s.
     */
    fun emails(): DataQuery<EmailField, Email>

    /**
     * Queries for [Event]s.
     */
    fun events(): DataQuery<EventField, Event>

    /**
     * Queries for [GroupMembership]s.
     */
    fun groupMemberships(): DataQuery<GroupMembershipField, GroupMembership>

    /**
     * Queries for [Im]s.
     */
    fun ims(): DataQuery<ImField, Im>

    /**
     * Queries for [Name]s.
     */
    fun names(): DataQuery<NameField, Name>

    /**
     * Queries for [Nickname]s.
     */
    fun nicknames(): DataQuery<NicknameField, Nickname>

    /**
     * Queries for [Note]s.
     */
    fun notes(): DataQuery<NoteField, Note>

    /**
     * Queries for [Organization]s.
     */
    fun organizations(): DataQuery<OrganizationField, Organization>

    /**
     * Queries for [Phone]s.
     */
    fun phones(): DataQuery<PhoneField, Phone>

    // Photos are intentionally left out as it is internal to the library.

    /**
     * Queries for [Relation]s.
     */
    fun relations(): DataQuery<RelationField, Relation>

    /**
     * Queries for [SipAddress]es.
     */
    fun sipAddresses(): DataQuery<SipAddressField, SipAddress>

    /**
     * Queries for [Website]s.
     */
    fun websites(): DataQuery<WebsiteField, Website>

    /**
     * Queries for custom data of type [E] with the given custom [mimeType].
     */
    fun <F : AbstractCustomDataField, E : ExistingCustomDataEntity>
            customData(mimeType: MimeType.Custom): DataQuery<F, E>
}

@Suppress("FunctionName")
internal fun DataQuery(contacts: Contacts, isProfile: Boolean): DataQueryFactory =
    DataQueryFactoryImpl(
        contacts, isProfile
    )

private class DataQueryFactoryImpl(
    private val contacts: Contacts,
    private val isProfile: Boolean
) : DataQueryFactory {

    override fun addresses(): DataQuery<AddressField, Address> = DataQueryImpl(
        contacts, Fields.Address, MimeType.Address, isProfile
    )

    override fun emails(): DataQuery<EmailField, Email> = DataQueryImpl(
        contacts, Fields.Email, MimeType.Email, isProfile
    )

    override fun events(): DataQuery<EventField, Event> = DataQueryImpl(
        contacts, Fields.Event, MimeType.Event, isProfile
    )

    override fun groupMemberships(): DataQuery<GroupMembershipField, GroupMembership> =
        DataQueryImpl(
            contacts, Fields.GroupMembership, MimeType.GroupMembership, isProfile
        )

    override fun ims(): DataQuery<ImField, Im> = DataQueryImpl(
        contacts, Fields.Im, MimeType.Im, isProfile
    )

    override fun names(): DataQuery<NameField, Name> = DataQueryImpl(
        contacts, Fields.Name, MimeType.Name, isProfile
    )

    override fun nicknames(): DataQuery<NicknameField, Nickname> = DataQueryImpl(
        contacts, Fields.Nickname, MimeType.Nickname, isProfile
    )

    override fun notes(): DataQuery<NoteField, Note> = DataQueryImpl(
        contacts, Fields.Note, MimeType.Note, isProfile
    )

    override fun organizations(): DataQuery<OrganizationField, Organization> =
        DataQueryImpl(
            contacts, Fields.Organization, MimeType.Organization, isProfile
        )

    override fun phones(): DataQuery<PhoneField, Phone> = DataQueryImpl(
        contacts, Fields.Phone, MimeType.Phone, isProfile
    )

    override fun relations(): DataQuery<RelationField, Relation> = DataQueryImpl(
        contacts, Fields.Relation, MimeType.Relation, isProfile
    )

    override fun sipAddresses(): DataQuery<SipAddressField, SipAddress> = DataQueryImpl(
        contacts, Fields.SipAddress, MimeType.SipAddress, isProfile
    )

    override fun websites(): DataQuery<WebsiteField, Website> = DataQueryImpl(
        contacts, Fields.Website, MimeType.Website, isProfile
    )

    @Suppress("UNCHECKED_CAST")
    override fun <F : AbstractCustomDataField, E : ExistingCustomDataEntity>
            customData(mimeType: MimeType.Custom): DataQuery<F, E> = DataQueryImpl(
        contacts,
        contacts.customDataRegistry.entryOf(mimeType).fieldSet as AbstractCustomDataFieldSet<F>,
        mimeType, isProfile
    )
}

/**
 * Queries the Data table and returns one or more Profile OR non-Profile (depending on instance)
 * data of type [E] matching the search criteria.
 *
 * This returns a list of specific data type (e.g. emails, phones, etc). This is optimized and
 * useful for searching through and paginating one data type.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. If not granted, the query will do nothing and return an empty list.
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
 */
interface DataQuery<F : DataField, E : ExistingDataEntity> {

    /**
     * Limits this query to only search for data associated with one of the given [accounts].
     *
     * If no accounts are specified (this function is not called or called with no Accounts), then
     * all data from all accounts are searched.
     *
     * A null [Account] may be provided here, which results in data belonging to RawContacts with no
     * associated Account to be included in the search. RawContacts without an associated account
     * are considered local or device-only contacts, which are not synced.
     */
    fun accounts(vararg accounts: Account?): DataQuery<F, E>

    /**
     * See [DataQuery.accounts]
     */
    fun accounts(accounts: Collection<Account?>): DataQuery<F, E>

    /**
     * See [DataQuery.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): DataQuery<F, E>

    /**
     * Includes the given set of [fields] of type [F] in the resulting data objects of type [E].
     *
     * If no fields are specified, then all fields are included. Otherwise, only the specified
     * fields will be included in addition to [Fields.Required], which are always included.
     *
     * Fields that are included will not guarantee non-null attributes in the returned entity
     * instances.
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
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
     * val emails = emailsQuery.include(Fields.Email.Address).find()
     * val mutableEmails = setEmailAddresses(emails)
     * dataUpdate.data(mutableEmails).include(Fields.Email.Address).commit()
     * ```
     *
     * On the other hand, you may intentionally include only some data and perform updates without
     * on all data (not just the included ones) to effectively delete all non-included data. This
     * is, currently, a feature- not a bug! For example, in order to get and set only given name
     * and set all other data to null (such as given name, middle name, prefix) in the database..
     *
     * ```kotlin
     * val names = namesQuery.include(Fields.Name.GivenName).find()
     * val mutableNames = setGivenNames(names)
     * dataUpdate.data(mutableNames).include(Fields.Name.all).commit()
     * ```
     *
     * This gives you the most flexibility when it comes to specifying what fields to
     * include/exclude in queries, inserts, and update, which will allow you to do things beyond
     * your wildest imagination!
     */
    fun include(vararg fields: F): DataQuery<F, E>

    /**
     * See [DataQuery.include].
     */
    fun include(fields: Collection<F>): DataQuery<F, E>

    /**
     * See [DataQuery.include].
     */
    fun include(fields: Sequence<F>): DataQuery<F, E>

    /**
     * Filters the returned data matching the criteria defined by the [where].
     *
     * Be careful what fields are used in this where. Querying for all addresses where the phone
     * number starts with whatever will produce no results. Think about it =)
     *
     * If not specified or null, then all data of type [E] is returned.
     *
     * ## Developer notes
     *
     * The Field type of the Where is not constrained to [F] because consumers need to be able to
     * use other fields such as [Fields.Contact] (perhaps to get all data of type [E] of a Contact),
     * [Fields.RawContact], [Fields.IsSuperPrimary], etc...
     *
     * This allows consumers to make a mistake about trying to match addresses using phone fields.
     * At this point, I'll say we are consenting adults (Python motto if you don't know).
     */
    fun where(where: Where<AbstractDataField>?): DataQuery<F, E>

    /**
     * Orders the returned data using one or more [orderBy]s. If not specified, then data is ordered
     * by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     */
    @SafeVarargs
    fun orderBy(vararg orderBy: OrderBy<F>): DataQuery<F, E>

    /**
     * See [DataQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<F>>): DataQuery<F, E>

    /**
     * See [DataQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<F>>): DataQuery<F, E>

    /**
     * Limits the maximum number of returned data to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): DataQuery<F, E>

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): DataQuery<F, E>

    /**
     * The list of [E]s.
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
    fun find(): List<E>

    /**
     * The list of [E]s.
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
    fun find(cancel: () -> Boolean): List<E>
}

private class DataQueryImpl<F : DataField, E : ExistingDataEntity>(
    private val contacts: Contacts,

    private val defaultIncludeFields: FieldSet<F>,
    private val mimeType: MimeType,
    private val isProfile: Boolean,

    // Yes, the Include, Where, and OrderBy Field types are all AbstractDataField, not T.
    // The type T is mainly used to constrict consumers, not implementors (us).
    // This allows us to append the RequiredDataFields, which casts T to AbstractDataField.
    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include<AbstractDataField> =
        Include(defaultIncludeFields.all + REQUIRED_INCLUDE_FIELDS),
    private var where: Where<AbstractDataField>? = DEFAULT_WHERE,
    private var orderBy: CompoundOrderBy<AbstractDataField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : DataQuery<F, E> {

    override fun toString(): String =
        """
            DataQuery {                
                mimeType: $mimeType                
                isProfile: $isProfile
                rawContactsWhere: $rawContactsWhere
                include: $include
                where: $where
                orderBy: $orderBy
                limit: $limit
                offset: $offset
            }
        """.trimIndent()

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): DataQuery<F, E> = apply {
        rawContactsWhere = accounts.toRawContactsWhere()
    }

    override fun include(vararg fields: F) = include(fields.asSequence())

    override fun include(fields: Collection<F>) = include(fields.asSequence())

    override fun include(fields: Sequence<F>): DataQuery<F, E> = apply {
        val includeFields = if (fields.isEmpty()) {
            defaultIncludeFields.all.asSequence()
        } else {
            fields
        }

        include = Include(includeFields + REQUIRED_INCLUDE_FIELDS)
    }

    override fun where(where: Where<AbstractDataField>?): DataQuery<F, E> = apply {
        // Yes, I know DEFAULT_WHERE is null. This reads better though.
        this.where = where ?: DEFAULT_WHERE
    }

    override fun orderBy(vararg orderBy: OrderBy<F>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<F>>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<F>>): DataQuery<F, E> = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun limit(limit: Int): DataQuery<F, E> = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): DataQuery<F, E> = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): List<E> = find { false }

    override fun find(cancel: () -> Boolean): List<E> {
        if (!contacts.permissions.canQuery()) {
            return emptyList()
        }

        return contacts.resolveDataEntity(
            isProfile, mimeType, rawContactsWhere, include, where, orderBy, limit, offset, cancel
        )
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val REQUIRED_INCLUDE_FIELDS by unsafeLazy { Fields.Required.all.asSequence() }
        val DEFAULT_WHERE: Where<AbstractDataField>? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(Fields.DataId.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

internal fun <T : ExistingDataEntity> Contacts.resolveDataEntity(
    isProfile: Boolean,
    mimeType: MimeType,
    rawContactsWhere: Where<RawContactsField>?,
    include: Include<AbstractDataField>,
    where: Where<AbstractDataField>?,
    orderBy: CompoundOrderBy<AbstractDataField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<T> {

    // var dataWhere: Where<AbstractDataField>? = mimeType.dataWhere()
    var dataWhere: Where<AbstractDataField> = Fields.MimeType equalTo mimeType

    if (rawContactsWhere != null) {
        // Limit the data to the set associated with the RawContacts found in the RawContacts
        // table matching the rawContactsWhere.
        val rawContactIds = applicationContext.contentResolver
            .findRawContactIdsInRawContactsTable(isProfile, rawContactsWhere, cancel)

        dataWhere = dataWhere and (Fields.RawContact.Id `in` rawContactIds)
    }

    if (where != null) {
        dataWhere = dataWhere and where
    }

    return applicationContext.contentResolver.query(
        // mimeType.contentUri(),
        if (isProfile) ProfileUris.DATA.uri else Table.Data.uri,
        include, dataWhere, "$orderBy LIMIT $limit OFFSET $offset"
    ) {
        mutableListOf<T>().apply {
            val entityMapper = it.dataEntityMapperFor<T>(mimeType, customDataRegistry)
            while (!cancel() && it.moveToNext()) {
                // Do not add blanks.
                entityMapper.nonBlankValueOrNull?.let(::add)
            }

            // Ensure only complete data sets are returned.
            if (cancel()) {
                clear()
            }
        }
    } ?: emptyList()
}

private fun ContentResolver.findRawContactIdsInRawContactsTable(
    isProfile: Boolean,
    rawContactsWhere: Where<RawContactsField>,
    cancel: () -> Boolean
): Set<Long> =
    query(
        if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
        Include(RawContactsFields.Id),
        rawContactsWhere
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

/*
Phones, Emails, and Addresses have a CONTENT_URI that contains all rows consisting of only those
data kinds. Other data kinds do not have this content uri. These probably exists as an index /
for optimization since phones, emails, and addresses are the most frequently used data kinds. Using
these CONTENT_URIs probably results in shorter search times since it only has to look through a
subset of data instead of the entire data table.

private fun MimeType.dataWhere(): Where<AbstractDataField>? = when (this) {
    MimeType.PHONE, MimeType.EMAIL, MimeType.ADDRESS -> null
    else -> Fields.MimeType equalTo this
}

However, the content uris for phone, email, and address can only contain non-Profile data. Also,
Uri.withAppendedPath(ProfileUris.DATA.uri, "phones") throws an exception.

Therefore, we just end up using the generic profile and non-profile data table.

private fun MimeType.contentUri(): Uri = when (this) {
    // I'm aware that CONTENT_FILTER_URI exist for PHONE and EMAIL. We are not using that here
    // because it does not support a selection (where) clause.
    MimeType.PHONE -> ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    MimeType.EMAIL -> ContactsContract.CommonDataKinds.Email.CONTENT_URI
    MimeType.ADDRESS -> ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI
    else -> Table.Data.uri
}
 */