package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.AddressEntity
import contacts.ui.R
import contacts.ui.entities.AddressTypeFactory
import contacts.ui.entities.NewAddressFactory

/**
 * A [DataEntityWithTypeAndLabelListView] for [AddressEntity]s.
 */
class AddressesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeAndLabelListView<AddressEntity.Type, AddressEntity>(
    context, attributeSet, defStyleAttr,
    dataFactory = NewAddressFactory,
    dataViewFactory = AddressViewFactory,
    defaultUnderlyingDataTypes = AddressEntity.Type.values().filter { !it.isCustomType }
)

private object AddressViewFactory :
    DataEntityWithTypeAndLabelView.Factory<AddressEntity.Type, AddressEntity> {
    override fun create(
        context: Context
    ): DataEntityWithTypeAndLabelView<AddressEntity.Type, AddressEntity> =
        DataEntityWithTypeAndLabelView(
            context,
            dataFieldInputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
            dataFieldHintResId = R.string.contacts_ui_address_hint,
            dataTypeFactory = AddressTypeFactory
        )
}