package contacts.core

import android.accounts.Account
import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import contacts.core.entities.Contact
import contacts.core.entities.Group
import contacts.core.entities.cursor.contactsCursor
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.util.isEmpty
import contacts.core.util.query
import contacts.core.util.toRawContactsWhere
import contacts.core.util.unsafeLazy

/**
 * A generalized version of [Query], that lets the Contacts Provider perform the search using its
 * own custom matching algorithm. It allows you to get the exact same search results as the native
 * Contacts app!
 *
 * This type of query is the basis of an app that does a broad search of the Contacts Provider. The
 * technique is useful for apps that want to implement functionality similar to the People app's
 * contact list screen.
 *
 * Custom mimetypes / data are excluded from the search! Use [Query] to search for contacts using
 * custom data.
 *
 * If you need more granularity and customizations when providing matching criteria, use [Query].
 * For example, getting a Contact by ID is not supported by [BroadQuery] but can be achieved by
 * [Query].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. If not granted, the query will do nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example query that returns the first 10 [Contact]s, skipping the first 5, where the
 * any Contact data (e.g. name, email, address, phone, etc) matches the search term "john", ordered
 * by the Contact display name primary (given name first) in ascending order (ignoring case).  Only
 * Contacts with at least one RawContact belonging to the given account and groups are included.
 * Only the full name and email address attributes of the [Contact] objects are included.
 *
 * ```kotlin
 * import contacts.core.Fields.Name
 * import contacts.core.Fields.Address
 * import contacts.core.ContactsFields.DisplayNamePrimary
 *
 * val contacts : List<Contact> = broadQuery.
 *      .accounts(account)
 *      .groups(groups)
 *      .include(Name, Address)
 *      .whereAnyContactDataPartiallyMatches("john")
 *      .orderBy(DisplayNamePrimary.asc())
 *      .offset(5)
 *      .limit(10)
 *      .find()
 * ```
 *
 * ## How does the matching process work?
 *
 * This query lets the Contacts Provider perform the search using its own custom matching algorithm.
 *
 * Most, but not all, Contact data are included in the matching process. Some are not probably
 * because some data may result in unintentional matching.
 *
 * See [AbstractDataFieldSet.forMatching] documentation on all the fields that are included in
 * this match.
 *
 * Data matching is more sophisticated under the hood than [Query]. The Contacts Provider matches
 * parts of several types of data in segments. For example, a Contact having the email
 * "hologram@gram.net" will be matched with the following texts;
 *
 * - h
 * - HOLO
 * - @g
 * - @gram.net
 * - gram@
 * - net
 * - holo.net
 * - hologram.net
 *
 * But will NOT be matched with the following texts;
 *
 * - olo
 * - @
 * - gram@gram
 * - am@gram.net
 *
 * Similarly, a Contact having the name "Zack Air" will be matched with the following texts;
 *
 * - z
 * - zack
 * - zack, air
 * - air, zack
 * - za a
 * - , z
 * - , a
 * - ,a
 *
 * But will NOT be matched with the following texts;
 *
 * - ack
 * - ir
 * - ,
 *
 * Another example is a Contact having the note "Lots   of   spa        ces." will be matched with
 * the following texts;
 *
 * - l
 * - lots
 * - lots of
 * - of lots
 * - ces spa       lots of.
 * - lo o sp ce . . . . .
 *
 * But will NOT be matched with the following texts;
 *
 * - .
 * - ots
 *
 * Several types of data are matched in segments. E.G. A Contact with display name "Bell Zee" and
 * phone numbers "987", "1 23", and "456" will be matched with "be bell ze 9 123 1 98 456".
 *
 * Matching is **case-insensitive** (case is ignored).
 */
interface BroadQuery {

