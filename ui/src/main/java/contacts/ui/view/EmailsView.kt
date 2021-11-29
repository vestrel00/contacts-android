package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.Email
import contacts.core.entities.MutableEmail
import contacts.ui.R
import contacts.ui.entities.EmailFactory
import contacts.ui.entities.EmailTypeFactory

/**
 * A [DataEntityWithTypeListView] for [MutableEmail]s.
 */
class EmailsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeListView<Email.Type, MutableEmail>(
    context, attributeSet, defStyleAttr,
    dataFactory = EmailFactory,
    dataViewFactory = EmailViewFactory,
    defaultUnderlyingDataTypes = Email.Type.values().filter { !it.isCustomType }
)

private object EmailViewFactory :
    DataEntityWithTypeView.Factory<Email.Type, MutableEmail> {
    override fun create(context: Context): DataEntityWithTypeView<Email.Type, MutableEmail> =
        DataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            dataFieldHintResId = R.string.contacts_ui_email_hint,
            dataTypeFactory = EmailTypeFactory
        )
}