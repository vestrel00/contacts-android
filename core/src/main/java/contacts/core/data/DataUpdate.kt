package contacts.core.data

import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.operation.updateOperation
import contacts.core.util.applyBatch
import contacts.core.util.isEmpty
import contacts.core.util.unsafeLazy

/**
 * Updates one or more Profile OR non-Profile (depending on instance) data rows in the data table.
 *
 * Updating data that has already been deleted may return a successful result. However, no update
 * actually occurred in the Content Provider Data table because the data row no longer existed.
 *
 * ## Blank data are deleted
 *
 * Blank data will be deleted. For example, if all properties of an email are all null, empty, or
 * blank, then the email is deleted. This is the same behavior as the native Contacts app. This
 * behavior cannot be modified.
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
 * ```kotlin
 * val result = dataUpdate
 *      .data(existingDataEntities)
 *      .commit()
 * ```
 */
interface DataUpdate {

    /**
     * Specifies that only the given set of [fields] (data) will be updated.
     *
     * If no fields are specified, then all fields will be updated. Otherwise, only the specified
     * fields will be updated in addition to required API fields [Fields.Required] (e.g. IDs),
     * which are always included.
     *
     * Note that this may affect performance. It is recommended to only include fields that will be
     * used to save CPU and memory.
     *
     * ## Performing updates on entities with partial includes
     *
     * When the query include function is used, only certain data will be included in the returned
     * entities. All other data are guaranteed to be null (except for those in [Fields.Required]).
     * When performing updates on entities that have only partial data included, make sure to use
     * the same included fields in the update operation as the included fields used in the query.
     * This will ensure that the set of data queried and updated are the same. For example, in order
     * to get and set only email addresses and leave everything the same in the database...
     *
     * ```kotlin
     * val data = emailQuery.include(Fields.Email.Address).find()
     * val mutableData = setEmailAddresses(data)
     * update.data(mutableData).include(Fields.Email.Address).commit()
     * ```
     *
     * On the other hand, you may intentionally include only some data and perform updates without
     * on all data (not just the included ones) to effectively delete all non-included data. This
     * is, currently, a feature- not a bug! For example, in order to get and set only email
     * addresses and set all other data to null (such as phone numbers, name, etc) in the database..
     *
     * ```kotlin
     * val data = emailQuery.include(Fields.Email.Address).find()
     * val mutableData = setEmailAddresses(data)
     * update.data(mutableData).include(Fields.Email.all).commit()
     * ```
     *
     * This gives you the most flexibility when it comes to specifying what fields to
     * include/exclude in queries, inserts, and update, which will allow you to do things beyond
     * your wildest imagination!
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
     * Adds the given [data] to the update queue, which will be updated on [commit].
     *
     * Blank data ([ExistingDataEntity.isBlank] will be deleted instead.
     *
     * Only existing [data] that have been retrieved via a query will be added to the update queue.
     * Those that have been manually created via a constructor will be ignored and result in a
     * failed operation.
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

    interface Result {
        /**
         * True if all data have successfully been updated. False if even one update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [data] has been successfully updated. False otherwise.
         */
        fun isSuccessful(data: ExistingDataEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun DataUpdate(contacts: Contacts, isProfile: Boolean): DataUpdate = DataUpdateImpl(
    contacts.applicationContext.contentResolver,
    contacts.permissions,
    contacts.customDataRegistry,
    isProfile
)

private class DataUpdateImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val customDataRegistry: CustomDataRegistry,
    private val isProfile: Boolean,
    private var include: Include<AbstractDataField> = allDataFields(customDataRegistry),
    private val data: MutableSet<ExistingDataEntity> = mutableSetOf()
) : DataUpdate {

    override fun toString(): String =
        """
            DataUpdate {
                isProfile: $isProfile
                include: $include
                data: $data
            }
        """.trimIndent()

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): DataUpdate = apply {
        include = if (fields.isEmpty()) {
            allDataFields(customDataRegistry)
        } else {
            Include(fields + Fields.Required.all.asSequence())
        }
    }

    override fun data(vararg data: ExistingDataEntity) = data(data.asSequence())

    override fun data(data: Collection<ExistingDataEntity>) = data(data.asSequence())

    override fun data(data: Sequence<ExistingDataEntity>): DataUpdate = apply {
        this.data.addAll(data)
    }

    override fun commit(): DataUpdate.Result = commit { false }

    override fun commit(cancel: () -> Boolean): DataUpdate.Result {
        if (data.isEmpty() || !permissions.canUpdateDelete() || cancel()) {
            return DataUpdateFailed()
        }

        val results = mutableMapOf<Long, Boolean>()
        for (data in data) {
            if (cancel()) {
                break
            }

            val dataId = data.id
            if (dataId != null) {
                results[dataId] = if (data.isProfile != isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // intended profile or non-profile data updates. Otherwise, operation can
                    // succeed. This is only done to enforce API design.
                    false
                } else {
                    contentResolver.updateData(include.fields, data, customDataRegistry)
                }
            } else {
                results[INVALID_ID] = false
            }
        }
        return DataUpdateResult(results)
    }

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
    }
}

private fun ContentResolver.updateData(
    includeFields: Set<AbstractDataField>,
    data: ExistingDataEntity,
    customDataRegistry: CustomDataRegistry
): Boolean = data.updateOperation(includeFields, customDataRegistry)?.let { applyBatch(it) } != null

private class DataUpdateResult(private val dataIdsResultMap: Map<Long, Boolean>) :
    DataUpdate.Result {

    override val isSuccessful: Boolean by unsafeLazy { dataIdsResultMap.all { it.value } }

    override fun isSuccessful(data: ExistingDataEntity): Boolean {
        val dataId = data.id
        return dataId != null && dataIdsResultMap.getOrElse(dataId) { false }
    }
}

private class DataUpdateFailed : DataUpdate.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(data: ExistingDataEntity): Boolean = false
}
