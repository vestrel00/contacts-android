package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.AddressEntity
import contacts.core.entities.EmailEntity
import contacts.core.entities.MutableAddressEntity
import contacts.core.entities.MutableEmailEntity
import contacts.ui.R
import contacts.ui.entities.AddressTypeFactory
import contacts.ui.entities.EmailTypeFactory
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

private class AddressView(context: Context) :
    DataEntityWithTypeAndLabelView<AddressEntity.Type, AddressEntity>(
        context,
        dataFieldInputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
        dataFieldHintResId = R.string.contacts_ui_address_hint,
        dataTypeFactory = AddressTypeFactory
    ) {

    override fun onDataSet() {
        super.onDataSet()
        // This view does not expose the individual structured address components
        // (e.g. street, city). Therefore, the structured components should be set to null to allow
        // the Contacts Provider to determine its value from the formattedAddress upon
        // insert/update. Not setting these to null will lead to a bug where sers are not able to
        // clear the address.
        data?.applyIfMutable {
            street = null
            poBox = null
            neighborhood = null
            city = null
            region = null
            postcode = null
            country = null
        }
    }
}

private object AddressViewFactory :
    DataEntityWithTypeAndLabelView.Factory<AddressEntity.Type, AddressEntity> {
    override fun create(
        context: Context
    ): DataEntityWithTypeAndLabelView<AddressEntity.Type, AddressEntity> = AddressView(context)
}

private fun AddressEntity.applyIfMutable(block: MutableAddressEntity.() -> Unit) {
    if (this is MutableAddressEntity) {
        block(this)
    }
}