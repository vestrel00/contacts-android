package contacts.core.sim

import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.SimContact
import contacts.core.entities.mapper.simContactMapper
import contacts.core.entities.table.Table
import contacts.core.util.query

/**
 * Queries on the SIM card table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.READ_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. If not granted, the query will do nothing and return an empty list.
 *
 * ## Usage
 *
 * Here is an example query that returns all of the contacts in the SIM card.
 *
 * In Kotlin,
 *
 * ```kotlin
 * val simContacts = query.find()
 * ```
 *
 * In Java,
 *
 * ```java
 * List<SimContact> simContacts = query.find();
 * ```
 *
 * ## Limitations
 *
 * Projection (include), selection (where), order, limit, and offset are not supported by the
 * SIM Contacts table. Therefore, you are only able to get all SIM contacts when querying.
 *
 * Depending on memory size, SIM cards can hold 200 to 500+ contacts. The most common being around
 * 250. Most, if not all, SIM cards have less than 1mb memory (averaging 32KB to 64KB). Therefore,
 * memory and speed should not be affected much by not being able to sort/order and paginate.
 *
 * You may perform your own sorting and pagination if you wish.
 */
interface SimContactsQuery : CrudApi {

    /**
     * Returns the [Result] matching the preceding query options.
     *
     * ## Privileges
     *
     * Requires [ContactsPermissions.READ_PERMISSION]. Returns an empty result otherwise.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun find(): Result

    /**
     * Returns the [Result] matching the preceding query options.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION]. Returns an empty result otherwise.
     *
     * ## Cancellation
     *
     * The number of SIM contact data found may take more than a few milliseconds to process.
     * Therefore, cancellation is supported while the SIM contact list is being built.
     * To cancel at any time, the [cancel] function should return true.
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
    override fun redactedCopy(): SimContactsQuery

    /**
     * A list of [SimContact]s.
     *
     * ## The [toString] function
     *
     * The [toString] function of instances of this will not return the string representation of
     * every SIM contact in the list. It will instead return a summary of the SIM contacts in
     * the list and perhaps the first [SimContact] only.
     *
     * This is done due to the potentially large quantities of SIM contacts, which could block
     * the UI if not logging in background threads.
     *
     * You may print individual SIM contacts in this list by iterating through it.
     */
    interface Result : List<SimContact>, CrudApi.Result {

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun SimContactsQuery(contacts: Contacts): SimContactsQuery = SimContactsQueryImpl(contacts)

private class SimContactsQueryImpl(
    override val contactsApi: Contacts,
    override val isRedacted: Boolean = false
) : SimContactsQuery {

    override fun toString(): String =
        """
            SimContactsQuery {
                hasPermission: ${permissions.canQuery()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsQuery = SimContactsQueryImpl(
        contactsApi,
        isRedacted = true
    )

    override fun find(): SimContactsQuery.Result = find { false }

    override fun find(cancel: () -> Boolean): SimContactsQuery.Result {
        onPreExecute()

        return if (!permissions.canQuery()) {
            SimContactsQueryResult(emptyList())
        } else {
            contentResolver.resolve(cancel)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private fun ContentResolver.resolve(cancel: () -> Boolean): SimContactsQuery.Result = query(
    Table.SimContacts,
    // The actual database query selection is not supported. However, we still need to include all
    // fields so that our custom cursors will not return null.
    Include(SimContactsFields),
    null
) {
    val simContactsList = mutableListOf<SimContact>()
    val simContactMapper = it.simContactMapper()

    while (!cancel() && it.moveToNext()) {
        simContactsList.add(simContactMapper.value)
    }

    // Ensure incomplete data sets are not returned.
    if (cancel()) {
        simContactsList.clear()
    }

    SimContactsQueryResult(simContactsList)

} ?: SimContactsQueryResult(emptyList())

private class SimContactsQueryResult private constructor(
    simContacts: List<SimContact>,
    override val isRedacted: Boolean
) : ArrayList<SimContact>(simContacts), SimContactsQuery.Result {

    constructor(simContacts: List<SimContact>) : this(simContacts, false)

    override fun toString(): String =
        """
            SimContactsQuery.Result {
                Number of SIM contacts found: $size
                First SIM contact: ${firstOrNull()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsQuery.Result = SimContactsQueryResult(
        redactedCopies(),
        isRedacted = true
    )
}