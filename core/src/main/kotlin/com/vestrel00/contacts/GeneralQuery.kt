package com.vestrel00.contacts

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.cursor.contactsCursor
import com.vestrel00.contacts.util.isEmpty
import com.vestrel00.contacts.util.query
import com.vestrel00.contacts.util.toRawContactsWhere
import com.vestrel00.contacts.util.unsafeLazy

/**
 * A generalized version of [Query], that lets the Contacts Provider perform the search using its
 * own custom matching algorithm.
 *
 * This type of query is the basis of an app that does a broad search of the Contacts Provider. The
 * technique is useful for apps that want to implement functionality similar to the People app's
 * contact list screen.
 *
 * See https://developer.android.com/training/contacts-provider/retrieve-names#GeneralMatch
 *
 * If you need more granularity and customizations when providing matching criteria, use [Query].
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
 * any Contact data (e.g. name, email, address, phone, etc) matches the search term "john", ordered
 * by the Contact display name primary (given name first) in ascending order (ignoring case).  Only
 * Contacts with at least one RawContact belonging to the given account and groups are included.
 * Only the full name and email address attributes of the [Contact] objects are included.
 *
 * ```kotlin
 * import com.vestrel00.contacts.Fields.Name
 * import com.vestrel00.contacts.Fields.Address
 * import com.vestrel00.contacts.ContactsFields.DisplayNamePrimary
 *
 * val contacts : List<Contact> = generalQuery.
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
 * ## Which Contact data are matched and how?
 *
 * Most, but not all, Contact data are included in the matching process probably because some data
 * may result in unintentional matching.
 *
 * TODO just mention [Fields.forMatching]
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
interface GeneralQuery {

    /**
     * If [includeBlanks] is set to true, then queries may include blank RawContacts. Otherwise,
     * blanks are not guaranteed to be included. This flag is set to true by default, which results
     * in  more database queries so setting this to false will increase performance, especially for
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
    fun includeBlanks(includeBlanks: Boolean): GeneralQuery

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
    fun accounts(vararg accounts: Account?): GeneralQuery

    /**
     * See [GeneralQuery.accounts]
     */
    fun accounts(accounts: Collection<Account?>): GeneralQuery

    /**
     * See [GeneralQuery.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): GeneralQuery

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
    fun groups(vararg groups: Group): GeneralQuery

    /**
     * See [GeneralQuery.groups]
     */
    fun groups(groups: Collection<Group>): GeneralQuery

    /**
     * See [GeneralQuery.groups]
     */
    fun groups(groups: Sequence<Group>): GeneralQuery

    /**
     * Includes the given set of [fields] from [Fields] ([DataFields]) in the resulting contact
     * object(s).
     *
     * If no fields are specified, then all fields are included. Otherwise, only the specified
     * fields will be included in addition to [Fields.Required], which are always included.
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
     * ## IMPORTANT
     *
     * Do not perform updates on contacts returned by a query where all fields are not included as
     * it may result in data loss!
     */
    fun include(vararg fields: AbstractDataField): GeneralQuery

    /**
     * See [GeneralQuery.include].
     */
    fun include(fields: Collection<AbstractDataField>): GeneralQuery

    /**
     * See [GeneralQuery.include].
     */
    fun include(fields: Sequence<AbstractDataField>): GeneralQuery

    /**
     * Filters the [Contact]s partially matching the [searchString]. If not specified or null or
     * empty, then all [Contact]s are returned.
     *
     * For more info, see [GeneralQuery] **Which Contact data are matched and how?** section.
     *
     * ## Performance
     *
     * This may require one or more additional queries, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun whereAnyContactDataPartiallyMatches(searchString: String?): GeneralQuery

    /**
     * Orders the [Contact]s using one or more [orderBy]s. If not specified, then contacts are
     * ordered by ID in ascending order.
     *
     * String comparisons ignores case by default. Each [orderBy]s provides `ignoreCase` as an
     * optional parameter.
     *
     * Use [ContactsFields] to construct the [orderBy].
     *
     * If you need to sort a collection of [Contact] **objects** retrieved from this query using any
     * field from [Fields], use the ContactsComparator extension functions.
     */
    fun orderBy(vararg orderBy: OrderBy<ContactsField>): GeneralQuery

    /**
     * See [GeneralQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<ContactsField>>): GeneralQuery

    /**
     * See [GeneralQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): GeneralQuery

    /**
     * Limits the maximum number of returned [Contact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     */
    fun limit(limit: Int): GeneralQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     */
    fun offset(offset: Int): GeneralQuery

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
internal fun GeneralQuery(context: Context): GeneralQuery = GeneralQueryImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class GeneralQueryImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,

    private var includeBlanks: Boolean = DEFAULT_INCLUDE_BLANKS,
    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var groupMembershipWhere: Where<GroupMembershipField>? = DEFAULT_GROUP_MEMBERSHIP_WHERE,
    private var include: Include<AbstractDataField> = DEFAULT_INCLUDE,
    private var searchString: String? = DEFAULT_SEARCH_STRING,
    private var orderBy: CompoundOrderBy<ContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET
) : GeneralQuery {

    override fun toString(): String {
        return """
            includeBlanks = $includeBlanks
            rawContactsWhere = $rawContactsWhere
            groupMembershipWhere = $groupMembershipWhere
            include = $include
            searchString = $searchString
            orderBy = $orderBy
            limit = $limit
            offset = $offset
        """.trimIndent()
    }

    override fun includeBlanks(includeBlanks: Boolean): GeneralQuery = apply {
        this.includeBlanks = includeBlanks
    }

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): GeneralQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()
    }

    override fun groups(vararg groups: Group) = groups(groups.asSequence())

    override fun groups(groups: Collection<Group>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<Group>): GeneralQuery = apply {
        val groupIds = groups.map { it.id }.filterNotNull()
        groupMembershipWhere = if (groupIds.isEmpty()) {
            DEFAULT_GROUP_MEMBERSHIP_WHERE
        } else {
            Fields.GroupMembership.GroupId `in` groupIds
        }
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): GeneralQuery = apply {
        include = if (fields.isEmpty()) {
            DEFAULT_INCLUDE
        } else {
            Include(fields + REQUIRED_INCLUDE_FIELDS)
        }
    }

    override fun whereAnyContactDataPartiallyMatches(searchString: String?): GeneralQuery = apply {
        // Yes, I know DEFAULT_SEARCH_STRING is null. This reads better though.
        this.searchString = searchString ?: DEFAULT_SEARCH_STRING
    }

    override fun orderBy(vararg orderBy: OrderBy<ContactsField>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): GeneralQuery = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun limit(limit: Int): GeneralQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw IllegalArgumentException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): GeneralQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw IllegalArgumentException("Offset must be greater than or equal to 0")
        }
    }

    override fun find(): List<Contact> = find { false }

    override fun find(cancel: () -> Boolean): List<Contact> {
        if (!permissions.canQuery()) {
            return emptyList()
        }

        return contentResolver.resolve(
            includeBlanks, rawContactsWhere, groupMembershipWhere, include, searchString,
            orderBy, limit, offset, cancel
        )
    }

    private companion object {
        const val DEFAULT_INCLUDE_BLANKS = true
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val DEFAULT_GROUP_MEMBERSHIP_WHERE: Where<GroupMembershipField>? = null
        val DEFAULT_INCLUDE by unsafeLazy { Include(Fields) }
        val REQUIRED_INCLUDE_FIELDS by unsafeLazy { Fields.Required.all.asSequence() }
        val DEFAULT_SEARCH_STRING: String? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(ContactsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
    }
}

private fun ContentResolver.resolve(
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

    return resolve(contactIds, includeBlanks, include, orderBy, limit, offset, cancel)
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
