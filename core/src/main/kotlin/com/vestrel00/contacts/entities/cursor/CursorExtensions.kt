package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import android.net.Uri
import com.vestrel00.contacts.AbstractField
import java.util.*

// region GET

/*
 * This should be used for all retrievals of ints, longs, and everything else (including strings).
 *
 * Unlike the other native cursor get functions, this get string function returns null if the cursor
 * value is null. For example, the native [Cursor.getLong] function would return 0 if it has a null
 * value. However, we want the null value instead of 0 so we first use the native [Cursor.getString]
 * and then attempt to cast it to long.
 */
internal fun Cursor.getString(field: AbstractField): String? {
    val index = getColumnIndex(field.columnName)
    return if (index == -1) null else try {
        getString(index)
    } catch (e: Exception) {
        null
    }
}

internal fun Cursor.getInt(field: AbstractField): Int? = getString(field)?.toIntOrNull()

internal fun Cursor.getBoolean(field: AbstractField): Boolean? = getInt(field)?.let { it == 1 }

internal fun Cursor.getLong(field: AbstractField): Long? = getString(field)?.toLongOrNull()

internal fun Cursor.getBlob(field: AbstractField): ByteArray? {
    val index = getColumnIndex(field.columnName)
    return if (index == -1) null else try {
        // Should probably not use getString for getting a byte array.
        // Worst case the byte array would be null or empty
        getBlob(index)
    } catch (e: Exception) {
        null
    }
}

internal fun Cursor.getUri(field: AbstractField): Uri? {
    val uriStr = getString(field)
    return if (uriStr != null) Uri.parse(uriStr) else null
}

internal fun Cursor.getDate(field: AbstractField): Date? {
    val dateMillis = getLong(field)
    return if (dateMillis != null && dateMillis > 0) Date(dateMillis) else null
}

// endregion

internal fun Cursor.resetPosition() {
    moveToPosition(-1)
}

internal fun <T> Cursor.getNextOrNull(next: () -> T?): T? = if (moveToNext()) next() else null