package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.entities.Address
import contacts.entities.MutableAddress
import contacts.ui.R
import contacts.ui.entities.AddressFactory
import contacts.ui.entities.AddressTypeFactory

/**
 * A [CommonDataEntityWithTypeListView] for [MutableAddress]es.
 */
class AddressesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityWithTypeListView<Address.Type, MutableAddress>(
    context, attributeSet, defStyleAttr,
    dataFieldInputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
    dataFieldHintResId = R.string.contacts_ui_address_hint,
    dataFactory = AddressFactory,
    dataTypeFactory = AddressTypeFactory,
    defaultUnderlyingDataTypes = DEFAULT_ADDRESS_TYPES
)

private val DEFAULT_ADDRESS_TYPES = listOf(
    Address.Type.HOME, Address.Type.WORK, Address.Type.OTHER
)