package contacts.core.blockednumbers

import android.annotation.TargetApi
import android.content.ContentResolver
import android.os.Build
import android.provider.BlockedNumberContract
import contacts.core.*
import contacts.core.blockednumbers.BlockedNumbersInsert.Result.FailureReason
import contacts.core.entities.NewBlockedNumber
import contacts.core.entities.operation.BlockedNumbersOperation
import contacts.core.util.applyBlockedNumberBatch
import contacts.core.util.unsafeLazy

/**
 * Inserts one or more user blocked numbers into the blocked numbers table.
 *
 * ## Privileges
 *
 * Requires [BlockedNumbersPrivileges.canReadAndWrite]. All inserts will do nothing if these
 * privileges are not acquired.
 *
 * ## Usage
 *
 * To insert a number to block (i.e. (555) 555-5555),
 *
 * ```kotlin
 * val result = blockedNumbersInsert
 *      .blockedNumbers(NewBlockedNumber(number = "(555) 555-5555"))
 *      .commit()
 * ```
 */
interface BlockedNumbersInsert : CrudApi {

    /**
     * Adds a new [NewBlockedNumber] to the insert queue, which will be inserted on [commit].
     * The new instance is configured by the [configureBlockedNumber] function.
     */
    fun blockedNumber(configureBlockedNumber: NewBlockedNumber.() -> Unit): BlockedNumbersInsert

    /**
     * Adds the given [blockedNumbers] to the insert queue, which will be inserted on [commit].
     * Duplicates (blocked numbers with identical attributes to already added blocked numbers) are
     * ignored.
     */
    fun blockedNumbers(vararg blockedNumbers: NewBlockedNumber): BlockedNumbersInsert

    /**
     * See [BlockedNumbersInsert.blockedNumbers].
     */
    fun blockedNumbers(blockedNumbers: Collection<NewBlockedNumber>): BlockedNumbersInsert

    /**
     * See [BlockedNumbersInsert.blockedNumbers].
     */
    fun blockedNumbers(blockedNumbers: Sequence<NewBlockedNumber>): BlockedNumbersInsert

    /**
     * Inserts the [NewBlockedNumber]s in the queue (added via [blockedNumbers]) and returns the
     * [Result].
     *
     * ## Privileges
     *
     * Requires [BlockedNumbersPrivileges.canReadAndWrite]. This will do nothing if these privileges
     * are not acquired.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Inserts the [NewBlockedNumber]s in the queue (added via [blockedNumbers]) and returns the
     * [Result].
     *
     * ## Privileges
     *
     * Requires [BlockedNumbersPrivileges.canReadAndWrite]. This will do nothing if these privileges
     * are not acquired.
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo insertions. This means that depending on when the cancellation
     * occurs, some if not all of the BlockedNumbers in the insert queue may have already been
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
    override fun redactedCopy(): BlockedNumbersInsert

    interface Result : CrudApi.Result {

        /**
         * The list of IDs of successfully created BlockedNumbers.
         */
        val blockedNumberIds: List<Long>

        /**
         * True if all NewBlockedNumbers have successfully been inserted. False if even one insert
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [blockedNumber] has been successfully inserted. False otherwise.
         */
        fun isSuccessful(blockedNumber: NewBlockedNumber): Boolean

        /**
         * Returns the ID of the newly created BlockedNumber. Use the ID to get the newly created
         * BlockedNumber via a query.
         *
         * Returns null if the insert operation failed.
         */
        fun blockedNumberId(blockedNumber: NewBlockedNumber): Long?

        /**
         * Returns the reason why the insert failed for this [blockedNumber].
         * Null if it did not fail.
         */
        fun failureReason(blockedNumber: NewBlockedNumber): FailureReason?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result

        enum class FailureReason {

            /**
             * Duplicate (unformatted) numbers are allowed but would lead to unnecessary
             * duplication. Therefore, the insert is not attempted if the number is already blocked.
             * This is the same behavior as the builtin native blocked numbers activity.
             */
            NUMBER_ALREADY_BLOCKED,

            /**
             * Blank numbers (null, empty, or only spaces) are not allowed. This is the same
             * behavior as the builtin native blocked numbers activity.
             */
            NUMBER_IS_BLANK,

