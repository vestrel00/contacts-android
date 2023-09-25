package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.AbstractDataField

/**
 * An abstract cursor for data fields of type [T].
 *
 * ## Include fields
 *
 * Data whose corresponding field is not specified in [includeFields] are guaranteed to be returned
 * as null even if the value in the database in not null. If [includeFields] is null, then the
 * included field checks are disabled. This means that any non-null data will be returned as is
 * (not null). This is a more optimal, recommended way of including all fields.
 */
abstract class AbstractDataCursor<T : AbstractDataField>(cursor: Cursor, includeFields: Set<T>?) :
    AbstractEntityCursor<T>(cursor, includeFields), DataIdCursor {

    // Cannot be in the constructor as DataCursor is internal, which is why I couldn't use
    // DataIdCursor by dataCursor at the class declaration level and instead resort to delegation
    // at the property level.
    private val dataCursor: DataCursor = DataCursor(cursor, includeFields)

    override val dataId: Long by dataCursor::dataId

    override val rawContactId: Long by dataCursor::rawContactId

    override val contactId: Long by dataCursor::contactId

    val isPrimary: Boolean by dataCursor::isPrimary

    val isSuperPrimary: Boolean by dataCursor::isSuperPrimary
}