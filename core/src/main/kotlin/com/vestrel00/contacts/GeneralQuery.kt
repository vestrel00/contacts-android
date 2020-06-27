package com.vestrel00.contacts

import android.content.ContentResolver
import com.vestrel00.contacts.entities.Contact

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
 * If you need more granularity and customizations when providing matching criteria (at the cost of
 * higher CPU and (temporary) memory usage) use [Query].
 *
 * To query specific types of data (e.g. emails, phones, etc), use DataQuery.
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
 * by the Contact display name primary (given name first) in ascending order (ignoring case). Only
 * the full name and email address attributes of the [Contact] objects are included.
 *
 * ```kotlin
 * import com.vestrel00.contacts.Fields.Name
 * import com.vestrel00.contacts.Fields.Address
 * import com.vestrel00.contacts.ContactsFields.DisplayNamePrimary
 *
 * val contacts : List<Contact> = search.
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
 * TODO
 *
 * Data matching is more sophisticated under the hood than [Query]. The Contacts Provider matches
 * parts of data in segments. For example, a Contact having the email "hologram@gram.net" will be
 * matched with the following texts;
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
 * Data matching is **case-insensitive**.
 *
 * ## Developer Notes
 *
 * TODO
 */
interface GeneralQuery {

    // TODO Accounts and groups?

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
     * There is only one scenario where blank RawContacts may not be returned if this flag is set to
     * false. A Contact that has a RawContact with Data row(s) and a RawContact with no Data rows.
     * In this case, the Contact and the RawContact with Data row(s) are not blank but the
     * RawContact with no Data row is blank.
     *
     * // TODO
     * 1. Query Contacts table for contactIds.
     *      - Use CONTENT_FILTER_URI if searchString is not null.
     *      - Use CONTENT_URI if searchString is null.
     * 2. Query the Data table with the given contactIds.
     * 3. If includeBlanks is true, query the RawContacts table with the given contactIds.
     */
    fun includeBlanks(includeBlanks: Boolean): GeneralQuery

    /**
     * Includes the given set of [fields] from [Fields] in the resulting contact object(s).
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
    fun include(vararg fields: Field): GeneralQuery

    /**
     * See [GeneralQuery.include].
     */
    fun include(fields: Collection<Field>): GeneralQuery

    /**
     * See [GeneralQuery.include].
     */
    fun include(fields: Sequence<Field>): GeneralQuery

    /**
     * Filters the [Contact]s matching the criteria defined by the [where]. If not specified or
     * null, then all [Contact]s are returned, limited by [limit].
     *
     * For more info, see [GeneralQuery] **Which Contact data are matched and how?** section.
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
     */
    fun orderBy(vararg orderBy: OrderBy): GeneralQuery

    /**
     * See [GeneralQuery.orderBy].
     */
    fun orderBy(orderBy: Collection<OrderBy>): GeneralQuery

    /**
     * See [GeneralQuery.orderBy].
     */
    fun orderBy(orderBy: Sequence<OrderBy>): GeneralQuery

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