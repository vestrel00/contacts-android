package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.MutableWebsite
import contacts.ui.R
import contacts.ui.entities.WebsiteFactory

/**
 * A [DataEntityListView] for [MutableWebsite]s.
 */
class WebsitesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityListView<MutableWebsite, DataEntityView<MutableWebsite>>(
    context, attributeSet, defStyleAttr,
    dataFactory = WebsiteFactory,
    dataViewFactory = WebsiteViewFactory
)

private object WebsiteViewFactory :
    DataEntityView.Factory<MutableWebsite, DataEntityView<MutableWebsite>> {
    override fun create(context: Context): DataEntityView<MutableWebsite> =
        DataEntityView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_URI,
            dataFieldHintResId = R.string.contacts_ui_website_hint,
        )
}