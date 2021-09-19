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
    dataFieldInputType = InputType.TYPE_CLASS_PHONE,
    dataFieldHintResId = R.string.contacts_ui_phone_number_hint,
    dataFactory = PhoneFactory,
    dataTypeFactory = PhoneTypeFactory,
    defaultUnderlyingDataTypes = DEFAULT_PHONE_TYPES
)

private val DEFAULT_PHONE_TYPES = listOf(
    Phone.Type.MOBILE, Phone.Type.HOME, Phone.Type.WORK, Phone.Type.MAIN, Phone.Type.OTHER
)