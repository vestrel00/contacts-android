package contacts.core.sim

import contacts.core.*
import contacts.core.entities.NewSimContact
import contacts.core.entities.operation.SimContactsOperation
import contacts.core.entities.table.Table
import contacts.core.sim.SimContactsInsert.Result.FailureReason

/**
 * Inserts one or more user SIM contacts into the SIM contacts table.
 *
 * ## Blank contacts are not allowed
 *
 * Blank SimContacts (name AND number are both null or blank) will NOT be inserted. The name OR
 * number can be null or blank but not both.
 *
 * ## Character limits
 *
 * The `name` and `number` are subject to the SIM card's maximum character limit, which is typically
 * around 20-30 characters (in modern times). This may vary per SIM card. Inserts or updates will
 * fail if the limit is breached.
 *
 * ## SIM Card state
 *
 * The [SimCardInfo.isReady] is assumed to be true in these examples for brevity. If false, the
 * insert will do nothing.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All inserts will do nothing if this permission is not granted.
 *
 * ## Usage
 *
 * To insert a contact to the SIM card,
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = insert
 *      .simContact(NewSimContact(name = "Dude", number = "5555555555"))
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * SimContactsInsert.Result result = insert
 *      .simContact(new NewSimContact("Dude", "5555555555"))
 *      .commit();
 * ```
 */
interface SimContactsInsert : CrudApi {

    /**
     * Adds a new [NewSimContact] to the insert queue, which will be inserted on [commit].
     * The new instance is configured by the [configureSimContact] function.
     */
    fun simContact(configureSimContact: NewSimContact.() -> Unit): SimContactsInsert

    /**
     * Adds the given [simContacts] to the insert queue, which will be inserted on [commit].
     * Duplicates (SIM contacts with identical attributes to already added SIM contacts) are
     * ignored.
     */
    fun simContacts(vararg simContacts: NewSimContact): SimContactsInsert

    /**
     * See [SimContactsInsert.simContacts].
     */
    fun simContacts(simContacts: Collection<NewSimContact>): SimContactsInsert

    /**
     * See [SimContactsInsert.simContacts].
     */
    fun simContacts(simContacts: Sequence<NewSimContact>): SimContactsInsert

    /**
     * Inserts the [NewSimContact]s in the queue (added via [simContacts]) and returns the
     * [Result].
     *
     * ## SIM Card state
     *
     * Requires [SimCardInfo.isReady] to be true.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION]. This will do nothing if this permission is
     * not granted.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Inserts the [NewSimContact]s in the queue (added via [simContacts]) and returns the
     * [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION]. This will do nothing if this permission is
     * not granted.
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo insertions. This means that depending on when the cancellation
     * occurs, some if not all of the SimContacts in the insert queue may have already been
     * inserted.**
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun commit(cancel: () -> Boolean = { false }): Result
    fun commit(cancel: () -> Boolean): Result

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
    override fun redactedCopy(): SimContactsInsert

    interface Result : CrudApi.Result {

        /**
         * Contains all [NewSimContact] that have been successfully inserted.
         */
        val newSimContacts: Set<NewSimContact>

        /**
         * True if all [NewSimContact] have successfully been inserted. False if even one insert
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [simContact] has been successfully inserted. False otherwise.
         */
        fun isSuccessful(simContact: NewSimContact): Boolean

        /**
         * Returns the reason why the insert failed for this [simContact].
         * Null if it did not fail.
         */
        fun failureReason(simContact: NewSimContact): FailureReason?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result

        enum class FailureReason {

            /**
             * The [NewSimContact.name] has exceeded the max character limit.
             */
            NAME_EXCEEDED_MAX_CHAR_LIMIT,

            /**
             * The [NewSimContact.number] has exceeded the max character limit.
             */
            NUMBER_EXCEEDED_MAX_CHAR_LIMIT,

            /**
             * The [NewSimContact.name] and [NewSimContact.number] are both blank.
             */
            NAME_AND_NUMBER_ARE_BLANK,

            /**
             * The insert failed because of no SIM card in the ready state, no SimContacts
             * specified for insert, number is invalid, etc...
             *
             * ## Dev note
             *
             * We can probably add more reasons instead of just putting all others in the "unknown"
             * bucket. We'll see if consumers need to know about other failure reasons.
             */
            UNKNOWN
        }
    }
}

internal fun SimContactsInsert(contacts: Contacts): SimContactsInsert =
    SimContactsInsertImpl(contacts)

