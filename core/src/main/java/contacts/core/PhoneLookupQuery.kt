package contacts.core

import android.accounts.Account
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import contacts.core.PhoneLookupQuery.Match
import contacts.core.entities.Contact
import contacts.core.entities.Group
import contacts.core.entities.cursor.phoneLookupCursor
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.util.*

/**
 * Performs a highly optimized query using a phone number or SIP address.
 *
 * This will only match EXACT phone numbers or SIP addresses of different formatting and
 * variations. There is no partial matching. This is useful for caller IDs in incoming and
 * outgoing calls.
 *
 * If you need to perform partial matching based on other data than just phone numbers and SIP
 * addresses, use [Query] or [PhoneLookupQuery].
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
 * Here is an example query that searches for the phone number "123";
 *
 * In Kotlin,
 *
 * ```kotlin
 * val contacts = phoneLookupQuery.whereExactlyMatches("123").find()
 * ```
 *
 * In Java,
 *
 * ```java
 * List<Contact> contacts = phoneLookupQuery.whereExactlyMatches("123").find();
 * ```
 */
interface PhoneLookupQuery : CrudApi {

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
    fun accounts(vararg accounts: Account?): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.accounts]
     */
    fun accounts(accounts: Collection<Account?>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): PhoneLookupQuery

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
    fun groups(vararg groups: Group): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.groups]
     */
    fun groups(groups: Collection<Group>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.groups]
     */
    fun groups(groups: Sequence<Group>): PhoneLookupQuery

    /**
     * Includes only the given set of [fields] in each of the matching contacts.
     *
     * If no fields are specified, then all fields ([Fields.all]) are included. Otherwise, only the
     * specified fields will be included in addition to required API fields [Fields.Required]
     * (e.g. IDs), which are always included.
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
     * To include RawContact specific properties, use [PhoneLookupQuery.includeRawContactsFields].
     *
     * ## Performance
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
     *
     * The most optimal queries only include fields from [Fields.Contact] because no Data table rows
     * need to be processed.
     */
    fun include(vararg fields: AbstractDataField): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.include].
     */
    fun include(fields: Collection<AbstractDataField>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.include].
     */
    fun include(fields: Sequence<AbstractDataField>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): PhoneLookupQuery

    /**
     * Includes [fields] from the RawContacts table corresponding to the following RawContacts
     * properties;
     *
     * - [contacts.core.entities.RawContact.displayNamePrimary]
     * - [contacts.core.entities.RawContact.displayNameAlt]
     * - [contacts.core.entities.RawContact.account]
     * - [contacts.core.entities.RawContact.options]
     *
     * For all other fields/properties, use [include].
     *
     * If no fields are specified, then all RawContacts fields ([RawContactsFields.all]) are
     * included. Otherwise, only the specified fields will be included in addition to required API
     * fields [RawContactsFields.Required].
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
     * ## Developer notes
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
    fun includeRawContactsFields(vararg fields: RawContactsField): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: RawContactsFields.() -> Collection<RawContactsField>): PhoneLookupQuery

    /**
     * Specifies the type of lookup data that should be used in the matching process. This
     * will affect the search results when [whereExactlyMatches] is used.
     *
     * The default is [Match.PHONE].
     */
    fun match(match: Match): PhoneLookupQuery

    /**
     * Filters the [Contact]s exactly matching the [searchString]. If not specified or null or
     * empty, then no [Contact]s are returned.
     *
     * Specify the type of contact data that should be used in the matching process using the
     * [match] function.
     *
     * **Custom data are not included in the matching process!** To match custom data, use [Query].
     */
    fun whereExactlyMatches(searchString: String?): PhoneLookupQuery

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
    fun orderBy(vararg orderBy: OrderBy<ContactsField>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<ContactsField>>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): PhoneLookupQuery

    /**
     * See [PhoneLookupQuery.orderBy].
     */
    fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>): PhoneLookupQuery

    /**
     * Limits the maximum number of returned [Contact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     *
     * Some devices do not support this. See [forceOffsetAndLimit].
     */
    fun limit(limit: Int): PhoneLookupQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     *
     * Some devices do not support this. See [forceOffsetAndLimit].
     */
    fun offset(offset: Int): PhoneLookupQuery

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
    fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): PhoneLookupQuery

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
    override fun redactedCopy(): PhoneLookupQuery

    /**
     * Specifies the type of lookup data that should be used in the matching process.
     */
    enum class Match {

        /**
         * Match phone numbers. This is the default.
         *
         * This will only match EXACT phone numbers or SIP addresses of different formatting and
         * variations. There is no partial matching. This is useful in cases where you want to
         * implement a caller ID function for incoming and outgoing calls. For example, if there
         * are contacts with the following numbers;
         *
         * - 123
         * - 1234
         * - 1234
         * - 12345
         *
         * Searching for "123" will only return the one contact with the number "123". Searching for
         * "1234" will return the contact(s) with the number "1234".
         *
         * Additionally, this is able to match phone numbers with or without using country codes.
         * For example, the phone number "+923123456789" (country code 92) will be matched using
         * any of the following; "03123456789", "923123456789", "+923123456789".
         *
         * The reverse is true. For example, the phone number "03123456789" will be matched using
         * any of the following; "03123456789", "923123456789", "+923123456789".
         *
         * However, if a phone number is saved with AND without a country code, then only the
         * contact with the number that matches exactly will be returned. For example, when numbers
         * "+923123456789" and "03123456789" are saved, searching for "03123456789" will return
         * only the contact with that exact number (NOT including the contact with "+923123456789").
         */
        PHONE,

        /**
         * Same as [PHONE] except this matches SIP addresses instead of phone numbers.
         *
         * ## API version 21+ only
         *
         * This is only available for API 21 and above. The [PHONE] will be used for API
         * versions below 21 even if [SIP] is specified.
         */
        // [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
        SIP
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
internal fun PhoneLookupQuery(contacts: Contacts): PhoneLookupQuery = PhoneLookupQueryImpl(contacts)

private class PhoneLookupQueryImpl(
    override val contactsApi: Contacts,

    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var groupMembershipWhere: Where<GroupMembershipField>? = DEFAULT_GROUP_MEMBERSHIP_WHERE,
    private var include: Include<AbstractDataField> = contactsApi.includeAllFields(),
    private var includeRawContactsFields: Include<RawContactsField> = DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS,
    private var match: Match = DEFAULT_MATCH,
    private var searchString: String? = DEFAULT_SEARCH_STRING,
    private var orderBy: CompoundOrderBy<ContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,
    private var forceOffsetAndLimit: Boolean = DEFAULT_FORCE_OFFSET_AND_LIMIT,

    override val isRedacted: Boolean = false
) : PhoneLookupQuery {

    override fun toString(): String =
        """
            PhoneLookupQuery {
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

    override fun redactedCopy(): PhoneLookupQuery = PhoneLookupQueryImpl(
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

    override fun accounts(accounts: Sequence<Account?>): PhoneLookupQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()?.redactedCopyOrThis(isRedacted)
    }

    override fun groups(vararg groups: Group) = groups(groups.asSequence())

    override fun groups(groups: Collection<Group>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<Group>): PhoneLookupQuery = apply {
        val groupIds = groups.map { it.id }
        groupMembershipWhere = if (groupIds.isEmpty()) {
            DEFAULT_GROUP_MEMBERSHIP_WHERE
        } else {
            Fields.GroupMembership.GroupId `in` groupIds
        }
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): PhoneLookupQuery = apply {
        include = if (fields.isEmpty()) {
            contactsApi.includeAllFields()
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

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): PhoneLookupQuery =
        apply {
            includeRawContactsFields = if (fields.isEmpty()) {
                DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS
            } else {
                Include(fields + REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS)
            }
        }

    override fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ) = includeRawContactsFields(fields(RawContactsFields))

    override fun match(match: Match): PhoneLookupQuery = apply {
        this.match = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Match.PHONE
        } else {
            match
        }
    }

    override fun whereExactlyMatches(searchString: String?): PhoneLookupQuery = apply {
        // Yes, I know DEFAULT_SEARCH_STRING is null. This reads better though.
        this.searchString = (searchString ?: DEFAULT_SEARCH_STRING)?.redactStringOrThis(isRedacted)
    }

    override fun orderBy(vararg orderBy: OrderBy<ContactsField>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): PhoneLookupQuery = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy(ContactsFields))

    override fun limit(limit: Int): PhoneLookupQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): PhoneLookupQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): PhoneLookupQuery = apply {
        this.forceOffsetAndLimit = forceOffsetAndLimit
    }

    override fun find(): PhoneLookupQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): PhoneLookupQuery.Result {
        onPreExecute()

        var contacts = if (!permissions.canQuery()) {
            emptyList()
        } else {
            contentResolver.resolve(
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

        return PhoneLookupQueryResult(contacts, isLimitBreached)
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val DEFAULT_GROUP_MEMBERSHIP_WHERE: Where<GroupMembershipField>? = null
        val DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS by unsafeLazy { Include(RawContactsFields.all) }
        val REQUIRED_INCLUDE_FIELDS by unsafeLazy { Fields.Required.all.asSequence() }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by unsafeLazy {
            RawContactsFields.Required.all.asSequence()
        }
        val DEFAULT_MATCH: Match = Match.PHONE
        val DEFAULT_SEARCH_STRING: String? = null
        val DEFAULT_ORDER_BY by unsafeLazy { CompoundOrderBy(setOf(ContactsFields.Id.asc())) }
        const val DEFAULT_LIMIT = Int.MAX_VALUE
        const val DEFAULT_OFFSET = 0
        const val DEFAULT_FORCE_OFFSET_AND_LIMIT = true
    }
}

private fun ContentResolver.resolve(
    customDataRegistry: CustomDataRegistry,
    rawContactsWhere: Where<RawContactsField>?,
    groupMembershipWhere: Where<GroupMembershipField>?,
    include: Include<AbstractDataField>,
    includeRawContactsFields: Include<RawContactsField>,
    match: Match,
    searchString: String?,
    orderBy: CompoundOrderBy<ContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<Contact> {

    if (searchString.isNullOrEmpty()) {
        return emptyList()
    }

    var contactIds: MutableSet<Long>? = null

    // Get Contact Ids exactly matching the searchString from the PhoneLookup table.
    if (!cancel()) {
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

private fun ContentResolver.findMatchingContactIds(
    match: Match, searchString: String, cancel: () -> Boolean
): Set<Long> = query(
    ContactsContract.PhoneLookup.CONTENT_FILTER_URI
        .buildUpon()
        .appendEncodedPath(Uri.encode(searchString))
        .let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.appendQueryParameter(
                    ContactsContract.PhoneLookup.QUERY_PARAMETER_SIP_ADDRESS,
                    when (match) {
                        Match.PHONE -> "0"
                        Match.SIP -> "1"
                    }
                )
            } else {
                it
            }
        }
        .build(),
    Include(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PhoneLookupFields.ContactId
        } else {
            PhoneLookupFields.Id
        }
    ),
    null
) {
    val contactIds = mutableSetOf<Long>()
    val phoneLookupCursor = it.phoneLookupCursor()
    while (!cancel() && it.moveToNext()) {
        contactIds.add(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                phoneLookupCursor.contactId
            } else {
                phoneLookupCursor.id
            }
        )
    }
    contactIds
} ?: emptySet()

private class PhoneLookupQueryResult private constructor(
    contacts: List<Contact>,
    override val isLimitBreached: Boolean,
    override val isRedacted: Boolean
) : ArrayList<Contact>(contacts), PhoneLookupQuery.Result {

    constructor(contacts: List<Contact>, isLimitBreached: Boolean) : this(
        contacts = contacts,
        isLimitBreached = isLimitBreached,
        isRedacted = false
    )

    override fun toString(): String =
        """
            PhoneLookupQuery.Result {
                Number of contacts found: $size
                First contact: ${firstOrNull()}
                isLimitBreached: $isLimitBreached
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): PhoneLookupQuery.Result = PhoneLookupQueryResult(
        contacts = redactedCopies(),
        isLimitBreached = isLimitBreached,
        isRedacted = true
    )
}