package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.NicknameField
import contacts.core.entities.MimeType
import contacts.core.entities.MutableNickname

internal class NicknameOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<NicknameField, MutableNickname>(isProfile) {

    override val mimeType = MimeType.Nickname

    override fun setData(
        data: MutableNickname, setValue: (field: NicknameField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Nickname.Name, data.name)
    }
}