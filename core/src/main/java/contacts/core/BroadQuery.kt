package contacts.core

import android.accounts.Account
import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import contacts.core.BroadQuery.Match
import contacts.core.entities.Contact
import contacts.core.entities.Group
import contacts.core.entities.cursor.contactsCursor
import contacts.core.entities.cursor.dataCursor
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.util.*

/**
 * A generalized version of [Query], that lets the Contacts Provider perform the search using its
 * own custom matching algorithm. It allows you to get the exact same search results as the AOSP
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
 * ## How does the matching process work?
 *
 * See the [Match].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. If not granted, the query will do nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example query that returns the first 10 [Contact]s, skipping the first 5, where the
 * any Contact data (e.g. name, email, address, phone, note, etc) partially matches the search term
 * "john", ordered by the Contact display name primary (given name first) in ascending order
 * (ignoring case). Include only Contacts with at least one RawContact belonging to the given
 * account and groups. Include only the name and email properties of [Contact]s.
 *
 * In Kotlin,
 *
 * ```kotlin
 * val contacts = broadQuery
 *      .accounts(account)
 *      .groups(groups)
 *      .include { Name.all + Address.all }
 *      .wherePartiallyMatches("john")
 *      .orderBy(ContactsFields.DisplayNamePrimary.asc())
 *      .offset(5)
 *      .limit(10)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ```java
 * import static contacts.core.Fields.*;
 * import static contacts.core.OrderByKt.*;
 *
 * List<Contact> contacts = broadQuery
 *      .accounts(account)
 *      .groups(groups)
 *      .include(new ArrayList<>() {{
 *           addAll(Name.getAll());
 *           addAll(Address.getAll());
 *       }})
 *      .wherePartiallyMatches("john")
 *      .orderBy(asc(ContactsFields.DisplayNamePrimary))
 *      .offset(5)
 *      .limit(10)
 *      .find();
 * ```
 */
interface BroadQuery : CrudApi {

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
     * different Groups. This is the same behavior as the AOSP Contacts app.
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
     * Includes only the given set of [fields] in each of the matching contacts.
     *
     * If no fields are specified (empty list), then all fields are included. Otherwise, only
     * the specified fields will be included.
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
     * To include RawContact specific properties, use [BroadQuery.includeRawContactsFields].
     *
     * ## Performance
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     *
     * The most optimal queries only include fields from [Fields.Contact] because no Data table rows
     * need to be processed.
     *
     * ## Including all fields
     *
     * If you want to include all fields, including custom data fields, then passing in an empty
     * list or not invoking this function is the most performant way to do it because internal
     * checks will be disabled (less lines of code executed).
     *
     * ## Developer notes
     *
     * Passing in an empty list here should set the reference to the internal field set to null to
     * indicate that include field checks should be disabled when processing cursor data via
     * implementations of [contacts.core.entities.cursor.AbstractEntityCursor].
     *
     * When the internal field set is set to null, all fields should be included in the projection
     * list of the actual query, which is why the [allFieldsIfNull] functions exist. In order to
     * disable include field checks in the cursors, [contacts.core.util.query] provides a parameter
     * to set the cursor holder's include fields to null.
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
     * See [BroadQuery.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): BroadQuery

    /**
     * Includes [fields] from the RawContacts table corresponding to
     * [contacts.core.entities.RawContact] properties.
     *
     * For all other fields/properties, use [include].
     *
     * If no RawContacts fields are specified (empty list), then all RawContacts fields are
     * included. Otherwise, only the specified fields will be included.
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
     * #### Developer notes
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
    fun includeRawContactsFields(vararg fields: RawContactsField): BroadQuery

    /**
     * See [BroadQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): BroadQuery

    /**
     * See [BroadQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): BroadQuery

    /**
     * See [BroadQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: RawContactsFields.() -> Collection<RawContactsField>): BroadQuery

    /**
     * Specifies the type of contact data that should be used in the matching process. This will
     * affect the search results when [wherePartiallyMatches] is used.
     *
     * The default is [Match.ANY].
     */
    fun match(match: Match): BroadQuery

