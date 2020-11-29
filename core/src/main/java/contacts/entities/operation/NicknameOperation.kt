package contacts.entities.operation

import contacts.Field
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableNickname

internal class NicknameOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableNickname>(isProfile) {

    override val mimeType = MimeType.NICKNAME

    override fun setData(
        data: MutableNickname, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Nickname.Name, data.name)
    }
}