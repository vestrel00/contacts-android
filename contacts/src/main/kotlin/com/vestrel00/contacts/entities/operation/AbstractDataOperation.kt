package com.vestrel00.contacts.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.*
import android.content.ContentResolver
import android.database.Cursor
import com.vestrel00.contacts.AbstractField
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Where
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.getLong
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo

private val TABLE_URI = Table.DATA.uri

internal abstract class AbstractDataOperation<T : DataEntity> {

    abstract val mimeType: MimeType

    protected fun selection(rawContactId: Long): Where =
        (Fields.MimeType equalTo mimeType.value) and (Fields.RawContactId equalTo rawContactId)

    abstract fun setData(data: T, setValue: (field: AbstractField, value: Any?) -> Unit)

    fun insert(it: T): ContentProviderOperation? {
        if (it.isBlank()) {
            return null
        }

        val operation = newInsert(TABLE_URI)

        setData(it) { field, dataValue ->
            if (dataValue.isNotNullOrBlank()) {
                // Do not insert null or blank values, same as the native Android Contacts app.
                operation.withValue(field, dataValue)
            }
        }

        // Sets the raw contact id column of this Data table row to the first result of the
        // batch operation, which is assumed to be a new raw contact.
        // Note that the Contact ID is automatically set by the Contacts provider.
        operation.withValueBackReference(Fields.RawContactId.columnName, 0)

        // Sets the mimetype, which is the type of data (e.g. email) contained in this
        // row's "data1", "data2", ... columns
        operation.withValue(Fields.MimeType, mimeType.value)

        return operation.build()
    }

    fun insert(these: List<T>): List<ContentProviderOperation> =
        mutableListOf<ContentProviderOperation>().apply {
            for (it in these) {
                insert(it)?.let(::add)
            }
        }

    fun updateInsertOrDelete(
        entities: Collection<T>, rawContactId: Long, contentResolver: ContentResolver
    ): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {
        if (!entitiesAreAllBlank(entities)) {
            // Get all entities with a valid Id, which means they are (or have been) in the DB.
            val validEntitiesMap = entities.toValidEntitiesMap().toMutableMap()

            // Query for all rows in the database.
            val cursor = contentResolver.dataRowIdsFor(rawContactId)

            if (cursor != null) { // I like this better than cursor?.let { cursor ->
                while (cursor.moveToNext()) {
                    val dataRowId = cursor.getLong(Fields.Id)!!

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
                cursor.close()
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

    fun updateInsertOrDelete(
        entity: T?, rawContactId: Long, contentResolver: ContentResolver
    ):
            ContentProviderOperation =
        if (entity != null && !entity.isBlank()) {
            // Entity contains some data. Query for the row.
            val cursor = contentResolver.dataRowIdsFor(rawContactId)

            var dataRowId = INVALID_ID
            if (cursor != null && cursor.moveToNext()) {
                dataRowId = cursor.getLong(Fields.Id)!!
            }
            cursor?.close()

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

    protected fun insertDataRow(entity: T, rawContactId: Long): ContentProviderOperation {
        val operation = newInsert(TABLE_URI)
            // The Contacts Provider automatically sets the value of the CONTACT_ID and prohibits
            // us from setting it manually by failing the insert operation with an exception if
            // the CONTACT_ID is provided. After all, the Contacts Provider has exclusive rights
            // with deciding how the RawContacts are associated with Contacts.
            .withValue(Fields.RawContactId, rawContactId)
            .withValue(Fields.MimeType, mimeType.value)

        setData(entity) { field, dataValue ->
            if (dataValue.isNotNullOrBlank()) {
                // Do not insert null or blank values, same as the native Android Contacts app.
                operation.withValue(field, dataValue)
            }
        }

        return operation.build()
    }

    private fun updateDataRow(entity: T, dataRowId: Long): ContentProviderOperation {
        val operation = newUpdate(TABLE_URI)
            .withSelection("${Fields.Id equalTo dataRowId}", null)

        setData(entity) { field, dataValue ->
            // Intentionally allow to update values to null.
            operation.withValue(field, dataValue)
        }

        return operation.build()
    }

    private fun deleteDataRows(rawContactId: Long): ContentProviderOperation =
        newDelete(TABLE_URI)
            .withSelection("${selection(rawContactId)}", null)
            .build()

    protected fun deleteDataRowWithId(dataRowId: Long): ContentProviderOperation =
        newDelete(TABLE_URI)
            .withSelection("${Fields.Id equalTo dataRowId}", null)
            .build()

    private fun ContentResolver.dataRowIdsFor(rawContactId: Long): Cursor? = query(
        TABLE_URI,
        arrayOf(Fields.Id.columnName),
        "${selection(rawContactId)}",
        null,
        null
    )
}