package com.vestrel00.contacts.profile

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.cursor.rawContactsCursor
import com.vestrel00.contacts.entities.mapper.ContactsMapper
import com.vestrel00.contacts.entities.table.ProfileUris
import com.vestrel00.contacts.util.isEmpty
import com.vestrel00.contacts.util.query
import com.vestrel00.contacts.util.toRawContactsWhere
import com.vestrel00.contacts.util.unsafeLazy

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
     * If [includeBlanks] is set to true, then queries may include blank RawContacts. Otherwise,
     * blanks are not be included. If the Profile Contact only contains blank RawContacts, then
     * the query will still return the blank Contact regardless of this flag. This flag is set to
     * true by default.
     *
     * This flag is set to true by default, which results in more database queries so setting this
     * to false will increase performance, especially for large Contacts databases.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The native Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the native Contacts app shows them.
     *
     * ## Performance
     *
     * This may require one or more additional queries, internally performed in this function, which
     * increases the time it takes for [find] to complete. Therefore, you should only specify this
     * if you actually need it.
     */
    fun includeBlanks(includeBlanks: Boolean): ProfileQuery

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

    private var includeBlanks: Boolean = DEFAULT_INCLUDE_BLANKS,
    private var rawContactsWhere: Where<RawContactsField>? = DEFAULT_RAW_CONTACTS_WHERE,
    private var include: Include<AbstractDataField> = DEFAULT_INCLUDE
) : ProfileQuery {

    override fun toString(): String =
        """
            ProfileQuery {
                includeBlanks: $includeBlanks
                rawContactsWhere: $rawContactsWhere
                include: $include
            }
        """.trimIndent()

    override fun includeBlanks(includeBlanks: Boolean): ProfileQuery = apply {
        this.includeBlanks = includeBlanks
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

        return contentResolver.resolve(includeBlanks, rawContactsWhere, include, cancel)
    }

    private companion object {
        const val DEFAULT_INCLUDE_BLANKS = true
        val DEFAULT_RAW_CONTACTS_WHERE: Where<RawContactsField>? = null
        val DEFAULT_INCLUDE by unsafeLazy { Include(Fields) }
        val REQUIRED_INCLUDE_FIELDS by unsafeLazy { Fields.Required.all.asSequence() }
    }
}

private fun ContentResolver.resolve(
    includeBlanks: Boolean,
    rawContactsWhere: Where<RawContactsField>?,
    include: Include<AbstractDataField>,
    cancel: () -> Boolean
): Contact? {
    // Note that we can perform an optimization here. When rawContactsWhere is null, we don't need
    // to retrieve rawContactIds and query for its data one by one. We can simply perform one query
    // to get all data of all RawContacts. I didn't do this because it adds more lines of code for
    // something that will barely be used. Most profiles will only consist of one RawContact anyway.

    val rawContactIds = rawContactIds(rawContactsWhere, cancel)

    // Data table queries using profile uris only return user profile data.
    // Yes, I am aware of IS_USER_PROFILE and RAW_CONTACT_IS_USER_PROFILE but those seem to cause
    // queries to throw an exception.
    val contactsMapper = ContactsMapper(isProfile = true, cancel = cancel)

    // Get the Data, RawContacts, and Contact from the Data table.
    if (!cancel() && rawContactIds.isNotEmpty()) {
        query(
            ProfileUris.DATA.uri,
            include,
            Fields.RawContact.Id `in` rawContactIds,
            processCursor = contactsMapper::processDataCursor
        )
    }

    // If Contact only has blank RawContacts (no Data rows), then we need to query the Contacts
    // table. This should be done regardless of the includeBlanks flag. This query should be done
    // before a potential query to the RawContacts table so that the mapper saves the Contact with
    // the values in the Contacts table instead of the RawContacts table. Some fields such as
    // options are different depending on the table. The Contacts and Data table contains options
    // of the Contacts. The RawContacts table contains the options of the RawContacts. This libs'
    // Contact model requires the options of the Contacts table.
    if (!cancel() && rawContactIds.isNotEmpty() && contactsMapper.contactIds.isEmpty()) {
        query(
            ProfileUris.CONTACTS.uri,
            include.onlyContactsFields(),
            null,
            processCursor = contactsMapper::processContactsCursor
        )
    }

    // Get the blank RawContacts (no Data rows), which are those RawContacts that have not been
    // retrieved from the data query.
    val blankRawContactIds = rawContactIds.minus(contactsMapper.rawContactIds)
    if (!cancel() && includeBlanks && blankRawContactIds.isNotEmpty()) {
        query(
            ProfileUris.RAW_CONTACTS.uri,
            include.onlyRawContactsFields(),
            RawContactsFields.Id `in` blankRawContactIds,
            processCursor = contactsMapper::processRawContactsCursor
        )
    }

    return contactsMapper.map().firstOrNull()
}

private fun ContentResolver.rawContactIds(
    rawContactsWhere: Where<RawContactsField>?, cancel: () -> Boolean
): Set<Long> = query(
    ProfileUris.RAW_CONTACTS.uri,
    Include(RawContactsFields.Id),
    // There may be lingering RawContacts whose associated contact was already deleted.
    // Such RawContacts have contact id column value as null.
    RawContactsFields.ContactId.isNotNull() and rawContactsWhere
) {
    mutableSetOf<Long>().apply {
        val rawContactsCursor = it.rawContactsCursor()
        while (!cancel() && it.moveToNext()) {
            rawContactsCursor.rawContactId?.let(::add)
        }

        // Ensure incomplete data sets are not returned.
        if (cancel()) {
            clear()
        }
    }
} ?: emptySet()