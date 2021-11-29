package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.Address
import contacts.core.entities.MutableAddress
import contacts.ui.R
import contacts.ui.entities.AddressFactory
import contacts.ui.entities.AddressTypeFactory

/**
 * A [DataEntityWithTypeListView] for [MutableAddress]es.
 */
class AddressesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeListView<Address.Type, MutableAddress>(
    context, attributeSet, defStyleAttr,
    dataFactory = AddressFactory,
    dataViewFactory = AddressViewFactory,
    defaultUnderlyingDataTypes = Address.Type.values().filter { !it.isCustomType }
)

private object AddressViewFactory :
    DataEntityWithTypeView.Factory<Address.Type, MutableAddress> {
    override fun create(context: Context): DataEntityWithTypeView<Address.Type, MutableAddress> =
        DataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
            dataFieldHintResId = R.string.contacts_ui_address_hint,
            dataTypeFactory = AddressTypeFactory
        )
}