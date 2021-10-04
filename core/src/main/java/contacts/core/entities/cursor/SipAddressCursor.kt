package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.SipAddressField

/**
 * Retrieves [Fields.SipAddress] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class SipAddressCursor(cursor: Cursor) : AbstractDataCursor<SipAddressField>(cursor) {

    val sipAddress: String? by string(Fields.SipAddress.SipAddress)
}
