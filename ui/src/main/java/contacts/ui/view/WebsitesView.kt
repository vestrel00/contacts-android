package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.WebsiteEntity
import contacts.ui.R
import contacts.ui.entities.NewWebsiteFactory

/**
 * A [DataEntityListView] for [WebsiteEntity]s.
 */
class WebsitesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityListView<WebsiteEntity, DataEntityView<WebsiteEntity>>(
    context, attributeSet, defStyleAttr,
    dataFactory = NewWebsiteFactory,
    dataViewFactory = WebsiteViewFactory
)

private object WebsiteViewFactory :
    DataEntityView.Factory<WebsiteEntity, DataEntityView<WebsiteEntity>> {
    override fun create(context: Context): DataEntityView<WebsiteEntity> =
        DataEntityView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_URI,
            dataFieldHintResId = R.string.contacts_ui_website_hint,
        )
}