package contacts.core.data

import contacts.core.*
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.operation.updateOperation
import contacts.core.util.applyBatch
import contacts.core.util.contacts
import contacts.core.util.isEmpty

/**
 * Updates one or more Profile OR non-Profile (depending on instance) data in the Contacts Provider
 * database to ensure that it contains the same data as those provided in [data].
 *
 * Updating data that has already been deleted may return a successful result. However, no update
 * actually occurred in the Content Provider Data table because the data row no longer existed.
 *
 * ## Blank data are deleted
 *
 * Blank data will be deleted, unless the corresponding fields are not provided in [include].
 * For example, if all properties of an email are all null, empty, or blank, then the email is
 * deleted. This is the same behavior as the AOSP Contacts app.
 *
 * Note that in cases where blank data are deleted, existing RawContact instances (in memory) will
 * still have references to the deleted data instance. The RawContact instances (in memory) must be
 * refreshed to get the most up-to-date data sets.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All updates will do nothing if these permissions are not granted.
 *
 * ## Usage
 *
 * To update a set of [ExistingDataEntity];
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = dataUpdate.data(existingDataEntities).commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * Result result = dataUpdate.data(existingDataEntities).commit();
 * ```
 */
interface DataUpdate : CrudApi {

    /**
     * Specifies that only the given set of [fields] (data) will be updated.
     *
     * If no fields are specified (empty list), then all fields will be updated. Otherwise, only
     * the specified fields will be updated.
     *
     * Blank data are deleted on update, unless the corresponding fields are NOT included.
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
     * indicate that include field checks should be disabled. Implementations of
     * [contacts.core.entities.operation.AbstractDataOperation] and other similar operations classes
     * treat empty list vs null field sets differently. If the included field set is...
     *
     * - null, then the included field checks are disabled. This means that any non-blank data will
     *   be processed. This is a more optimal, recommended way of including all fields.
     * - not null but empty, then data will be skipped (no-op).
     *
     * Note that internal operations class instances may receive an empty list of fields instead of
     * null when the **intersection** of the corresponding set of all fields and the
     * non-null&non-empty set of included fields... is empty.
     */
    fun include(vararg fields: AbstractDataField): DataUpdate

    /**
     * See [DataUpdate.include].
     */
    fun include(fields: Collection<AbstractDataField>): DataUpdate

    /**
     * See [DataUpdate.include].
     */
    fun include(fields: Sequence<AbstractDataField>): DataUpdate

    /**
     * See [DataUpdate.include].
     */
    fun include(fields: Fields.() -> Sequence<AbstractDataField>): DataUpdate

    /**
     * Adds the given [data] to the update queue, which will be updated on [commit].
     *
     * Blank data ([ExistingDataEntity.isBlank] will be deleted instead.
     */
    fun data(vararg data: ExistingDataEntity): DataUpdate

    /**
     * See [DataUpdate.data].
     */
    fun data(data: Collection<ExistingDataEntity>): DataUpdate

    /**
     * See [DataUpdate.data].
     */
    fun data(data: Sequence<ExistingDataEntity>): DataUpdate

    /**
     * Updates the [ExistingDataEntity]s in the queue (added via [data]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Updates the [ExistingDataEntity]s in the queue (added via [data]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo updates. This means that depending on when the cancellation
     * occurs, some if not all of the data in the update queue may have already been updated.**
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
    override fun redactedCopy(): DataUpdate

    interface Result : CrudApi.Result {
        /**
         * True if all data have successfully been updated. False if even one update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [data] has been successfully updated. False otherwise.
         */
        fun isSuccessful(data: ExistingDataEntity): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun DataUpdate(contacts: Contacts, isProfile: Boolean): DataUpdate = DataUpdateImpl(
    contacts, isProfile
)

private class DataUpdateImpl(
    override val contactsApi: Contacts,
    private val isProfile: Boolean,

    private var include: Include<AbstractDataField>? = null,
    private val data: MutableSet<ExistingDataEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : DataUpdate {

    override fun toString(): String =
        """
            DataUpdate {
                isProfile: $isProfile
                include: $include
                data: $data
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): DataUpdate = DataUpdateImpl(
        contactsApi, isProfile,

        include,
        // Redact contact data.
        data.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): DataUpdate = apply {
        include = if (fields.isEmpty()) {
            null // Set to null to disable include field checks, for optimization purposes.
        } else {
            Include(fields + Fields.Required.all.asSequence())
        }
    }

    override fun include(fields: Fields.() -> Sequence<AbstractDataField>) = include(fields(Fields))

    override fun data(vararg data: ExistingDataEntity) = data(data.asSequence())

    override fun data(data: Collection<ExistingDataEntity>) = data(data.asSequence())

    override fun data(data: Sequence<ExistingDataEntity>): DataUpdate = apply {
        this.data.addAll(data.redactedCopiesOrThis(isRedacted))
    }

    override fun commit(): DataUpdate.Result = commit { false }

    override fun commit(cancel: () -> Boolean): DataUpdate.Result {
        onPreExecute()

        return if (data.isEmpty() || !permissions.canUpdateDelete() || cancel()) {
            DataUpdateFailed()
        } else {
            val results = mutableMapOf<Long, Boolean>()
            for (data in data) {
                if (cancel()) {
                    break
                }

                results[data.id] = if (data.isProfile != isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // intended profile or non-profile data updates. Otherwise, operation can
                    // succeed. This is only done to enforce API design.
                    false
                } else {
                    contactsApi.updateData(include?.fields, data, customDataRegistry)
                }
            }
            DataUpdateResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private fun Contacts.updateData(
    // Disable include checks when field set is null.
    includeFields: Set<AbstractDataField>?,
    data: ExistingDataEntity,
    customDataRegistry: CustomDataRegistry
): Boolean = data.updateOperation(
    callerIsSyncAdapter = callerIsSyncAdapter,
    includeFields = includeFields,
    customDataRegistry = customDataRegistry
)?.let { contentResolver.applyBatch(it) } != null

private class DataUpdateResult private constructor(
    private val dataIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : DataUpdate.Result {

    constructor(dataIdsResultMap: Map<Long, Boolean>) : this(dataIdsResultMap, false)

    override fun toString(): String =
        """
            DataUpdate.Result {
                isSuccessful: $isSuccessful
                dataIdsResultMap: $dataIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): DataUpdate.Result = DataUpdateResult(
        dataIdsResultMap,
        isRedacted = true
    )

    override val isSuccessful: Boolean by lazy {
        // By default, all returns true when the collection is empty. So, we override that.
        dataIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(data: ExistingDataEntity): Boolean {
        return dataIdsResultMap.getOrElse(data.id) { false }
    }
}

private class DataUpdateFailed private constructor(
    override val isRedacted: Boolean
) : DataUpdate.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            DataUpdate.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): DataUpdate.Result = DataUpdateFailed(true)

    override val isSuccessful: Boolean = false

    override fun isSuccessful(data: ExistingDataEntity): Boolean = false
}
