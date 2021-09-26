package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.entities.Email
import contacts.entities.MutableEmail
import contacts.ui.R
import contacts.ui.entities.EmailFactory
import contacts.ui.entities.EmailTypeFactory

/**
 * A [CommonDataEntityWithTypeListView] for [MutableEmail]s.
 */
class EmailsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityWithTypeListView<Email.Type, MutableEmail>(
    context, attributeSet, defStyleAttr,
    dataFactory = EmailFactory,
    dataViewFactory = EmailViewFactory,
    defaultUnderlyingDataTypes = listOf(
        Email.Type.HOME, Email.Type.WORK, Email.Type.OTHER
    )
)

private object EmailViewFactory :
    CommonDataEntityWithTypeView.Factory<Email.Type, MutableEmail> {
    override fun create(context: Context): CommonDataEntityWithTypeView<Email.Type, MutableEmail> =
        CommonDataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            dataFieldHintResId = R.string.contacts_ui_email_hint,
            dataTypeFactory = EmailTypeFactory
        )
}