package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.entities.MutablePhone
import contacts.entities.Phone
import contacts.ui.R
import contacts.ui.entities.PhoneFactory
import contacts.ui.entities.PhoneTypeFactory

/**
 * A [CommonDataEntityWithTypeListView] for [MutablePhone]s.
 */
class PhonesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityWithTypeListView<Phone.Type, MutablePhone>(
    context, attributeSet, defStyleAttr,
    dataFactory = PhoneFactory,
    dataViewFactory = PhoneViewFactory,
    defaultUnderlyingDataTypes = listOf(
        Phone.Type.MOBILE, Phone.Type.HOME, Phone.Type.WORK, Phone.Type.MAIN, Phone.Type.OTHER
    )
)

private object PhoneViewFactory :
    CommonDataEntityWithTypeView.Factory<Phone.Type, MutablePhone> {
    override fun create(context: Context): CommonDataEntityWithTypeView<Phone.Type, MutablePhone> =
        CommonDataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_PHONE,
            dataFieldHintResId = R.string.contacts_ui_phone_number_hint,
            dataTypeFactory = PhoneTypeFactory
        )
}