package contacts.core.util

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.database.SQLException
import android.net.Uri
import android.provider.ContactsContract
import contacts.core.ContactsException
import contacts.core.Field
import contacts.core.Include
import contacts.core.Where
import contacts.core.entities.cursor.CursorHolder
import contacts.core.entities.cursor.toEntityCursor
import contacts.core.entities.table.Table

// region QUERY

internal inline fun <reified T : Field, R> ContentResolver.query(
    table: Table<T>, include: Include<T>, where: Where<T>?,

    /**
     * The sort order, which may also be appended with the LIMIT and OFFSET.
     */
    sortOrder: String? = null,

    /**
     * If true, SQLiteExceptions will be caught instead of crashing the app and returns a null
     * result. This is false by default.
     */
    suppressDbExceptions: Boolean = false,

    /**
     * Function that processes the non-null cursor (if any rows have been matched).
     */
    processCursor: (CursorHolder<T>) -> R?
): R? = query(table.uri, include, where, sortOrder, suppressDbExceptions, processCursor)

@SuppressLint("Recycle")
internal inline fun <reified T : Field, R> ContentResolver.query(
    contentUri: Uri, include: Include<T>, where: Where<T>?,

    /**
     * The sort order, which may also be appended with the LIMIT and OFFSET.
     */
    sortOrder: String? = null,

    /**
     * If true, SQLiteExceptions will be caught instead of crashing the app and returns a null
     * result. This is false by default.
     */
    suppressDbExceptions: Boolean = false,

    /**
     * Function that processes the non-null cursor (if any rows have been matched).
     */
    processCursor: (CursorHolder<T>) -> R?
): R? {
    val cursor = try {
        query(
            contentUri,
            include.columnNames.toTypedArray(),
            where?.toString(),
            null,
            sortOrder
        )
    } catch (exception: SQLException) {
        if (suppressDbExceptions) {
            null
        } else {
            throw ContactsException("Error resolving query", exception)
        }
    }

    var result: R? = null

    if (cursor != null) {
        result = processCursor(cursor.toEntityCursor(include.fields))
        cursor.close()
    }

    return result
}

// endregion

// region APPLY BATCH

internal fun ContentResolver.applyBatch(vararg operations: ContentProviderOperation) =
    applyBatch(arrayListOf(*operations))

internal fun ContentResolver.applyBatch(operations: ArrayList<ContentProviderOperation>):
        Array<ContentProviderResult>? =
    try {
        applyBatch(ContactsContract.AUTHORITY, operations)
    } catch (exception: Exception) {
        null
    }

// endregion