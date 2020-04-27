package com.vestrel00.contacts.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.*
import android.content.ContentResolver
import android.database.Cursor
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.dataCursor
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.query

private val TABLE_URI = Table.DATA.uri

/**
 * Builds [ContentProviderOperation]s for [Table.DATA].
 */
internal abstract class AbstractDataOperation<T : DataEntity> {

    abstract val mimeType: MimeType

    /**
     * There [Where] clause used as the selection for queries, updates, and deletes.
     */
    protected fun selection(rawContactId: Long): Where =
        (Fields.MimeType equalTo mimeType.value) and (Fields.RawContact.Id equalTo rawContactId)

    /**
     * Sets the [data] values into the operation via the provided [setValue] function.
     */
    abstract fun setData(data: T, setValue: (field: AbstractField, value: Any?) -> Unit)

    /**
     * Returns a [ContentProviderOperation] for adding [it]'s properties to the insert operation.
     * This assumes that this will be used in a batch of operations where the first operation is the
     * insertion of a new RawContact.
     *
     * Returns null if [entity] is blank.
     */
    fun insert(entity: T): ContentProviderOperation? {
        if (entity.isBlank()) {
            return null
        }

        val operation = newInsert(TABLE_URI)

        setData(entity) { field, dataValue ->
            if (dataValue.isNotNullOrBlank()) {
                // Do not insert null or blank values, same as the native Android Contacts app.
                operation.withValue(field, dataValue)
            }
        }

        return operation
            // Sets the raw contact id column of this Data table row to the first result of the
            // batch operation, which is assumed to be a new raw contact.
            // Note that the Contact ID is automatically set by the Contacts provider.
            .withValueBackReference(Fields.RawContact.Id.columnName, 0)
            // Sets the mimetype, which is the type of data (e.g. email) contained in this
            // row's "data1", "data2", ... columns
            .withValue(Fields.MimeType, mimeType.value)
            .build()
    }

    /**
     * Returns [ContentProviderOperation]s for adding [these]'s properties to the insert operation.
     * This assumes that this will be used in a batch of operations where the first operation is the
     * insertion of a new RawContact.
     *
     * Blank entities are excluded.
     */
    fun insert(these: List<T>): List<ContentProviderOperation> =
        mutableListOf<ContentProviderOperation>().apply {
            for (it in these) {
                insert(it)?.let(::add)
            }
        }

    /**
     * Provides the [ContentProviderOperation] for updating, inserting, or deleting the data row(s)
     * (represented by the [entities]) of the raw contact with the given [rawContactId].
     *
     * Use this function for data rows of a contact with [mimeType] that may occur more than once.
     * For example, a contact may have more than 1 data row for address, email, phone, etc.
     */
    fun updateInsertOrDelete(
        entities: Collection<T>, rawContactId: Long, contentResolver: ContentResolver
    ): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {
        if (!entitiesAreAllBlank(entities)) {
            // Get all entities with a valid Id, which means they are (or have been) in the DB.
            val validEntitiesMap = entities.toValidEntitiesMap().toMutableMap()

            // Query for all rows in the database.
            contentResolver.dataRowIdsFor(rawContactId) {
                val dataCursor = it.dataCursor()
                while (it.moveToNext()) {
                    val dataRowId = dataCursor.dataId

                    val entity = validEntitiesMap.remove(dataRowId)
                    val operation = if (entity != null && !entity.isBlank()) {
                        // If dataRowId is in entities, update if not blank.
                        updateDataRow(entity, dataRowId)
                    } else {
                        // If dataRowId is not in entities or it is but the entity is blank, delete.
                        // Note that we can gather all data rows to be deleted and delete them using
                        // a single operation but code will get messier.
                        deleteDataRowWithId(dataRowId)
                    }
                    add(operation)
                }
            }

            // Insert all remaining data rows in the valid entities that is not in the cursor.
            // Valid entities have a valid id, which means they are already in the DB. In this case,
            // The entity may have been deleted in another app or another entity belonging to a
            // different contact is included here. Blank entities are not inserted.
            for (entity in validEntitiesMap.values.filter { !it.isBlank() }) {
                add(insertDataRow(entity, rawContactId))
            }

            // Insert all invalid entities.
            // Invalid entities have an invalid id, which means they are newly created entities
            // that are not yet in the DB. Blank entities are not inserted.
            val invalidEntities = entities.toInvalidEntities()
            for (entity in invalidEntities.filter { !it.isBlank() }) {
                add(insertDataRow(entity, rawContactId))
            }
        } else {
            // Entities' empty or contains no data. Delete all data rows of this type.
            add(deleteDataRows(rawContactId))
        }
    }

