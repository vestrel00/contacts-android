package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.EmailEntity
import contacts.core.entities.MutableEmail
import contacts.ui.R
import contacts.ui.entities.EmailTypeFactory
import contacts.ui.entities.MutableEmailFactory

/**
 * A [DataEntityWithTypeListView] for [MutableEmail]s.
 */
class EmailsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeListView<EmailEntity.Type, MutableEmail>(
    context, attributeSet, defStyleAttr,
    dataFactory = MutableEmailFactory,
    dataViewFactory = EmailViewFactory,
    defaultUnderlyingDataTypes = EmailEntity.Type.values().filter { !it.isCustomType }
)

private object EmailViewFactory :
    DataEntityWithTypeView.Factory<EmailEntity.Type, MutableEmail> {
    override fun create(context: Context): DataEntityWithTypeView<EmailEntity.Type, MutableEmail> =
        DataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            dataFieldHintResId = R.string.contacts_ui_email_hint,
            dataTypeFactory = EmailTypeFactory
        )
}