package contacts.entities.operation

import contacts.Fields
import contacts.NicknameField
import contacts.entities.MimeType
import contacts.entities.MutableNickname

internal class NicknameOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<NicknameField, MutableNickname>(isProfile) {

    override val mimeType = MimeType.Nickname

    override fun setData(
        data: MutableNickname, setValue: (field: NicknameField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Nickname.Name, data.name)
    }
}