    /**
     * Provides the [ContentProviderOperation] for updating, inserting, or deleting the data row
     * (represented by the [entity]) of the raw contact with the given [rawContactId].
     *
     * Use this function for data rows of a contact with [mimeType] that may only occur once.
     * For example, a contact may only have 1 data row for company, name, note, etc.
     */
    fun updateInsertOrDelete(
        entity: T?, rawContactId: Long, contentResolver: ContentResolver
    ): ContentProviderOperation =
        if (entity != null && !entity.isBlank()) {
            // Entity contains some data. Query for the (first) row.
            val dataRowId = contentResolver.dataRowIdsFor(rawContactId) {
                if (it.moveToNext()) {
                    it.dataCursor().dataId
                } else {
                    INVALID_ID
                }
            } ?: INVALID_ID

            if (dataRowId == INVALID_ID) {
                // Row does not exist. Insert.
                insertDataRow(entity, rawContactId)
            } else {
                // Row exists. Update.
                updateDataRow(entity, dataRowId)
            }
        } else {
            // Entity contains no data. Delete.
            deleteDataRows(rawContactId)
        }

    /**
     * Provides the [ContentProviderOperation] for inserting the data row (represented by the
     * [entity]) for the given [RawContact] with [rawContactId].
     */
    protected fun insertDataRow(entity: T, rawContactId: Long): ContentProviderOperation {
        val operation = newInsert(TABLE_URI)
            // The Contacts Provider automatically sets the value of the CONTACT_ID and prohibits
            // us from setting it manually by failing the insert operation with an exception if
            // the CONTACT_ID is provided. After all, the Contacts Provider has exclusive rights
            // with deciding how the RawContacts are associated with Contacts.
            .withValue(Fields.RawContact.Id, rawContactId)
            .withValue(Fields.MimeType, mimeType.value)

        setData(entity) { field, dataValue ->
            if (dataValue.isNotNullOrBlank()) {
                // Do not insert null or blank values, same as the native Android Contacts app.
                operation.withValue(field, dataValue)
            }
        }

        return operation.build()
    }

    /**
     * Provides the [ContentProviderOperation] for updating the data row (represented by the
     * [entity]) with the given [dataRowId].
     */
    private fun updateDataRow(entity: T, dataRowId: Long): ContentProviderOperation {
        val operation = newUpdate(TABLE_URI)
            .withSelection("${Fields.Id equalTo dataRowId}", null)

        setData(entity) { field, dataValue ->
            // Intentionally allow to update values to null.
            operation.withValue(field, dataValue)
        }

        return operation.build()
    }

    /**
     * Provides the [ContentProviderOperation] for deleting the data rows of type [T] of the
     * [RawContact] with [rawContactId].
     */
    private fun deleteDataRows(rawContactId: Long): ContentProviderOperation =
        newDelete(TABLE_URI)
            .withSelection("${selection(rawContactId)}", null)
            .build()

    /**
     * Provides the [ContentProviderOperation] for deleting the data row with the given [dataRowId].
     */
    protected fun deleteDataRowWithId(dataRowId: Long): ContentProviderOperation =
        newDelete(TABLE_URI)
            .withSelection("${Fields.Id equalTo dataRowId}", null)
            .build()

    /**
     * Provides the [Cursor] to the data rows of type [T] of the [RawContact] with [rawContactId].
     */
    private fun <T> ContentResolver.dataRowIdsFor(
        rawContactId: Long, processCursor: (Cursor) -> T
    ) = query(
        TABLE_URI,
        Include(Fields.Id),
        selection(rawContactId),
        processCursor = processCursor
    )
}