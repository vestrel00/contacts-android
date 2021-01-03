package contacts.entities.operation

import contacts.Fields
import contacts.WebsiteField
import contacts.entities.MimeType
import contacts.entities.MutableWebsite

internal class WebsiteOperation(isProfile: Boolean) :
    AbstractCommonDataOperation<WebsiteField, MutableWebsite>(isProfile) {

    override val mimeType = MimeType.Website

    override fun setData(
        data: MutableWebsite, setValue: (field: WebsiteField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Website.Url, data.url)
    }
}