package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.MutablePhone
import contacts.core.entities.Phone
import contacts.ui.R
import contacts.ui.entities.MutablePhoneFactory
import contacts.ui.entities.PhoneTypeFactory

/**
 * A [DataEntityWithTypeListView] for [MutablePhone]s.
 */
class PhonesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeListView<Phone.Type, MutablePhone>(
    context, attributeSet, defStyleAttr,
    dataFactory = MutablePhoneFactory,
    dataViewFactory = PhoneViewFactory,
    defaultUnderlyingDataTypes = listOf(
        // The other non-custom types are excluded in the native Contacts app so we'll do the same.
        Phone.Type.MOBILE, Phone.Type.HOME, Phone.Type.WORK, Phone.Type.MAIN, Phone.Type.OTHER
    )
)

private object PhoneViewFactory :
    DataEntityWithTypeView.Factory<Phone.Type, MutablePhone> {
    override fun create(context: Context): DataEntityWithTypeView<Phone.Type, MutablePhone> =
        DataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_PHONE,
            dataFieldHintResId = R.string.contacts_ui_phone_number_hint,
            dataTypeFactory = PhoneTypeFactory
        )
}