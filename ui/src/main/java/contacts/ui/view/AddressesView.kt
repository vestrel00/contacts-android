package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.AddressEntity
import contacts.core.entities.MutableAddress
import contacts.ui.R
import contacts.ui.entities.AddressTypeFactory
import contacts.ui.entities.MutableAddressFactory

/**
 * A [DataEntityWithTypeListView] for [MutableAddress]es.
 */
class AddressesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeListView<AddressEntity.Type, MutableAddress>(
    context, attributeSet, defStyleAttr,
    dataFactory = MutableAddressFactory,
    dataViewFactory = AddressViewFactory,
    defaultUnderlyingDataTypes = AddressEntity.Type.values().filter { !it.isCustomType }
)

private object AddressViewFactory :
    DataEntityWithTypeView.Factory<AddressEntity.Type, MutableAddress> {
    override fun create(context: Context): DataEntityWithTypeView<AddressEntity.Type, MutableAddress> =
        DataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
            dataFieldHintResId = R.string.contacts_ui_address_hint,
            dataTypeFactory = AddressTypeFactory
        )
}