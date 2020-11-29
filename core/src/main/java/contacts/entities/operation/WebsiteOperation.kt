package contacts.entities.operation

import contacts.Field
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableWebsite

internal class WebsiteOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableWebsite>(isProfile) {

    override val mimeType = MimeType.WEBSITE

    override fun setData(
        data: MutableWebsite, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Website.Url, data.url)
    }
}