    /**
     * Filters the [Contact]s partially matching the [searchString]. If not specified or null or
     * empty, then all [Contact]s are returned.
     *
     * Specify the type of contact data that should be used in the matching process using the
     * [match] function.
     *
     * Matching is **case-insensitive** (case is ignored).
     *
     * **Custom data are not included in the matching process!** To match custom data, use [Query].
     *
     * ## Performance
     *
     * This may require one or more additional queries, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun wherePartiallyMatches(searchString: String?): BroadQuery

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
     * See [BroadQuery.orderBy].
     */
    fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>): BroadQuery

    /**
     * Limits the maximum number of returned [Contact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     *
     * Some devices do not support this. See [forceOffsetAndLimit].
     */
    fun limit(limit: Int): BroadQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     *
     * Some devices do not support this. See [forceOffsetAndLimit].
     */
    fun offset(offset: Int): BroadQuery

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
    fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): BroadQuery

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
    override fun redactedCopy(): BroadQuery

    /**
     * Specifies the type of contact data that should be used in the matching process.
     */
    enum class Match {

        /**
         * Any contact data is included in the matching process. This is the default.
         *
         * Use this if you want to get the same results when searching contacts using the
         * AOSP Contacts app and the Google Contacts app.
         *
         * Most, but not all, contact data are included in the matching process.
         * E.G. name, email, phone, address, organization, note, etc.
         *
         * Data matching is more sophisticated under the hood than [Query]. The Contacts Provider
         * matches parts of several types of data in segments. For example, a Contact having the
         * email "hologram@gram.net" will be matched with the following texts;
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
         * Another example is a Contact having the note "Lots   of   spa        ces." will be
         * matched with the following texts;
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
         * Several types of data are matched in segments. E.G. A Contact with display name
         * "Bell Zee" and phone numbers "987", "1 23", and "456" will be matched with
         * "be bell ze 9 123 1 98 456".
         */
        ANY,

        /**
         * Only phones or (contact display name + any phones) are included in the matching process.
         *
         * Use this if you want to get contacts that have a matching phone number or matching
         * ([Contact.displayNamePrimary] + any phone number).
         *
         * If you are attempting to matching contacts with phone numbers using [Query], then you
         * will most likely find it to difficult and tricky because the normalizedNumber could be
         * null and matching formatted numbers (e.g. (718) 737-1991) would require some special
         * regular expressions. This match might just be what you need =)
         *
         * Only the [Contact.displayNamePrimary] and the phone number/normalizedNumber are included
         * in the matching process.
         *
         * For example, a contact with [Contact.displayNamePrimary] of "Bob Dole" and phone number
         * "(718) 737-1991" (regardless of the value of normalizedNumber) will be matched with the
         * following texts;
         *
         * - 718
         * - 7187371991
         * - 7.1-8.7-3.7-19(91)
         * - bob
         * - dole
         *
         * Notice that "bob" and "dole" will trigger a match because the display name matches and
         * the contact has a phone number.
         *
         * The following texts will NOT trigger a match because the comparison begins at the
         * beginning of the string and not in the middle or end;
         *
         * - 737
         * - 1991
         */
        PHONE,

        /**
         * Only emails or (contact display name + any emails) are included in the matching process.
         *
         * Only the [Contact.displayNamePrimary] and the email address are included in the matching
         * process.
         *
         * For example, the search text "bob" will match the following contacts;
         *
         * - Robert Parr (bob@incredibles.com)
         * - Bob Parr (incredible@android.com)
         *
         * Notice that the contact Bob Parr is also matched because the display name matches and an
         * email exist (even though it does not match).
         *
         * The following search texts will NOT trigger a match because the comparison begins at the
         * beginning of the string and not in the middle or end;
         *
         * - android
         * - gmail
         * - @
         * - .com
         */
        EMAIL
    }

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
    // I know that this interface also exist in Query but I want each API to have its own
    // interface for the results in case we need to deviate implementation. Besides, this is the
    // only pair of APIs in the library that have the same name for its results interface.
    interface Result : List<Contact>, CrudApi.QueryResultWithLimit {

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun BroadQuery(contacts: Contacts): BroadQuery = BroadQueryImpl(contacts)

private class BroadQueryImpl(
    override val contactsApi: Contacts,

    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var groupMembershipWhere: Where<GroupMembershipField>? = DEFAULT_GROUP_MEMBERSHIP_WHERE,
    private var include: Include<AbstractDataField>? = null,
    private var includeRawContactsFields: Include<RawContactsField>? = null,
    private var match: Match = DEFAULT_MATCH,
    private var searchString: String? = DEFAULT_SEARCH_STRING,
    private var orderBy: CompoundOrderBy<ContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,
    private var forceOffsetAndLimit: Boolean = DEFAULT_FORCE_OFFSET_AND_LIMIT,

    override val isRedacted: Boolean = false
) : BroadQuery {

    override fun toString(): String =
        """
            BroadQuery {
                rawContactsWhere: $rawContactsWhere
                groupMembershipWhere: $groupMembershipWhere
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                match: $match
                searchString: $searchString
                orderBy: $orderBy
                limit: $limit
                offset: $offset
                forceOffsetAndLimit: $forceOffsetAndLimit
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BroadQuery = BroadQueryImpl(
        contactsApi,

        // Redact Account information.
        rawContactsWhere?.redactedCopy(),
        groupMembershipWhere,
        include,
        includeRawContactsFields,
        match,
        // Redact search input.
        searchString?.redact(),
        orderBy,
        limit,
        offset,
        forceOffsetAndLimit,

        isRedacted = true
    )

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): BroadQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()?.redactedCopyOrThis(isRedacted)
    }

    override fun groups(vararg groups: Group) = groups(groups.asSequence())

    override fun groups(groups: Collection<Group>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<Group>): BroadQuery = apply {
        val groupIds = groups.map { it.id }
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
            null // Set to null to disable include field checks, for optimization purposes.
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

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): BroadQuery = apply {
        includeRawContactsFields = if (fields.isEmpty()) {
            null // Set to null to disable include field checks, for optimization purposes.
        } else {
            Include(fields + REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS)
        }
    }

    override fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ) = includeRawContactsFields(fields(RawContactsFields))

    override fun match(match: Match): BroadQuery = apply {
        this.match = match
    }

    override fun wherePartiallyMatches(searchString: String?): BroadQuery = apply {
        // Yes, I know DEFAULT_SEARCH_STRING is null. This reads better though.
        this.searchString = (searchString ?: DEFAULT_SEARCH_STRING)?.redactStringOrThis(isRedacted)
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

    override fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy(ContactsFields))

    override fun limit(limit: Int): BroadQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): BroadQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): BroadQuery = apply {
        this.forceOffsetAndLimit = forceOffsetAndLimit
    }

    override fun find(): BroadQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): BroadQuery.Result {
        onPreExecute()

        var contacts = if (!permissions.canQuery()) {
            emptyList()
        } else {
            contactsApi.resolve(
                customDataRegistry,
                rawContactsWhere, groupMembershipWhere,
                include, includeRawContactsFields,
                match, searchString,
                orderBy, limit, offset,
                cancel
            )
        }

        val isLimitBreached = contacts.size > limit
        if (isLimitBreached && forceOffsetAndLimit) {
            contacts = contacts.offsetAndLimit(offset, limit)
        }

        return BroadQueryResult(contacts, isLimitBreached)
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val DEFAULT_GROUP_MEMBERSHIP_WHERE: Where<GroupMembershipField>? = null
        val REQUIRED_INCLUDE_FIELDS by lazy { Fields.Required.all.asSequence() }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by lazy {
            RawContactsFields.Required.all.asSequence()
        }
        val DEFAULT_MATCH: Match = Match.ANY
        val DEFAULT_SEARCH_STRING: String? = null
        val DEFAULT_ORDER_BY by lazy { CompoundOrderBy(setOf(ContactsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
        const val DEFAULT_FORCE_OFFSET_AND_LIMIT = true
    }
}

private fun Contacts.resolve(
    customDataRegistry: CustomDataRegistry,
    rawContactsWhere: Where<RawContactsField>?,
    groupMembershipWhere: Where<GroupMembershipField>?,
    include: Include<AbstractDataField>?,
    includeRawContactsFields: Include<RawContactsField>?,
    match: Match,
    searchString: String?,
    orderBy: CompoundOrderBy<ContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<Contact> {

    var contactIds: MutableSet<Long>? = null

    // Get Contact Ids partially matching the searchString from the Contacts table. If searchString
    // is null, skip.
    if (!searchString.isNullOrEmpty() && !cancel()) {
        contactIds = mutableSetOf<Long>().apply {
            addAll(findMatchingContactIds(match, searchString, cancel))
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

private fun Contacts.findMatchingContactIds(
    match: Match, searchString: String, cancel: () -> Boolean
): Set<Long> = when (match) {
    Match.ANY -> findContactIdsInContactsTable(searchString, cancel)
    Match.PHONE -> findContactIdsInDataTable(
        ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
        searchString,
        cancel
    )
    Match.EMAIL -> findContactIdsInDataTable(
        ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
        searchString,
        cancel
    )
}

private fun Contacts.findContactIdsInContactsTable(
    searchString: String, cancel: () -> Boolean
): Set<Long> = contentResolver.query(
    Uri.withAppendedPath(
        // The documentation states that this matches "various parts of the contact name".
        // However, it actually matches more than just the name. Even data such as note
        // that is not in ContactsContract.DisplayNameSources!
        // Also, note that CALLER_IS_SYNCADAPTER probably does not really matter for queries but
        // might as well be consistent...
        ContactsContract.Contacts.CONTENT_FILTER_URI.forSyncAdapter(callerIsSyncAdapter),
        Uri.encode(searchString)
    ),
    Include(ContactsFields.Id),
    null
) {
    val contactIds = mutableSetOf<Long>()
    val contactsCursor = it.contactsCursor()
    while (!cancel() && it.moveToNext()) {
        contactIds.add(contactsCursor.contactId)
    }
    contactIds
} ?: emptySet()

private fun Contacts.findContactIdsInDataTable(
    contentFilterUri: Uri,
    searchString: String, cancel: () -> Boolean
): Set<Long> = contentResolver.query(
    Uri.withAppendedPath(
        // Note that CALLER_IS_SYNCADAPTER probably does not really matter for queries but might as well
        // be consistent...
        contentFilterUri.forSyncAdapter(callerIsSyncAdapter),
        Uri.encode(searchString)
    ),
    Include(Fields.Contact.Id),
    null
) {
    val contactIds = mutableSetOf<Long>()
    val dataCursor = it.dataCursor()
    while (!cancel() && it.moveToNext()) {
        contactIds.add(dataCursor.contactId)
    }
    contactIds
} ?: emptySet()

private class BroadQueryResult private constructor(
    contacts: List<Contact>,
    override val isLimitBreached: Boolean,
    override val isRedacted: Boolean
) : ArrayList<Contact>(contacts), BroadQuery.Result {

    constructor(contacts: List<Contact>, isLimitBreached: Boolean) : this(
        contacts = contacts,
        isLimitBreached = isLimitBreached,
        isRedacted = false
    )

    override fun toString(): String =
        """
            BroadQuery.Result {
                Number of contacts found: $size
                First contact: ${firstOrNull()}
                isLimitBreached: $isLimitBreached
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BroadQuery.Result = BroadQueryResult(
        contacts = redactedCopies(),
        isLimitBreached = isLimitBreached,
        isRedacted = true
    )
}