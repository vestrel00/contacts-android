package contacts.entities.cursor

import android.database.Cursor
import contacts.*

/**
 * A wrapper around a [Cursor] with type [T].
 *
 * ## Developer notes
 *
 * The type [T] is not exactly used in this class itself. Rather, it is used for adding type
 * restrictions when constructing instances at compile time.
 */
internal sealed class EntityCursor<T : Field> {
    abstract val cursor: Cursor

    fun moveToNext(): Boolean = cursor.moveToNext()

    fun <R> getNextOrNull(next: () -> R?): R? = if (moveToNext()) next() else null

    fun resetPosition() {
        cursor.moveToPosition(-1)
    }
}

internal class DataEntityCursor(override val cursor: Cursor) : EntityCursor<AbstractDataField>()
internal class RawContactsEntityCursor(override val cursor: Cursor) :
    EntityCursor<RawContactsField>()

internal class ContactsEntityCursor(override val cursor: Cursor) : EntityCursor<ContactsField>()
internal class GroupsEntityCursor(override val cursor: Cursor) : EntityCursor<GroupsField>()

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Field> Cursor.toEntityCursor(): EntityCursor<T> = when (T::class) {
    AbstractDataField::class -> DataEntityCursor(this)
    RawContactsField::class -> RawContactsEntityCursor(this)
    ContactsField::class -> ContactsEntityCursor(this)
    GroupsField::class -> GroupsEntityCursor(this)
    else -> throw UnsupportedOperationException(
        "No entity cursor for ${T::class.java.simpleName}"
    )
} as EntityCursor<T>