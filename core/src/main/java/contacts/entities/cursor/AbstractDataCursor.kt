package contacts.entities.cursor

import android.database.Cursor
import contacts.AbstractDataField

/**
 * An abstract cursor for data fields of type [T].
 */
abstract class AbstractDataCursor<T : AbstractDataField>(cursor: Cursor) :
    AbstractEntityCursor<T>(cursor), DataIdCursor {

    // Cannot be in the constructor as DataCursor is internal, which is why I couldn't use
    // DataIdCursor by dataCursor at the class declaration level and instead resort to delegation
    // at the property level.
    private val dataCursor: DataCursor = DataCursor(cursor)

    override val dataId: Long? by dataCursor::dataId

    override val rawContactId: Long? by dataCursor::rawContactId

    override val contactId: Long? by dataCursor::contactId

    val isPrimary: Boolean by dataCursor::isPrimary

    val isSuperPrimary: Boolean by dataCursor::isSuperPrimary
}