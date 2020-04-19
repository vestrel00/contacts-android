package com.vestrel00.contacts.util

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.NoWhere
import com.vestrel00.contacts.Where
import com.vestrel00.contacts.entities.table.Table

// Not inlining these as they just add too many lines of code and are most likely only used in one
// time transactions (not in loops).

internal fun <T> ContentResolver.query(
    table: Table, include: Include, where: Where?, sortOrder: String? = null,
    processCursor: (Cursor) -> T?
): T? = query(table.uri, include, where, sortOrder, processCursor)

internal fun <T> ContentResolver.query(
    contentUri: Uri, include: Include, where: Where?, sortOrder: String? = null,
    processCursor: (Cursor) -> T?
): T? {
    val cursor = query(
        contentUri,
        include.columnNames,
        if (where != null && where != NoWhere) "$where" else null,
        null,
        sortOrder
    )

    var result: T? = null

    if (cursor != null) {
        result = processCursor(cursor)
        cursor.close()
    }

    return result
}