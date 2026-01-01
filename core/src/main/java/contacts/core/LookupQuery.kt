package contacts.core

import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract
import contacts.core.entities.Contact
import contacts.core.entities.Group
import contacts.core.entities.cursor.contactsCursor
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.util.findContactIdsInDataTable
import contacts.core.util.findContactIdsInRawContactsTable
import contacts.core.util.forSyncAdapter
import contacts.core.util.isEmpty
import contacts.core.util.offsetAndLimit
import contacts.core.util.query
import contacts.core.util.toRawContactsWhere

/**
 * Uses [android.provider.ContactsContract.Contacts.CONTENT_LOOKUP_URI] to get contacts using
 * lookup keys, which are typically used in shortcuts or other long-term links to contacts.
 *
 * The reason why lookup keys are used as long-term links to contacts is because Contact IDs and the
 * lookup keys themselves may change over time due to linking/unlinking, local contact updates, and
 * syncing adapter operations. Lookup keys provide the ability to retrieve contacts even when its ID
 * or lookup key itself changes.
 *
 * You may use [Query] to get contacts using lookup keys and ids as well but [LookupQuery] is
 * simpler and more robust and optimized for the specific purpose of getting contacts using
 * lookup keys.
 *
 * For a broader, and more AOSP Contacts app like query, use [BroadQuery].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. If not granted, the query will do nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example query that returns the contact with the provided `lookupKey`;
 *
 * In Kotlin,
 *
 * ```kotlin
 * val contacts = lookupQuery.whereLookupKeyMatches(lookupKey).find()
 * ```
 *
 * In Java,
 *
 * ```java
 * List<Contact> contacts = lookupQuery.whereLookupKeyMatches(lookupKey).find();
 * ```
 *
 * ## Developer notes
 *
 * This query will typically only be used to get a single contact. Therefore, it is really not
 * necessary (nor does it make practical sense) to provide filtering and paginating functions;
 * [accounts], [groups], [orderBy], [limit], [offset], and [forceOffsetAndLimit].
 *
 * However, there may be a user of this API that would want to load contacts from hundreds or
 * thousands of lookup keys. While this practice may violate YAGNI, adding these functions now
 * future-proofs this API and also makes it much similar to the other query APIs;
 * [Query], [BroadQuery], and [PhoneLookupQuery].
 */
interface LookupQuery : CrudApi {

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
    fun accounts(vararg accounts: Account?): LookupQuery

    /**
     * See [LookupQuery.accounts]
     */
    fun accounts(accounts: Collection<Account?>): LookupQuery

    /**
     * See [LookupQuery.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): LookupQuery

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
    fun groups(vararg groups: Group): LookupQuery

    /**
     * See [LookupQuery.groups]
     */
    fun groups(groups: Collection<Group>): LookupQuery

    /**
     * See [LookupQuery.groups]
     */
    fun groups(groups: Sequence<Group>): LookupQuery

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
     * To include RawContact specific properties, use [LookupQuery.includeRawContactsFields].
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
    fun include(vararg fields: AbstractDataField): LookupQuery

    /**
     * See [LookupQuery.include].
     */
    fun include(fields: Collection<AbstractDataField>): LookupQuery

    /**
     * See [LookupQuery.include].
     */
    fun include(fields: Sequence<AbstractDataField>): LookupQuery

    /**
     * See [LookupQuery.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): LookupQuery

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
     *
     * ## Including all fields
     *
     * If you want to include all fields, including custom data fields, then passing in an empty
     * list or not invoking this function is the most performant way to do it because internal
     * checks will be disabled (less lines of code executed).
     *
     * #### Developer notes
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
    fun includeRawContactsFields(vararg fields: RawContactsField): LookupQuery

    /**
     * See [LookupQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): LookupQuery

    /**
     * See [LookupQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): LookupQuery

    /**
     * See [LookupQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: RawContactsFields.() -> Collection<RawContactsField>): LookupQuery

    /**
     * Sets the query to search for contacts that match one of the [lookupKeys].
     *
     * Empty or blank strings are ignored.
     *
     * If you additionally have the contact ID, use [whereLookupKeyWithIdMatches].
     */
    fun whereLookupKeyMatches(vararg lookupKeys: String): LookupQuery

    /**
     * See [LookupQuery.whereLookupKeyMatches].
     */
    fun whereLookupKeyMatches(lookupKeys: Collection<String>): LookupQuery

    /**
     * See [LookupQuery.whereLookupKeyMatches].
     */
    fun whereLookupKeyMatches(lookupKeys: Sequence<String>): LookupQuery

    /**
     * Sets the query to search for contacts that match one of the [lookupKeysWithIds].
     *
     * The [LookupKeyWithId.lookupKey] is optionally paired with the last known
     * [LookupKeyWithId.contactId]. This "complete" format is an important optimization and is
     * highly recommended.
     *
     * Instances with [LookupKeyWithId.lookupKey] being an empty or blank string are ignored. If the
     * [LookupKeyWithId.contactId] is 0 or less, then only the [LookupKeyWithId.lookupKey] is used.
     *
     * If you only have the lookup key, use [whereLookupKeyMatches].
     */
    fun whereLookupKeyWithIdMatches(vararg lookupKeysWithIds: LookupKeyWithId): LookupQuery

