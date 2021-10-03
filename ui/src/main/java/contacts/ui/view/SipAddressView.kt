package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.entities.MutableSipAddress
import contacts.ui.R

/**
 * A [CommonDataEntityView] for a [MutableSipAddress].
 */
class SipAddressView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityView<MutableSipAddress>(
    context, attributeSet, defStyleAttr,
    dataFieldInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
    dataFieldHintResId = R.string.contacts_ui_sip_address_hint,
    dataDeleteButtonIsVisible = false
)