package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.SipAddressField

/**
 * Retrieves [Fields.SipAddress] data from the given [cursor].
 */
internal class SipAddressCursor(cursor: Cursor, includeFields: Set<SipAddressField>?) :
    AbstractDataCursor<SipAddressField>(cursor, includeFields) {

    val sipAddress: String? by string(Fields.SipAddress.SipAddress)
}
