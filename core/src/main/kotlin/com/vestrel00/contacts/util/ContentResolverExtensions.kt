package com.vestrel00.contacts.util

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import android.provider.ContactsContract
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.Where
import com.vestrel00.contacts.entities.table.Table

// region QUERY

// Not inlining these as they just add too many lines of code and are most likely only used in one
// time transactions (not in loops).

internal fun <T> ContentResolver.query(
    table: Table, include: Include, where: Where?,

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
    processCursor: (Cursor) -> T?
): T? = query(table.uri, include, where, sortOrder, suppressDbExceptions, processCursor)

@SuppressLint("Recycle")
internal fun <T> ContentResolver.query(
    contentUri: Uri, include: Include, where: Where?,

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
    processCursor: (Cursor) -> T?
): T? {
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
            throw exception
        }
    }

    var result: T? = null

    if (cursor != null) {
        result = processCursor(cursor)
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