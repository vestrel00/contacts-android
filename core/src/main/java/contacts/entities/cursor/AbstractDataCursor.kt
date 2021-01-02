package contacts.entities.cursor

import android.database.Cursor
import contacts.AbstractDataField

/**
 * An abstract cursor for data fields of type [T].
 */
abstract class AbstractDataCursor<T : AbstractDataField>(cursor: Cursor) :
    AbstractCursor<T>(cursor), DataIdCursor {

    // Cannot be in the constructor as this is internal, which is why I couldn't use
    // DataIdCursor by dataCursor at the class declaration level.
    private val dataCursor: DataCursor = DataCursor(cursor)

    override val dataId: Long?
        get() = dataCursor.dataId

    override val rawContactId: Long?
        get() = dataCursor.rawContactId

    override val contactId: Long?
        get() = dataCursor.contactId

    val isPrimary: Boolean
        get() = dataCursor.isPrimary

    val isSuperPrimary: Boolean
        get() = dataCursor.isSuperPrimary
}