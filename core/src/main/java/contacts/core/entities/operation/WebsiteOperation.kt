package contacts.core.entities.operation

import contacts.core.Fields
import contacts.core.WebsiteField
import contacts.core.entities.MimeType
import contacts.core.entities.WebsiteEntity

internal class WebsiteOperation(isProfile: Boolean, includeFields: Set<WebsiteField>) :
    AbstractDataOperation<WebsiteField, WebsiteEntity>(isProfile, includeFields) {

    override val mimeType = MimeType.Website

    override fun setValuesFromData(
        data: WebsiteEntity, setValue: (field: WebsiteField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Website.Url, data.url)
    }
}