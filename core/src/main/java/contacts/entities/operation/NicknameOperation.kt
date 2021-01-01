package contacts.entities.operation

import contacts.CommonDataField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableNickname

internal class NicknameOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableNickname>(isProfile) {

    override val mimeType = MimeType.Nickname

    override fun setData(
        data: MutableNickname, setValue: (field: CommonDataField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Nickname.Name, data.name)
    }
}