@file:Suppress("Deprecation")

package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.SipAddressEntity
import contacts.ui.R

/**
 * A [DataEntityView] for an [SipAddressEntity].
 */
class SipAddressView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityView<SipAddressEntity>(
    context, attributeSet, defStyleAttr,
    dataFieldInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
    dataFieldHintResId = R.string.contacts_ui_sip_address_hint,
    dataDeleteButtonIsVisible = false
)