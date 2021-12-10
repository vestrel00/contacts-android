package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.*

/**
 * A wrapper around a [Cursor] using fields of type [T]. This provides type restrictions with
 * specific fields and convenience functions.
 *
 * Used mainly for cursor manipulations and used for factory extensions.
 */
internal class CursorHolder<T : Field>(val cursor: Cursor, val includeFields: Set<T>) {

    fun moveToNext(): Boolean = cursor.moveToNext()

    fun <R> getNextOrNull(next: () -> R?): R? = if (moveToNext()) next() else null

    fun resetPosition() {
        cursor.moveToPosition(-1)
    }
}

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Field> Cursor.toEntityCursor(includeFields: Set<T>): CursorHolder<T> =
    when (T::class) {
        GroupsField::class -> CursorHolder(this, includeFields as Set<GroupsField>)
        ContactsField::class -> CursorHolder(this, includeFields as Set<ContactsField>)
        RawContactsField::class -> CursorHolder(this, includeFields as Set<RawContactsField>)
        AbstractDataField::class -> CursorHolder(this, includeFields as Set<AbstractDataField>)
        else -> throw ContactsException(
            "No entity cursor for ${T::class.java.simpleName}"
        )
    } as CursorHolder<T>