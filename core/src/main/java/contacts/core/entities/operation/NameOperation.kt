package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.NameField
import contacts.core.entities.MimeType
import contacts.core.entities.NameEntity

internal class NameOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<NameField>
) : AbstractDataOperation<NameField, NameEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType = MimeType.Name

    override fun setValuesFromData(
        data: NameEntity, setValue: (field: NameField, dataValue: Any?) -> Unit
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