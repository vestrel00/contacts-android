package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.MutableWebsite
import contacts.ui.R
import contacts.ui.entities.WebsiteFactory

/**
 * A [CommonDataEntityListView] for [MutableWebsite]s.
 */
class WebsitesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityListView<MutableWebsite, CommonDataEntityView<MutableWebsite>>(
    context, attributeSet, defStyleAttr,
    dataFactory = WebsiteFactory,
    dataViewFactory = WebsiteViewFactory
)

private object WebsiteViewFactory :
    CommonDataEntityView.Factory<MutableWebsite, CommonDataEntityView<MutableWebsite>> {
    override fun create(context: Context): CommonDataEntityView<MutableWebsite> =
        CommonDataEntityView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_URI,
            dataFieldHintResId = R.string.contacts_ui_website_hint,
        )
}