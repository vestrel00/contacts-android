package com.vestrel00.contacts.data

import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.`in`
import com.vestrel00.contacts.entities.CommonDataEntity
import com.vestrel00.contacts.entities.operation.newDelete
import com.vestrel00.contacts.entities.operation.withSelection
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo
import com.vestrel00.contacts.util.applyBatch
import com.vestrel00.contacts.util.unsafeLazy

/**
 * Deletes one or more data the Data table.
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
 * To delete a [CommonDataEntity];
 *
 * ```kotlin
 * val result = dataDelete
 *      .data(data)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * DataDelete.Result result = dataDelete
 *      .data(data)
 *      .commit()
 * ```
 */
interface DataDelete {

    /**
     * Adds the given [data] to the delete queue, which will be deleted on [commit].
     *
     * Only existing [data] that have been retrieved via a query will be added to the delete queue.
     * Those that have been manually created via a constructor will be ignored and result in a
     * failed operation.
     */
    fun data(vararg data: CommonDataEntity): DataDelete

    /**
     * See [DataDelete.data].
     */
    fun data(data: Collection<CommonDataEntity>): DataDelete

    /**
     * See [DataDelete.data].
     */
    fun data(data: Sequence<CommonDataEntity>): DataDelete

    /**
     * Deletes the [CommonDataEntity]s in the queue (added via [data]) and returns the [Result].
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
     * Deletes the [CommonDataEntity]s in the queue (added via [data]) in one transaction. Either ALL
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
        fun isSuccessful(data: CommonDataEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun DataDelete(context: Context): DataDelete = DataDeleteImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class DataDeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val dataIds: MutableSet<Long> = mutableSetOf()
) : DataDelete {

    override fun toString(): String =
        """
            DataDelete {
                dataIds: $dataIds
            }
        """.trimIndent()

    override fun data(vararg data: CommonDataEntity) = data(data.asSequence())

    override fun data(data: Collection<CommonDataEntity>) = data(data.asSequence())

    override fun data(data: Sequence<CommonDataEntity>): DataDelete = apply {
        dataIds.addAll(data.map { it.id ?: INVALID_ID })
    }

    override fun commit(): DataDelete.Result {
        if (dataIds.isEmpty() || !permissions.canUpdateDelete()) {
            return DataDeleteFailed
        }

        val dataIdsResultMap = mutableMapOf<Long, Boolean>()
        for (dataId in dataIds) {
            dataIdsResultMap[dataId] = if (dataId == INVALID_ID) {
                false
            } else {
                contentResolver.deleteDataWithId(dataId)
            }
        }

        return DataDeleteResult(dataIdsResultMap)
    }

    override fun commitInOneTransaction(): Boolean = permissions.canUpdateDelete()
            && dataIds.isNotEmpty()
            && !dataIds.contains(INVALID_ID)
            && contentResolver.deleteDataRowsWithIds(dataIds)

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
    }
}

private fun ContentResolver.deleteDataWithId(dataId: Long): Boolean = applyBatch(
    newDelete(Table.Data)
        .withSelection(Fields.DataId equalTo dataId)
        .build()
) != null

private fun ContentResolver.deleteDataRowsWithIds(dataIds: Collection<Long>): Boolean = applyBatch(
    newDelete(Table.Data)
        .withSelection(Fields.DataId `in` dataIds)
        .build()
) != null

private class DataDeleteResult(
    private val dataIdsResultMap: Map<Long, Boolean>
) : DataDelete.Result {

    override val isSuccessful: Boolean by unsafeLazy {
        dataIdsResultMap.isNotEmpty() && dataIdsResultMap.all { it.value }
    }

    override fun isSuccessful(data: CommonDataEntity): Boolean =
        data.id?.let { dataId ->
            dataIdsResultMap.getOrElse(dataId) { false }
        } ?: false
}

private object DataDeleteFailed : DataDelete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(data: CommonDataEntity): Boolean = false
}