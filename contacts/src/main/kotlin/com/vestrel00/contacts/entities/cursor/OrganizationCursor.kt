package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields

/**
 * Retrieves [Fields.Organization] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class OrganizationCursor(cursor: Cursor) : DataCursor(cursor) {

    val company: String?
        get() = cursor.getString(Fields.Organization.Company)

    val title: String?
        get() = cursor.getString(Fields.Organization.Title)

    val department: String?
        get() = cursor.getString(Fields.Organization.Department)

    val jobDescription: String?
        get() = cursor.getString(Fields.Organization.JobDescription)

    val officeLocation: String?
        get() = cursor.getString(Fields.Organization.OfficeLocation)

    val symbol: String?
        get() = cursor.getString(Fields.Organization.Symbol)

    val phoneticName: String?
        get() = cursor.getString(Fields.Organization.PhoneticName)
}