            /**
             * The update failed because of no privileges, no blocked numbers specified for insert,
             * etc...
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

@Suppress("FunctionName")
internal fun BlockedNumbersInsert(contacts: Contacts): BlockedNumbersInsert =
    BlockedNumbersInsertImpl(contacts, BlockedNumbersPrivileges(contacts.applicationContext))

private class BlockedNumbersInsertImpl(
    override val contactsApi: Contacts,
    private val privileges: BlockedNumbersPrivileges,

    private val blockedNumbers: MutableSet<NewBlockedNumber> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : BlockedNumbersInsert {

    override fun toString(): String =
        """
            BlockedNumbersInsert {
                blockedNumbers: $blockedNumbers
                hasPrivileges: ${privileges.canReadAndWrite()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersInsert = BlockedNumbersInsertImpl(
        contactsApi,
        privileges,

        // Redact blockedNumber data.
        blockedNumbers.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun blockedNumber(configureBlockedNumber: NewBlockedNumber.() -> Unit) =
        blockedNumbers(NewBlockedNumber().apply(configureBlockedNumber))

    override fun blockedNumbers(vararg blockedNumbers: NewBlockedNumber) =
        blockedNumbers(blockedNumbers.asSequence())

    override fun blockedNumbers(blockedNumbers: Collection<NewBlockedNumber>) =
        blockedNumbers(blockedNumbers.asSequence())

    override fun blockedNumbers(blockedNumbers: Sequence<NewBlockedNumber>): BlockedNumbersInsert =
        apply {
            this.blockedNumbers.addAll(blockedNumbers.redactedCopiesOrThis(isRedacted))
        }

    override fun commit(): BlockedNumbersInsert.Result = commit { false }

    @TargetApi(Build.VERSION_CODES.N)
    override fun commit(cancel: () -> Boolean): BlockedNumbersInsert.Result {
        onPreExecute()

        return if (blockedNumbers.isEmpty() || !privileges.canReadAndWrite() || cancel()) {
            BlockedNumbersInsertFailed()
        } else {

            val results = mutableMapOf<NewBlockedNumber, Long?>()
            val failureReasons = mutableMapOf<NewBlockedNumber, FailureReason>()

            for (blockedNumber in blockedNumbers) {
                if (cancel()) {
                    break
                }
                // Use the builtin query to check if the number is already blocked because it may be
                // difficult to use the query APIs we built to match the matching algorithm used
                // by the Blocked Number Provider.
                results[blockedNumber] = if (blockedNumber.number.isNullOrBlank()) {
                    failureReasons[blockedNumber] = FailureReason.NUMBER_IS_BLANK
                    null
                } else if (
                    BlockedNumberContract.isBlocked(
                        contactsApi.applicationContext,
                        blockedNumber.number
                    )
                ) {
                    failureReasons[blockedNumber] = FailureReason.NUMBER_ALREADY_BLOCKED
                    null
                } else {
                    contentResolver.insertBlockedNumber(blockedNumber).also { id ->
                        if (id == null) {
                            failureReasons[blockedNumber] = FailureReason.UNKNOWN
                        }
                    }
                }
            }
            BlockedNumbersInsertResult(results, failureReasons)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private fun ContentResolver.insertBlockedNumber(blockedNumber: NewBlockedNumber): Long? {
    val results = BlockedNumbersOperation().insert(blockedNumber)?.let {
        applyBlockedNumberBatch(it)
    }

    /*
     * The ContentProviderResult[0] contains the first result of the batch, which is the
     * BlockedNumberOperation. The uri contains the BlockedNumbers.COLUMN_ID as the last path
     * segment.
     *
     * E.G. "content://com.android.blockednumber/blocked/18"
     * In this case, 18 is the BlockedNumbers.COLUMN_ID.
     *
     * It is formed by the Contacts Provider using
     * Uri.withAppendedPath(BlockedNumbers.CONTENT_URI, "18")
     */
    return results?.firstOrNull()?.let { result ->
        val blockedNumberUri = result.uri
        val blockedNumberId = blockedNumberUri?.lastPathSegment?.toLongOrNull()
        blockedNumberId
    }
}

private class BlockedNumbersInsertResult private constructor(
    private val blockedNumbersMap: Map<NewBlockedNumber, Long?>,
    private val failureReasons: Map<NewBlockedNumber, FailureReason>,
    override val isRedacted: Boolean
) : BlockedNumbersInsert.Result {

    constructor(
        blockedNumbersMap: Map<NewBlockedNumber, Long?>,
        failureReasons: Map<NewBlockedNumber, FailureReason>
    ) : this(blockedNumbersMap, failureReasons, false)

    override fun toString(): String =
        """
            BlockedNumbersInsert.Result {
                isSuccessful: $isSuccessful
                blockedNumbersMap: $blockedNumbersMap
                failureReasons: $failureReasons
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersInsert.Result = BlockedNumbersInsertResult(
        blockedNumbersMap.redactedKeys(),
        failureReasons.redactedKeys(),
        isRedacted = true
    )

    override val blockedNumberIds: List<Long> by unsafeLazy {
        blockedNumbersMap.asSequence()
            .mapNotNull { it.value }
            .toList()
    }

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        blockedNumbersMap.run { isNotEmpty() && all { it.value != null } }
    }

    override fun isSuccessful(blockedNumber: NewBlockedNumber): Boolean =
        blockedNumberId(blockedNumber) != null

    override fun blockedNumberId(blockedNumber: NewBlockedNumber): Long? =
        blockedNumbersMap.getOrElse(blockedNumber) { null }

    override fun failureReason(blockedNumber: NewBlockedNumber): FailureReason? =
        failureReasons[blockedNumber]
}

private class BlockedNumbersInsertFailed private constructor(override val isRedacted: Boolean) :
    BlockedNumbersInsert.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            BlockedNumbersInsert.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersInsert.Result = BlockedNumbersInsertFailed(true)

    override val blockedNumberIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(blockedNumber: NewBlockedNumber): Boolean = false

    override fun blockedNumberId(blockedNumber: NewBlockedNumber): Long? = null

    override fun failureReason(blockedNumber: NewBlockedNumber) = FailureReason.UNKNOWN
}