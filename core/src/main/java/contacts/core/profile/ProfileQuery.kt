package contacts.core.profile

import android.accounts.Account
import android.content.ContentResolver
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
     * If no fields are specified, then all fields ([Fields.all]) are included. Otherwise, only the
     * specified fields will be included in addition to required API fields [Fields.Required]
     * (e.g. IDs), which are always included.
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
     * To include RawContact specific properties, use [ProfileQuery.includeRawContactsFields].
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
    private var include: Include<AbstractDataField> = contactsApi.includeAllFields(),
    private var includeRawContactsFields: Include<RawContactsField> = DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS,

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

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): ProfileQuery =
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

    override fun find(): ProfileQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): ProfileQuery.Result {
        onPreExecute()

        val profileContact = if (!permissions.canQuery()) {
            null
        } else {
            contentResolver.resolve(
                customDataRegistry, rawContactsWhere, include, includeRawContactsFields, cancel
            )
        }
        return ProfileQueryResult(profileContact)
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS by lazy { Include(RawContactsFields.all) }
        val REQUIRED_INCLUDE_FIELDS by lazy { Fields.Required.all.asSequence() }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by lazy {
            RawContactsFields.Required.all.asSequence()
        }
    }
}

private fun ContentResolver.resolve(
    customDataRegistry: CustomDataRegistry,
    rawContactsWhere: Where<RawContactsField>?,
    include: Include<AbstractDataField>,
    includeRawContactsFields: Include<RawContactsField>,
    cancel: () -> Boolean
): Contact? {
    val rawContactIds = rawContactIds(rawContactsWhere, cancel)

    if (cancel() || rawContactIds.isEmpty()) {
        return null
    }

    // Collect Contacts, RawContacts, and Data with this mapper.
    val contactsMapper = ContactsMapper(customDataRegistry, cancel)

    // Collect the profile Contact, if exist.
    query(
        ProfileUris.CONTACTS.uri,
        include.onlyContactsFields(),
        null,
        processCursor = contactsMapper::processContactsCursor
    )

    // Collect RawContacts.
    if (!cancel() && rawContactIds.isNotEmpty()) {
        query(
            ProfileUris.RAW_CONTACTS.uri,
            includeRawContactsFields,
            RawContactsFields.Id `in` rawContactIds,
            processCursor = contactsMapper::processRawContactsCursor
        )
    }

    // Collect Data
    if (!cancel() && rawContactIds.isNotEmpty()) {
        query(
            ProfileUris.DATA.uri,
            include,
            Fields.RawContact.Id `in` rawContactIds,
            processCursor = contactsMapper::processDataCursor
        )
    }

    return contactsMapper.mapContacts().firstOrNull()
}

private fun ContentResolver.rawContactIds(
    rawContactsWhere: Where<RawContactsField>?, cancel: () -> Boolean
): Set<Long> = query(
    ProfileUris.RAW_CONTACTS.uri,
    Include(RawContactsFields.Id),
    // There may be RawContacts that are marked for deletion that have not yet been deleted.
    (RawContactsFields.Deleted notEqualTo true) and rawContactsWhere
) {
    mutableSetOf<Long>().apply {
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