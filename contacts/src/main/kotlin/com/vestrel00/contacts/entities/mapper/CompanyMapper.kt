package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableCompany
import com.vestrel00.contacts.entities.cursor.CompanyCursor

internal class CompanyMapper(private val companyCursor: CompanyCursor) {

    val company: MutableCompany
        get() = MutableCompany(
            id = companyCursor.id,
            rawContactId = companyCursor.rawContactId,
            contactId = companyCursor.contactId,

            isPrimary = companyCursor.isPrimary,
            isSuperPrimary = companyCursor.isSuperPrimary,

            company = companyCursor.company,
            title = companyCursor.title,
            department = companyCursor.department,
            jobDescription = companyCursor.jobDescription,
            officeLocation = companyCursor.officeLocation,

            symbol = companyCursor.symbol,
            phoneticName = companyCursor.phoneticName
        )
}