    /**
     * If [includeBlanks] is set to true, then queries may include blank RawContacts. Otherwise,
     * blanks are not guaranteed to be included. This flag is set to true by default, which results
     * in more database queries so setting this to false will increase performance, especially for
     * large Contacts databases.
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
    fun includeBlanks(includeBlanks: Boolean): BroadQuery

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
    fun accounts(vararg accounts: Account?): BroadQuery

    /**
     * See [BroadQuery.accounts]
     */
    fun accounts(accounts: Collection<Account?>): BroadQuery

    /**
     * See [BroadQuery.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): BroadQuery

    /**
     * Limits the search to only those RawContacts associated with at least one of the given groups.
     * Contacts returned may still contain RawContacts / data that belongs to other groups not
     * specified in [groups] because Contacts may be made up of more than one RawContact from
     * different Groups. This is the same behavior as the native Contacts app.
     *
     * If no groups are specified (this function is not called or called with no Groups), then all
     * RawContacts of Contacts are included in the search.
     *
     * ## Performance
     *
     * This may require one or more additional queries, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun groups(vararg groups: Group): BroadQuery

    /**
     * See [BroadQuery.groups]
     */
    fun groups(groups: Collection<Group>): BroadQuery

    /**
     * See [BroadQuery.groups]
     */
    fun groups(groups: Sequence<Group>): BroadQuery

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
    fun include(vararg fields: AbstractDataField): BroadQuery

    /**
     * See [BroadQuery.include].
     */
    fun include(fields: Collection<AbstractDataField>): BroadQuery

    /**
     * See [BroadQuery.include].
     */
    fun include(fields: Sequence<AbstractDataField>): BroadQuery

