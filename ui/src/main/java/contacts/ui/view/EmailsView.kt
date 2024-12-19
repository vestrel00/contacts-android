package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.EmailEntity
import contacts.ui.R
import contacts.ui.entities.EmailTypeFactory
import contacts.ui.entities.NewEmailFactory

/**
 * A [DataEntityWithTypeAndLabelListView] for [EmailEntity]s.
 */
class EmailsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeAndLabelListView<EmailEntity.Type, EmailEntity>(
    context, attributeSet, defStyleAttr,
    dataFactory = NewEmailFactory,
    dataViewFactory = EmailViewFactory,
    defaultUnderlyingDataTypes = EmailEntity.Type.entries.filter { !it.isCustomType }
)

private object EmailViewFactory :
    DataEntityWithTypeAndLabelView.Factory<EmailEntity.Type, EmailEntity> {
    override fun create(
        context: Context
    ): DataEntityWithTypeAndLabelView<EmailEntity.Type, EmailEntity> =
        DataEntityWithTypeAndLabelView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            dataFieldHintResId = R.string.contacts_ui_email_hint,
            dataTypeFactory = EmailTypeFactory
        )
}