    /**
     * See [LookupQuery.whereLookupKeyWithIdMatches].
     */
    fun whereLookupKeyWithIdMatches(lookupKeysWithIds: Collection<LookupKeyWithId>): LookupQuery

    /**
     * See [LookupQuery.whereLookupKeyWithIdMatches].
     */
    fun whereLookupKeyWithIdMatches(lookupKeysWithIds: Sequence<LookupKeyWithId>): LookupQuery

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
    fun orderBy(vararg orderBy: OrderBy<ContactsField>): LookupQuery

    /**
     * See [LookupQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy<ContactsField>>): LookupQuery

    /**
     * See [LookupQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): LookupQuery

    /**
     * See [LookupQuery.orderBy].
     */
    fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>): LookupQuery

    /**
     * Limits the maximum number of returned [Contact]s to the given [limit].
     *
     * If not specified, limit value of [Int.MAX_VALUE] is used.
     *
     * Some devices do not support this. See [forceOffsetAndLimit].
     */
    fun limit(limit: Int): LookupQuery

    /**
     * Skips results 0 to [offset] (excluding the offset).
     *
     * If not specified, offset value of 0 is used.
     *
     * Some devices do not support this. See [forceOffsetAndLimit].
     */
    fun offset(offset: Int): LookupQuery

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
    fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): LookupQuery

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
    override fun redactedCopy(): LookupQuery

    /**
     * The [lookupKey] is optionally paired with the last known [contactId]. This "complete"
     * format is an important optimization and is highly recommended.
     *
     * Instances with [lookupKey] being an empty or blank string are ignored. If the [contactId] is 0 or
     * less, then only the [lookupKey] is used.
     */
    data class LookupKeyWithId(
        val lookupKey: String,
        val contactId: Long
    )

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

internal fun LookupQuery(contacts: Contacts): LookupQuery = LookupQueryImpl(contacts)

