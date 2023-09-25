package contacts.core.profile

import android.accounts.Account
import contacts.core.*
import contacts.core.entities.Contact
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.mapper.ContactsMapper
import contacts.core.entities.table.ProfileUris
import contacts.core.util.*

/**
 * Queries the Contacts, RawContacts, and Data tables and returns the one and only profile
 * [Contact], if available.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. If not granted, the query will do nothing and return null.
 *
 * For API 22 and below, the permission "android.permission.READ_PROFILE" is also required but only
 * at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted prior
 * to installation instead of at runtime.
 *
 * ## Usage
 *
 * Here is an example query that returns the profile [Contact]. Include only RawContacts belonging
 * to the given account. Include only the name and email properties of the profile [Contact].
 *
 * In Kotlin,
 *
 * ```kotlin
 * val profileContact : Contact? = profileQuery.
 *      .accounts(account)
 *      .include { Name.all + Address.all }
 *      .find()
 * ```
 *
 * In Java,
 *
 * ```java
 * import static contacts.core.Fields.*;
 *
 * List<Contact> profileContact = profileQuery
 *      .accounts(account)
 *      .include(new ArrayList<>() {{
 *           addAll(Name.getAll());
 *           addAll(Address.getAll());
 *       }})
 *      .find();
 * ```
 */
interface ProfileQuery : CrudApi {

    /**
     * Limits the RawContacts and associated data to those associated with one of the given
     * accounts. The Contact returned will not contain data that belongs to other accounts not
     * specified in [accounts].
     *
     * If no accounts are specified (this function is not called or called with no Accounts), then
     * all RawContacts and associated data are included in the search.
     *
     * A null [Account] may be provided here, which results in RawContacts with no associated
     * Account to be included in the search. RawContacts without an associated account are
     * considered local or device-only contacts, which are not synced.
     */
    fun accounts(vararg accounts: Account?): ProfileQuery

    /**
     * See [ProfileQuery.accounts]
     */
    fun accounts(accounts: Collection<Account?>): ProfileQuery

    /**
     * See [ProfileQuery.accounts]
     */
    fun accounts(accounts: Sequence<Account?>): ProfileQuery

    /**
     * Includes only the given set of [fields] (data) in the profile Contact.
     *
     * If no fields are specified (empty list), then all fields are included. Otherwise, only
     * the specified fields will be included.
     *
     * When all fields are included in a query operation, all properties of the profile Contacts
     * and Data are populated with values from the database. Properties of fields that are included
     * are not guaranteed to be non-null because the database may actually have no data for the
     * corresponding field.
     *
     * When only some fields are included, only those included properties of the profile Contacts
     * and Data are populated with values from the database. Properties of fields that are not
     * included are guaranteed to be null.
     *
     * ## Performance
     *
     * It is recommended to only include fields that will be used to save CPU and memory.
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
    fun include(vararg fields: AbstractDataField): ProfileQuery

    /**
     * See [ProfileQuery.include].
     */
    fun include(fields: Collection<AbstractDataField>): ProfileQuery

    /**
     * See [ProfileQuery.include].
     */
    fun include(fields: Sequence<AbstractDataField>): ProfileQuery

    /**
     * See [ProfileQuery.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): ProfileQuery

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
     * To include RawContact specific properties, use [ProfileQuery.includeRawContactsFields].
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
    fun includeRawContactsFields(vararg fields: RawContactsField): ProfileQuery

    /**
     * See [ProfileQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): ProfileQuery

    /**
     * See [ProfileQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): ProfileQuery

    /**
     * See [ProfileQuery.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: RawContactsFields.() -> Collection<RawContactsField>): ProfileQuery

    /**
     * Returns the profile [Contact] (inside the [Result]), if available.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION].
     *
     * For API 22 and below, the permission "android.permission.READ_PROFILE" is also required but
     * only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
     * prior to installation instead of at runtime.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun find(): Result

    /**
     * Returns the profile [Contact] (inside the [Result]), if available.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION].
     *
     * For API 22 and below, the permission "android.permission.READ_PROFILE" is also required but
     * only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
     * prior to installation instead of at runtime.
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
    override fun redactedCopy(): ProfileQuery

    /**
     * Contains the Profile [contact].
     */
    interface Result : CrudApi.Result {

