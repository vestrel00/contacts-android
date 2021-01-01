package contacts.entities.operation

import contacts.CommonDataField
import contacts.Fields
import contacts.entities.MimeType
import contacts.entities.MutableWebsite

internal class WebsiteOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<MutableWebsite>(isProfile) {

    override val mimeType = MimeType.Website

    override fun setData(
        data: MutableWebsite, setValue: (field: CommonDataField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Website.Url, data.url)
    }
}