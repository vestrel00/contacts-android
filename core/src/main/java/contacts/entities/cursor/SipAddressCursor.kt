package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.SipAddressField

/**
 * Retrieves [Fields.SipAddress] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class SipAddressCursor(cursor: Cursor) : AbstractDataCursor<SipAddressField>(cursor) {

    val sipAddress: String?
        get() = getString(Fields.SipAddress.SipAddress)
}
