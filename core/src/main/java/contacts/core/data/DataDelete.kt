package contacts.core.data

import android.content.ContentProviderOperation.newDelete
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.operation.withSelection
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.util.applyBatch
import contacts.core.util.isProfileId
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more Profile OR non-Profile (depending on instance) data rows the data table.
 *
 * Note that deleting data will not remove it from existing RawContact instances. The RawContact
 * instances must be refreshed to get the most up-to-date data.
 *
 * Deleting data that has already been deleted may return a successful result. However, no delete
 * actually occurred in the Content Provider Data table because the data row no longer existed.
 * This also applies to data that has not yet been inserted.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete a set of [ExistingDataEntity];
 *
 * ```kotlin
 * val result = dataDelete
 *      .data(existingDataEntities)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * DataDelete.Result result = dataDelete
 *      .data(existingDataEntities)
 *      .commit()
 * ```
 */
interface DataDelete {

    /**
     * Adds the given [data] to the delete queue, which will be deleted on [commit].
     */
    fun data(vararg data: ExistingDataEntity): DataDelete

    /**
     * See [DataDelete.data].
     */
    fun data(data: Collection<ExistingDataEntity>): DataDelete

    /**
     * See [DataDelete.data].
     */
    fun data(data: Sequence<ExistingDataEntity>): DataDelete

    /**
     * Deletes the [ExistingDataEntity]s in the queue (added via [data]) and returns the [Result].
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
     * Deletes the [ExistingDataEntity]s in the queue (added via [data]) in one transaction. Either ALL
     * deletes succeed or ALL fail.
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
    fun commitInOneTransaction(): Boolean

    interface Result {

        /**
         * True if all data have successfully been deleted. False if even one delete failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [data] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(data: ExistingDataEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun DataDelete(contacts: Contacts, isProfile: Boolean): DataDelete = DataDeleteImpl(
    contacts.applicationContext.contentResolver,
    contacts.permissions,
    isProfile
)

private class DataDeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val isProfile: Boolean,
    private val dataIds: MutableSet<Long> = mutableSetOf()
) : DataDelete {

    override fun toString(): String =
        """
            DataDelete {
                isProfile: $isProfile
                dataIds: $dataIds
            }
        """.trimIndent()

    override fun data(vararg data: ExistingDataEntity) = data(data.asSequence())

    override fun data(data: Collection<ExistingDataEntity>) = data(data.asSequence())

    override fun data(data: Sequence<ExistingDataEntity>): DataDelete = apply {
        dataIds.addAll(data.map { it.id })
    }

    override fun commit(): DataDelete.Result {
        if (dataIds.isEmpty() || !permissions.canUpdateDelete()) {
            return DataDeleteFailed()
        }

        val dataIdsResultMap = mutableMapOf<Long, Boolean>()
        for (dataId in dataIds) {
            dataIdsResultMap[dataId] =
                if (dataId.isProfileId != isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for profile
                    // or non-profile deletes. Otherwise, operation can succeed. This is only done
                    // to enforce API design.
                    false
                } else {
                    contentResolver.deleteDataWithId(dataId)
                }
        }

        return DataDeleteResult(dataIdsResultMap)
    }

    override fun commitInOneTransaction(): Boolean {
        if (dataIds.isEmpty() || !permissions.canUpdateDelete()) {
            return false
        }

        val validDataIds = dataIds.filter { it.isProfileId == isProfile }

        if (dataIds.size != validDataIds.size) {
            // There are some invalid ids or profile or non-profile data ids, fail without
            // performing operation.
            return false
        }

        return contentResolver.deleteDataRowsWithIds(dataIds, isProfile)
    }
}

private fun ContentResolver.deleteDataWithId(dataId: Long): Boolean = applyBatch(
    newDelete(if (dataId.isProfileId) ProfileUris.DATA.uri else Table.Data.uri)
        .withSelection(Fields.DataId equalTo dataId)
        .build()
) != null

private fun ContentResolver.deleteDataRowsWithIds(
    dataIds: Collection<Long>, isProfile: Boolean
): Boolean = applyBatch(
    newDelete(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
        .withSelection(Fields.DataId `in` dataIds)
        .build()
) != null

private class DataDeleteResult(
    private val dataIdsResultMap: Map<Long, Boolean>
) : DataDelete.Result {

    override val isSuccessful: Boolean by unsafeLazy { dataIdsResultMap.all { it.value } }

    override fun isSuccessful(data: ExistingDataEntity): Boolean =
        dataIdsResultMap.getOrElse(data.id) { false }
}

private class DataDeleteFailed : DataDelete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(data: ExistingDataEntity): Boolean = false
}