private class LookupQueryImpl(
    override val contactsApi: Contacts,

    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var groupMembershipWhere: Where<GroupMembershipField>? = DEFAULT_GROUP_MEMBERSHIP_WHERE,
    private var include: Include<AbstractDataField>? = null,
    private var includeRawContactsFields: Include<RawContactsField>? = null,
    private var lookupKeys: MutableSet<LookupQuery.LookupKeyWithId> = mutableSetOf(),
    private var orderBy: CompoundOrderBy<ContactsField> = DEFAULT_ORDER_BY,
    private var limit: Int = DEFAULT_LIMIT,
    private var offset: Int = DEFAULT_OFFSET,
    private var forceOffsetAndLimit: Boolean = DEFAULT_FORCE_OFFSET_AND_LIMIT,

    override val isRedacted: Boolean = false
) : LookupQuery {

    override fun toString(): String =
        """
            LookupQuery {
                rawContactsWhere: $rawContactsWhere
                groupMembershipWhere: $groupMembershipWhere
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                lookupKeys: $lookupKeys
                orderBy: $orderBy
                limit: $limit
                offset: $offset
                forceOffsetAndLimit: $forceOffsetAndLimit
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): LookupQuery = LookupQueryImpl(
        contactsApi,

        // Redact Account information.
        rawContactsWhere?.redactedCopy(),
        groupMembershipWhere,
        include,
        includeRawContactsFields,
        lookupKeys,
        orderBy,
        limit,
        offset,
        forceOffsetAndLimit,

        isRedacted = true
    )

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): LookupQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()?.redactedCopyOrThis(isRedacted)
    }

    override fun groups(vararg groups: Group) = groups(groups.asSequence())

    override fun groups(groups: Collection<Group>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<Group>): LookupQuery = apply {
        val groupIds = groups.map { it.id }
        groupMembershipWhere = if (groupIds.isEmpty()) {
            DEFAULT_GROUP_MEMBERSHIP_WHERE
        } else {
            Fields.GroupMembership.GroupId `in` groupIds
        }
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): LookupQuery = apply {
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

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): LookupQuery =
        apply {
            includeRawContactsFields = if (fields.isEmpty()) {
                null // Set to null to disable include field checks, for optimization purposes.
            } else {
                Include(fields + REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS)
            }
        }

    override fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ) = includeRawContactsFields(fields(RawContactsFields))

    override fun whereLookupKeyMatches(vararg lookupKeys: String) =
        whereLookupKeyMatches(lookupKeys.asSequence())

    override fun whereLookupKeyMatches(lookupKeys: Collection<String>) =
        whereLookupKeyMatches(lookupKeys.asSequence())

    override fun whereLookupKeyMatches(lookupKeys: Sequence<String>) =
        whereLookupKeyWithIdMatches(lookupKeys.map { LookupQuery.LookupKeyWithId(it, 0) })

    override fun whereLookupKeyWithIdMatches(vararg lookupKeysWithIds: LookupQuery.LookupKeyWithId) =
        whereLookupKeyWithIdMatches(lookupKeysWithIds.asSequence())

    override fun whereLookupKeyWithIdMatches(lookupKeysWithIds: Collection<LookupQuery.LookupKeyWithId>) =
        whereLookupKeyWithIdMatches(lookupKeysWithIds.asSequence())

    override fun whereLookupKeyWithIdMatches(lookupKeysWithIds: Sequence<LookupQuery.LookupKeyWithId>): LookupQuery =
        apply { lookupKeys.addAll(lookupKeysWithIds.filter { it.lookupKey.isNotBlank() }) }

    override fun orderBy(vararg orderBy: OrderBy<ContactsField>) = orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy.asSequence())

    override fun orderBy(orderBy: Sequence<OrderBy<ContactsField>>): LookupQuery = apply {
        this.orderBy = if (orderBy.isEmpty()) {
            DEFAULT_ORDER_BY
        } else {
            CompoundOrderBy(orderBy.toSet())
        }
    }

    override fun orderBy(orderBy: ContactsFields.() -> Collection<OrderBy<ContactsField>>) =
        orderBy(orderBy(ContactsFields))

    override fun limit(limit: Int): LookupQuery = apply {
        this.limit = if (limit > 0) {
            limit
        } else {
            throw ContactsException("Limit must be greater than 0")
        }
    }

    override fun offset(offset: Int): LookupQuery = apply {
        this.offset = if (offset >= 0) {
            offset
        } else {
            throw ContactsException("Offset must be greater than or equal to 0")
        }
    }

    override fun forceOffsetAndLimit(forceOffsetAndLimit: Boolean): LookupQuery = apply {
        this.forceOffsetAndLimit = forceOffsetAndLimit
    }

    override fun find(): LookupQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): LookupQuery.Result {
        onPreExecute()

        var contacts = if (!permissions.canQuery()) {
            emptyList()
        } else {
            contactsApi.resolve(
                customDataRegistry,
                rawContactsWhere, groupMembershipWhere,
                include, includeRawContactsFields,
                lookupKeys,
                orderBy, limit, offset,
                cancel
            )
        }

        val isLimitBreached = contacts.size > limit
        if (isLimitBreached && forceOffsetAndLimit) {
            contacts = contacts.offsetAndLimit(offset, limit)
        }

        return LookupQueryResult(contacts, isLimitBreached)
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
    lookupKeys: Set<LookupQuery.LookupKeyWithId>,
    orderBy: CompoundOrderBy<ContactsField>,
    limit: Int,
    offset: Int,
    cancel: () -> Boolean
): List<Contact> {

    if (lookupKeys.isEmpty()) {
        return emptyList()
    }

    var contactIds: MutableSet<Long>? = null

    // Get Contact Ids using the lookup keys.
    if (!cancel()) {
        contactIds = mutableSetOf<Long>().apply {
            addAll(findMatchingContactIds(lookupKeys, cancel))
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
    lookupKeys: Set<LookupQuery.LookupKeyWithId>, cancel: () -> Boolean
): Set<Long> {
    val contactIds = mutableSetOf<Long>()

    for (lookupKey in lookupKeys) {
        if (cancel()) {
            break
        }

        var lookupUri =
            Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey.lookupKey)
        if (lookupKey.contactId > 0) {
            lookupUri = ContentUris.withAppendedId(lookupUri, lookupKey.contactId)
        }
        // Note that CALLER_IS_SYNCADAPTER probably does not really matter for queries but might as
        // well be consistent...
        lookupUri = lookupUri.forSyncAdapter(callerIsSyncAdapter)

        contentResolver.query(lookupUri, Include(ContactsFields.Id), null) {
            val ids = mutableSetOf<Long>()
            val contactsCursor = it.contactsCursor()
            while (!cancel() && it.moveToNext()) {
                ids.add(contactsCursor.contactId)
            }
            ids
        }
            ?.also(contactIds::addAll)
    }

    return contactIds
}

private class LookupQueryResult private constructor(
    contacts: List<Contact>,
    override val isLimitBreached: Boolean,
    override val isRedacted: Boolean
) : ArrayList<Contact>(contacts), LookupQuery.Result {

    constructor(contacts: List<Contact>, isLimitBreached: Boolean) : this(
        contacts = contacts,
        isLimitBreached = isLimitBreached,
        isRedacted = false
    )

    override fun toString(): String =
        """
            LookupQuery.Result {
                Number of contacts found: $size
                First contact: ${firstOrNull()}
                isLimitBreached: $isLimitBreached
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): LookupQuery.Result = LookupQueryResult(
        contacts = redactedCopies(),
        isLimitBreached = isLimitBreached,
        isRedacted = true
    )
}