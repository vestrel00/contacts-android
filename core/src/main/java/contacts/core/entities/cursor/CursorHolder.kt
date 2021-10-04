package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.*

/**
 * A wrapper around a [Cursor] using fields of type [T]. This provides type restrictions with
 * specific fields and convenience functions.
 *
 * Used mainly for cursor manipulations and used for factory extensions.
 *
 * ## Developer notes
 *
 * The type [T] is not exactly used in this class itself. Rather, it is used for adding type
 * restrictions when constructing instances at compile time.
 */
internal sealed class CursorHolder<T : Field> {
    abstract val cursor: Cursor

    fun moveToNext(): Boolean = cursor.moveToNext()

    fun <R> getNextOrNull(next: () -> R?): R? = if (moveToNext()) next() else null

    fun resetPosition() {
        cursor.moveToPosition(-1)
    }
}

internal class DataCursorHolder(override val cursor: Cursor) : CursorHolder<AbstractDataField>()
internal class RawContactsCursorHolder(override val cursor: Cursor) :
    CursorHolder<RawContactsField>()

internal class ContactsCursorHolder(override val cursor: Cursor) : CursorHolder<ContactsField>()
internal class GroupsCursorHolder(override val cursor: Cursor) : CursorHolder<GroupsField>()

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Field> Cursor.toEntityCursor(): CursorHolder<T> = when (T::class) {
    AbstractDataField::class -> DataCursorHolder(this)
    RawContactsField::class -> RawContactsCursorHolder(this)
    ContactsField::class -> ContactsCursorHolder(this)
    GroupsField::class -> GroupsCursorHolder(this)
    else -> throw UnsupportedOperationException(
        "No entity cursor for ${T::class.java.simpleName}"
    )
} as CursorHolder<T>