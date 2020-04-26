package com.vestrel00.contacts.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.NoWhere
import com.vestrel00.contacts.Where
import com.vestrel00.contacts.entities.table.Table

// Not inlining these as they just add too many lines of code and are most likely only used in one
// time transactions (not in loops).

internal fun <T> ContentResolver.query(
    table: Table, include: Include, where: Where?, sortOrder: String? = null,

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
    contentUri: Uri, include: Include, where: Where?, sortOrder: String? = null,

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
            include.columnNames,
            if (where != null && where != NoWhere) "$where" else null,
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