    /**
     * Filters the [Contact]s partially matching the [searchString]. If not specified or null or
     * empty, then all [Contact]s are returned.
     *
     * For more info, see [BroadQuery] **How does the matching process work?** section.
     *
     * ## Performance
     *
     * This may require one or more additional queries, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun whereAnyContactDataPartiallyMatches(searchString: String?): BroadQuery

    /**
     * Orders the [Contact]s using one or more [orderBy]s. If not specified, then contacts are
     * ordered by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     *
     * Use [ContactsFields] to construct the [orderBy].
     *
     * If you need to sort a collection of Contacts outside of a database query using any field
     * (in addition to [ContactsFields]), use [contacts.core.util.ContactsComparator].
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
    fun orderBy(vararg orderBy: OrderBy<ContactsField>): BroadQuery

    /**
     * See [BroadQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<ContactsField>>): BroadQuery

    /**
     * See [BroadQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): BroadQuery

    /**
     * Limits the maximum number of returned [Contact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): BroadQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): BroadQuery

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
    fun find(): List<Contact>

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
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun find(cancel: () -> Boolean = { false }): List<Contact>
    fun find(cancel: () -> Boolean): List<Contact>
}

@Suppress("FunctionName")
internal fun BroadQuery(contacts: Contacts): BroadQuery = BroadQueryImpl(
    contacts.applicationContext.contentResolver,
    contacts.permissions,
    contacts.customDataRegistry
)

private class BroadQueryImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val customDataRegistry: CustomDataRegistry,

    private var includeBlanks: Boolean = DEFAULT_INCLUDE_BLANKS,
    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var groupMembershipWhere: Where<GroupMembershipField>? = DEFAULT_GROUP_MEMBERSHIP_WHERE,
    private var include: Include<AbstractDataField> = allDataFields(customDataRegistry),
    private var searchString: String? = DEFAULT_SEARCH_STRING,
    private var orderBy: CompoundOrderBy<ContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : BroadQuery {

    override fun toString(): String =
        """
            BroadQuery {
                includeBlanks: $includeBlanks
                rawContactsWhere: $rawContactsWhere
                groupMembershipWhere: $groupMembershipWhere
                include: $include
                searchString: $searchString
                orderBy: $orderBy
                limit: $limit
                offset: $offset
            }
        """.trimIndent()

    override fun includeBlanks(includeBlanks: Boolean): BroadQuery = apply {
        this.includeBlanks = includeBlanks
    }

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): BroadQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()
    }

    override fun groups(vararg groups: Group) = groups(groups.asSequence())

    override fun groups(groups: Collection<Group>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<Group>): BroadQuery = apply {
        val groupIds = groups.mapNotNull { it.id }
        groupMembershipWhere = if (groupIds.isEmpty()) {
            DEFAULT_GROUP_MEMBERSHIP_WHERE
        } else {
            Fields.GroupMembership.GroupId `in` groupIds
        }
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): BroadQuery = apply {
        include = if (fields.isEmpty()) {
            allDataFields(customDataRegistry)
        } else {
            Include(fields + REQUIRED_INCLUDE_FIELDS)
        }
    }

    override fun whereAnyContactDataPartiallyMatches(searchString: String?): BroadQuery = apply {
        // Yes, I know DEFAULT_SEARCH_STRING is null. This reads better though.
        this.searchString = searchString ?: DEFAULT_SEARCH_STRING
    }

    override fun orderBy(vararg orderBy: OrderBy<ContactsField>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): BroadQuery = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun limit(limit: Int): BroadQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw IllegalArgumentException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): BroadQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw IllegalArgumentException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): List<Contact> = find { false }

    override fun find(cancel: () -> Boolean): List<Contact> {
        if (!permissions.canQuery) {
            return emptyList()
        }

        return contentResolver.resolve(
            customDataRegistry,
            includeBlanks, rawContactsWhere, groupMembershipWhere, include, searchString,
            orderBy, limit, offset, cancel
        )
    }

    private companion object {
        const val DEFAULT_INCLUDE_BLANKS = true
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val DEFAULT_GROUP_MEMBERSHIP_WHERE: Where<GroupMembershipField>? = null
        val REQUIRED_INCLUDE_FIELDS by unsafeLazy { Fields.Required.all.asSequence() }
        val DEFAULT_SEARCH_STRING: String? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(ContactsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private fun ContentResolver.resolve(
    customDataRegistry: CustomDataRegistry,
    includeBlanks: Boolean,
    rawContactsWhere: Where<RawContactsField>?,
    groupMembershipWhere: Where<GroupMembershipField>?,
    include: Include<AbstractDataField>,
    searchString: String?,
    orderBy: CompoundOrderBy<ContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<Contact> {

    var contactIds: MutableSet<Long>? = null

    // Get Contact Ids partially matching the searchString from the Contacts table. If searchString
    // is null, skip.
    if (searchString != null && searchString.isNotEmpty() && !cancel()) {
        contactIds = mutableSetOf<Long>().apply {
            addAll(findContactIdsInContactsTable(searchString, cancel))
        }

        // If no match, return empty list.
        if (contactIds.isEmpty() || cancel()) {
            return emptyList()
        }
    }

    // Get the Contact Ids matching groupMembershipWhere and contained in the contactIds from the
    // Data table. If groupMembershipWhere is null, skip.
    if (groupMembershipWhere != null && !cancel()) {
        val dataTableWhere = groupMembershipWhere and contactIds?.let {
            Fields.Contact.Id `in` it
        }

        // Intentionally replace the contactsIds instead of adding to it.
        contactIds = mutableSetOf<Long>().apply {
            addAll(findContactIdsInDataTable(dataTableWhere, cancel))
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

private fun ContentResolver.findContactIdsInContactsTable(
    searchString: String, cancel: () -> Boolean
): Set<Long> = query(
    Uri.withAppendedPath(
        ContactsContract.Contacts.CONTENT_FILTER_URI,
        Uri.encode(searchString)
    ),
    Include(ContactsFields.Id),
    null
) {
    val contactIds = mutableSetOf<Long>()
    val contactsCursor = it.contactsCursor()
    while (!cancel() && it.moveToNext()) {
        contactsCursor.contactId?.let(contactIds::add)
    }
    contactIds
} ?: emptySet()
