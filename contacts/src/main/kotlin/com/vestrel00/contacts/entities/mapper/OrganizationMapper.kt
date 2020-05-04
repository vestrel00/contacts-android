package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Organization
import com.vestrel00.contacts.entities.cursor.OrganizationCursor

internal class OrganizationMapper(private val organizationCursor: OrganizationCursor) :
    EntityMapper<Organization> {

    override val value: Organization
        get() = Organization(
            id = organizationCursor.dataId,
            rawContactId = organizationCursor.rawContactId,
            contactId = organizationCursor.contactId,

            isPrimary = organizationCursor.isPrimary,
            isSuperPrimary = organizationCursor.isSuperPrimary,

            company = organizationCursor.company,
            title = organizationCursor.title,
            department = organizationCursor.department,
            jobDescription = organizationCursor.jobDescription,
            officeLocation = organizationCursor.officeLocation,

            symbol = organizationCursor.symbol,
            phoneticName = organizationCursor.phoneticName
        )
}
