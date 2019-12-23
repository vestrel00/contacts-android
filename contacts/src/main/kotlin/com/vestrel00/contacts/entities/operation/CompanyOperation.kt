package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.AbstractField
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableCompany

internal class CompanyOperation : AbstractDataOperation<MutableCompany>() {

    override val mimeType = MimeType.COMPANY

    override fun setData(
        data: MutableCompany, setValue: (field: AbstractField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Company.Company, data.company)
        setValue(Fields.Company.Title, data.title)
        setValue(Fields.Company.Department, data.department)
        setValue(Fields.Company.JobDescription, data.jobDescription)
        setValue(Fields.Company.OfficeLocation, data.officeLocation)

        setValue(Fields.Company.Symbol, data.symbol)
        setValue(Fields.Company.PhoneticName, data.phoneticName)
    }
}