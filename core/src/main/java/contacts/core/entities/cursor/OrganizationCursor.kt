package contacts.core.entities.cursor

import android.database.Cursor
import contacts.core.Fields
import contacts.core.OrganizationField

/**
 * Retrieves [Fields.Organization] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class OrganizationCursor(cursor: Cursor) : AbstractDataCursor<OrganizationField>(cursor) {

    val company: String? by string(Fields.Organization.Company)

    val title: String? by string(Fields.Organization.Title)

    val department: String? by string(Fields.Organization.Department)

    val jobDescription: String? by string(Fields.Organization.JobDescription)

    val officeLocation: String? by string(Fields.Organization.OfficeLocation)

    val symbol: String? by string(Fields.Organization.Symbol)

    val phoneticName: String? by string(Fields.Organization.PhoneticName)
}