        /**
         * The Profile [Contact], if exist.
         */
        val contact: Contact?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun ProfileQuery(contacts: Contacts): ProfileQuery = ProfileQueryImpl(contacts)

private class ProfileQueryImpl(
    override val contactsApi: Contacts,

    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include<AbstractDataField>? = null,
    private var includeRawContactsFields: Include<RawContactsField>? = null,

    override val isRedacted: Boolean = false
) : ProfileQuery {

    override fun toString(): String =
        """
            ProfileQuery {
                rawContactsWhere: $rawContactsWhere
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                hasPermission: ${permissions.canQuery()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileQuery = ProfileQueryImpl(
        contactsApi,

        // Redact Account information.
        rawContactsWhere?.redactedCopy(),
        include,
        includeRawContactsFields,

        isRedacted = true
    )

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): ProfileQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()?.redactedCopyOrThis(isRedacted)
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): ProfileQuery = apply {
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

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): ProfileQuery =
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

    override fun find(): ProfileQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): ProfileQuery.Result {
        onPreExecute()

        val profileContact = if (!permissions.canQuery()) {
            null
        } else {
            contactsApi.resolve(
                customDataRegistry, rawContactsWhere, include, includeRawContactsFields, cancel
            )
        }
        return ProfileQueryResult(profileContact)
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val REQUIRED_INCLUDE_FIELDS by lazy { Fields.Required.all.asSequence() }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by lazy {
            RawContactsFields.Required.all.asSequence()
        }
    }
}

private fun Contacts.resolve(
    customDataRegistry: CustomDataRegistry,
    rawContactsWhere: Where<RawContactsField>?,
    include: Include<AbstractDataField>?,
    includeRawContactsFields: Include<RawContactsField>?,
    cancel: () -> Boolean
): Contact? {
    val rawContactIds = rawContactIds(rawContactsWhere, cancel)

    if (cancel() || rawContactIds.isEmpty()) {
        return null
    }

    val finalInclude = include.allFieldsIfNull(this)

    // Collect Contacts, RawContacts, and Data with this mapper.
    val contactsMapper = ContactsMapper(customDataRegistry, cancel)

    // Collect the profile Contact, if exist.
    contentResolver.query(
        ProfileUris.CONTACTS.uri(callerIsSyncAdapter),
        finalInclude.onlyContactsFields(),
        null,
        // Ignore include field checks if include is null.
        setCursorHolderIncludeFieldsToNull = include == null,
        processCursor = contactsMapper::processContactsCursor
    )

    // Collect RawContacts.
    if (!cancel() && rawContactIds.isNotEmpty()) {
        contentResolver.query(
            // Note that CALLER_IS_SYNCADAPTER probably does not really matter for queries but
            // might as well be consistent...
            ProfileUris.RAW_CONTACTS.uri(callerIsSyncAdapter),
            includeRawContactsFields.allFieldsIfNull(),
            RawContactsFields.Id `in` rawContactIds,
            // Ignore include field checks if includeRawContactsFields is null.
            setCursorHolderIncludeFieldsToNull = includeRawContactsFields == null,
            processCursor = contactsMapper::processRawContactsCursor
        )
    }

    // Collect Data
    if (!cancel() && rawContactIds.isNotEmpty()) {
        contentResolver.query(
            // Note that CALLER_IS_SYNCADAPTER probably does not really matter for queries but
            // might as well be consistent...
            ProfileUris.DATA.uri(callerIsSyncAdapter),
            finalInclude,
            Fields.RawContact.Id `in` rawContactIds,
            // Ignore include field checks if include is null.
            setCursorHolderIncludeFieldsToNull = include == null,
            processCursor = contactsMapper::processDataCursor
        )
    }

    return contactsMapper.mapContacts().firstOrNull()
}

private fun Contacts.rawContactIds(
    rawContactsWhere: Where<RawContactsField>?, cancel: () -> Boolean
): Set<Long> = contentResolver.query(
    // Note that CALLER_IS_SYNCADAPTER probably does not really matter for queries but
    // might as well be consistent...
    ProfileUris.RAW_CONTACTS.uri(callerIsSyncAdapter),
    Include(RawContactsFields.Id),
    // There may be RawContacts that are marked for deletion that have not yet been deleted.
    (RawContactsFields.Deleted notEqualTo true) and rawContactsWhere
) {
    buildSet {
        val rawContactsCursor = it.rawContactsCursor()
        while (!cancel() && it.moveToNext()) {
            add(rawContactsCursor.rawContactId)
        }

        // Ensure incomplete data sets are not returned.
        if (cancel()) {
            clear()
        }
    }
} ?: emptySet()

private class ProfileQueryResult private constructor(
    override val contact: Contact?,
    override val isRedacted: Boolean
) : ProfileQuery.Result {

    constructor(contact: Contact?) : this(contact, false)

    override fun toString(): String =
        """
            ProfileQuery.Result {
                Profile contact: $contact
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileQuery.Result = ProfileQueryResult(
        contact?.redactedCopy(),
        isRedacted = true
    )
}