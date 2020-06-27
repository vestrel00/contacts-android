package com.vestrel00.contacts.profile

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.mapper.ContactsMapper
import com.vestrel00.contacts.util.isEmpty
import com.vestrel00.contacts.util.query
import com.vestrel00.contacts.util.toRawContactsWhere

/**
 * Queries the Contacts, RawContacts, and Data tables and returns the one and only profile
 * [Contact], if available.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All queries will return null if the permission is not granted.
 *
 * ## Usage
 *
 * Here is an example query that returns the profile [Contact]. Only RawContacts belonging to the
 * given account are included. Only the full name and email address attributes of the profile
 * [Contact] are included.
 *
 * In Kotlin,
 *
 * ```kotlin
 * import com.vestrel00.contacts.Fields.Name
 * import com.vestrel00.contacts.Fields.Address
 *
 * val profileContact : Contact? = profileQuery.
 *      .accounts(account)
 *      .include(Name, Address)
 *      .find()
 * ```
 *
 * In Java,
 *
 * ```java
 * import static com.vestrel00.contacts.Fields.*;
 *
 * List<Contact> contacts = profileQuery
 *      .accounts(account)
 *      .include(Name, Address)
 *      .find();
 * ```
 */
interface ProfileQuery {

    /**
     * Limits the RawContacts and associated data to those associated with the given accounts. The
     * Contact returned will not contain data that belongs to other accounts not specified in
     * [accounts].
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
     * Includes the given set of [fields] from [Fields] ([DataFields]) in the resulting contact
     * object.
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
     * example, the column name of name display name is the same as address formatted address, which
     * is 'data1'. This means that formatted address is also included when the name display name is
     * included. There is no workaround for this because the [ContentResolver.query] function only
     * takes in an array of column names.
     *
     * ## IMPORTANT
     *
     * Do not perform updates on contacts returned by a query where all fields are not included as
     * it will result in data loss!
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
     * Returns the profile [Contact], if available.
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
    fun find(): Contact?

    /**
     * Returns the profile [Contact], if available.
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
    // fun find(cancel: () -> Boolean = { false }): Contact?
    fun find(cancel: () -> Boolean): Contact?
}

@Suppress("FunctionName")
internal fun ProfileQuery(context: Context): ProfileQuery = ProfileQueryImpl(
    ContactsPermissions(context),
    context.contentResolver
)

private class ProfileQueryImpl(
    private val permissions: ContactsPermissions,
    private val contentResolver: ContentResolver,

    private var rawContactsWhere: Where? = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include = DEFAULT_INCLUDE
) : ProfileQuery {

    override fun toString(): String {
        return """
            rawContactsWhere = $rawContactsWhere
            include = $include
        """.trimIndent()
    }

    override fun accounts(vararg accounts: Account?) = accounts(accounts.asSequence())

    override fun accounts(accounts: Collection<Account?>) = accounts(accounts.asSequence())

    override fun accounts(accounts: Sequence<Account?>): ProfileQuery = apply {
        rawContactsWhere = accounts.toRawContactsWhere()
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): ProfileQuery = apply {
        include = if (fields.isEmpty()) {
            DEFAULT_INCLUDE
        } else {
            Include(fields + REQUIRED_INCLUDE_FIELDS)
        }
    }

    override fun find(): Contact? = find { false }

    override fun find(cancel: () -> Boolean): Contact? {
        if (!permissions.canQuery()) {
            return null
        }

        return contentResolver.resolve(rawContactsWhere, include, cancel)
    }

    private companion object {
        val DEFAULT_RAW_CONTACTS_WHERE: Where? = null
        val DEFAULT_INCLUDE = Include(Fields.all)
        val REQUIRED_INCLUDE_FIELDS = Fields.Required.all.asSequence()
    }
}

private fun ContentResolver.resolve(
    rawContactsWhere: Where?, include: Include, cancel: () -> Boolean
): Contact? {
    val rawContactIds = rawContactIds(rawContactsWhere, cancel)

    // Data table queries using profile uris only return user profile data.
    val contactsMapper = ContactsMapper(isProfile = true, cancel = cancel)
    for (rawContactId in rawContactIds) {
        query(
            ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI.buildUpon()
                .appendEncodedPath(rawContactId)
                .appendEncodedPath(ContactsContract.RawContacts.Data.CONTENT_DIRECTORY)
                .build(),
            include,
            null,
            processCursor = contactsMapper::processDataCursor
        )

        // FIXME? Blank RawContacts (no Data rows) are not included here...

        if (cancel()) {
            return null
        }
    }

    return contactsMapper.map().firstOrNull()
}

private fun ContentResolver.rawContactIds(
    rawContactsWhere: Where?, cancel: () -> Boolean
): Set<String> = query(
    ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI,
    Include(RawContactsFields.Id),
    rawContactsWhere
) {
    mutableSetOf<String>().apply {
        val rawContactsCursor = it.rawContactsCursor()
        while (!cancel() && it.moveToNext()) {
            rawContactsCursor.rawContactId?.let { add("$it") }
        }

        // Ensure incomplete data sets are not returned.
        if (cancel()) {
            clear()
        }
    }
} ?: emptySet()