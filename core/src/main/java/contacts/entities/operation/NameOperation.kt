package contacts.entities.operation

import contacts.CommonDataField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableName

internal class NameOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableName>(isProfile) {

    override val mimeType = MimeType.Name

    override fun setData(
        data: MutableName, setValue: (field: CommonDataField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Name.DisplayName, data.displayName)

        setValue(Fields.Name.GivenName, data.givenName)
        setValue(Fields.Name.MiddleName, data.middleName)
        setValue(Fields.Name.FamilyName, data.familyName)

        setValue(Fields.Name.Prefix, data.prefix)
        setValue(Fields.Name.Suffix, data.suffix)

        setValue(Fields.Name.PhoneticGivenName, data.phoneticGivenName)
        setValue(Fields.Name.PhoneticMiddleName, data.phoneticMiddleName)
        setValue(Fields.Name.PhoneticFamilyName, data.phoneticFamilyName)
    }
}