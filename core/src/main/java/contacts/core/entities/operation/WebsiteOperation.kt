package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.WebsiteField
import contacts.core.entities.MimeType
import contacts.core.entities.MutableWebsite

internal class WebsiteOperation(isProfile: Boolean, includeFields: Set<WebsiteField>) :
    AbstractCommonDataOperation<WebsiteField, MutableWebsite>(isProfile, includeFields) {

    override val mimeType = MimeType.Website

    override fun setData(
        data: MutableWebsite, setValue: (field: WebsiteField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Website.Url, data.url)
    }
}