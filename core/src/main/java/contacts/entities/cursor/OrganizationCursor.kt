package contacts.entities.cursor

import android.database.Cursor
import contacts.Fields
import contacts.OrganizationField

/**
 * Retrieves [Fields.Organization] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class OrganizationCursor(cursor: Cursor) : AbstractDataCursor<OrganizationField>(cursor) {

    val company: String?
        get() = getString(Fields.Organization.Company)

    val title: String?
        get() = getString(Fields.Organization.Title)

    val department: String?
        get() = getString(Fields.Organization.Department)

    val jobDescription: String?
        get() = getString(Fields.Organization.JobDescription)

    val officeLocation: String?
        get() = getString(Fields.Organization.OfficeLocation)

    val symbol: String?
        get() = getString(Fields.Organization.Symbol)

    val phoneticName: String?
        get() = getString(Fields.Organization.PhoneticName)
}