private class SimContactsInsertImpl(
    override val contactsApi: Contacts,

    private val simContacts: MutableSet<NewSimContact> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : SimContactsInsert {

    override fun toString(): String =
        """
            SimContactsInsert {
                simContacts: $simContacts
                hasPermission: ${permissions.canInsertToSim()}
                isSimCardReady: ${simCardInfo.isReady}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsInsert = SimContactsInsertImpl(
        contactsApi,

        // Redact SIM contact data.
        simContacts.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun simContact(configureSimContact: NewSimContact.() -> Unit) =
        simContacts(NewSimContact().apply(configureSimContact))

    override fun simContacts(vararg simContacts: NewSimContact) =
        simContacts(simContacts.asSequence())

    override fun simContacts(simContacts: Collection<NewSimContact>) =
        simContacts(simContacts.asSequence())

    override fun simContacts(simContacts: Sequence<NewSimContact>): SimContactsInsert =
        apply {
            this.simContacts.addAll(simContacts.redactedCopiesOrThis(isRedacted))
        }

    override fun commit(): SimContactsInsert.Result = commit { false }

    override fun commit(cancel: () -> Boolean): SimContactsInsert.Result {
        onPreExecute()

        return if (
            simContacts.isEmpty() ||
            !permissions.canInsertToSim() ||
            !simCardInfo.isReady ||
            cancel()
        ) {
            SimContactsInsertFailed()
        } else {

            val failureReasons = mutableMapOf<NewSimContact, FailureReason?>()

            for (simContact in simContacts) {
                if (cancel()) {
                    break
                }

                val maxCharacterLimits = simCardInfo.maxCharacterLimits()
                failureReasons[simContact] = if (simContact.isBlank) {
                    FailureReason.NAME_AND_NUMBER_ARE_BLANK
                } else if (simContact.name.length > maxCharacterLimits.nameMaxLength(cancel)) {
                    FailureReason.NAME_EXCEEDED_MAX_CHAR_LIMIT
                } else if (simContact.number.length > maxCharacterLimits.numberMaxLength(cancel)) {
                    FailureReason.NUMBER_EXCEEDED_MAX_CHAR_LIMIT
                } else if (!contactsApi.insertSimContact(simContact, cancel)) {
                    FailureReason.UNKNOWN
                } else {
                    null
                }
            }

            SimContactsInsertResult(failureReasons)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private val String?.length: Int
    get() = this?.let { length } ?: 0

internal fun Contacts.insertSimContact(simContact: NewSimContact, cancel: () -> Boolean): Boolean {
    val result = SimContactsOperation().insert(simContact)?.let {
        contentResolver.insert(Table.SimContacts.uri(), it)
    }

    // Successful result is always "content://icc/adn/0"
    var insertSuccess = result != null

    if (insertSuccess) {
        // If result is not null, query to make sure a new row is inserted in the SIM table. In some
        // devices, the result will be successful but no new row is added to the SIM table. If a
        // duplicate is being inserted (an entry with the same name and number already exists),
        // then this extra check is useless because we can only compare by name and number due to
        // the id being unavailable from the insert result.
        val insertedSimContact = getSimContacts(cancel)
            .find { it.name == simContact.name && it.number == simContact.number }
        if (insertedSimContact == null) {
            insertSuccess = false
        }
    }

    return insertSuccess
}

private class SimContactsInsertResult private constructor(
    private val failureReasons: Map<NewSimContact, FailureReason?>,
    override val isRedacted: Boolean
) : SimContactsInsert.Result {

    constructor(failureReasons: Map<NewSimContact, FailureReason?>) : this(failureReasons, false)

    override fun toString(): String =
        """
            SimContactsInsert.Result {
                isSuccessful: $isSuccessful
                failureReasons: $failureReasons
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsInsert.Result = SimContactsInsertResult(
        failureReasons.redactedKeys(),
        isRedacted = true
    )

    override val newSimContacts: Set<NewSimContact>
        get() = failureReasons.filter { it.value == null }.keys

    override val isSuccessful: Boolean by lazy {
        // By default, all returns true when the collection is empty. So, we override that.
        failureReasons.run { isNotEmpty() && all { it.value == null } }
    }

    override fun isSuccessful(simContact: NewSimContact): Boolean =
        failureReasons.containsKey(simContact) && failureReasons[simContact] == null

    override fun failureReason(simContact: NewSimContact): FailureReason? =
        failureReasons[simContact]
}

private class SimContactsInsertFailed private constructor(override val isRedacted: Boolean) :
    SimContactsInsert.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            SimContactsInsert.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsInsert.Result = SimContactsInsertFailed(true)

    override val newSimContacts: Set<NewSimContact> = emptySet()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(simContact: NewSimContact): Boolean = false

    override fun failureReason(simContact: NewSimContact) = FailureReason.UNKNOWN
}