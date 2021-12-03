package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.OrganizationField
import contacts.core.entities.MimeType
import contacts.core.entities.OrganizationEntity

internal class OrganizationOperation(isProfile: Boolean, includeFields: Set<OrganizationField>) :
    AbstractDataOperation<OrganizationField, OrganizationEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.Organization

    override fun setValuesFromData(
        data: OrganizationEntity, setValue: (field: OrganizationField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Organization.Company, data.company)
        setValue(Fields.Organization.Title, data.title)
        setValue(Fields.Organization.Department, data.department)
        setValue(Fields.Organization.JobDescription, data.jobDescription)
        setValue(Fields.Organization.OfficeLocation, data.officeLocation)

        setValue(Fields.Organization.Symbol, data.symbol)
        setValue(Fields.Organization.PhoneticName, data.phoneticName)
    }
}