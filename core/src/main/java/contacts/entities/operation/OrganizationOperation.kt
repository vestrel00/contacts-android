package contacts.entities.operation

import contacts.CommonDataField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableOrganization

internal class OrganizationOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableOrganization>(isProfile) {

    override val mimeType = MimeType.Organization

    override fun setData(
        data: MutableOrganization, setValue: (field: CommonDataField, dataValue: Any?) -> Unit
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