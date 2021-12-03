package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.EmailField
import contacts.core.Fields
import contacts.core.entities.EmailEntity

/**
 * Retrieves [Fields.Email] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class EmailCursor(cursor: Cursor, includeFields: Set<EmailField>) :
    AbstractDataCursor<EmailField>(cursor, includeFields) {

    val type: EmailEntity.Type? by type(
        Fields.Email.Type,
        typeFromValue = EmailEntity.Type::fromValue
    )

    val label: String? by string(Fields.Email.Label)

    val address: String? by string(Fields.Email.Address)
}
