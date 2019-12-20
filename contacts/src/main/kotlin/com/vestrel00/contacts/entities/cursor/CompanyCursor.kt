package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.Fields

/**
 * Retrieves [Fields.Company] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class CompanyCursor(cursor: Cursor) : DataCursor(cursor) {

    val company: String?
        get() = cursor.getString(Fields.Company.Company)

    val title: String?
        get() = cursor.getString(Fields.Company.Title)

    val department: String?
        get() = cursor.getString(Fields.Company.Department)

    val jobDescription: String?
        get() = cursor.getString(Fields.Company.JobDescription)

    val officeLocation: String?
        get() = cursor.getString(Fields.Company.OfficeLocation)

    val symbol: String?
        get() = cursor.getString(Fields.Company.Symbol)

    val phoneticName: String?
        get() = cursor.getString(Fields.Company.PhoneticName)
}
