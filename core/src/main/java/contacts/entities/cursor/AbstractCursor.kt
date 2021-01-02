package contacts.entities.cursor

import android.database.Cursor
import android.net.Uri
import contacts.Field
import java.util.*

/**
 * A wrapper around a [Cursor] using fields of type [T]. This provides type restrictions with
 * specific fields and convenience functions.
 *
 * Used for extracting entity data from the [cursor] to construct entity instances.
 */
abstract class AbstractCursor<T : Field>(private val cursor: Cursor) {

    // TODO refactor to use delegate pattern so subclasses can do `by getString`
    protected fun getString(field: T): String? {
        val index = cursor.getColumnIndex(field.columnName)
        return if (index == -1) null else try {
            cursor.getString(index)
        } catch (e: Exception) {
            null
        }
    }

    protected fun getInt(field: T): Int? = getString(field)?.toIntOrNull()

    protected fun getBoolean(field: T): Boolean? = getInt(field)?.let { it == 1 }

    protected fun getLong(field: T): Long? = getString(field)?.toLongOrNull()

    protected fun getBlob(field: T): ByteArray? {
        val index = cursor.getColumnIndex(field.columnName)
        return if (index == -1) null else try {
            // Should probably not use getString for getting a byte array.
            // Worst case the byte array would be null or empty
            cursor.getBlob(index)
        } catch (e: Exception) {
            null
        }
    }

    protected fun getUri(field: T): Uri? {
        val uriStr = getString(field)
        return if (uriStr != null) Uri.parse(uriStr) else null
    }

    protected fun getDate(field: T): Date? {
        val dateMillis = getLong(field)
        return if (dateMillis != null && dateMillis > 0) Date(dateMillis) else null
    }
}