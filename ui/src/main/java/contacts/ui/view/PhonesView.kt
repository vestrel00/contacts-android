package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.PhoneEntity
import contacts.ui.R
import contacts.ui.entities.NewPhoneFactory
import contacts.ui.entities.PhoneTypeFactory

/**
 * A [DataEntityWithTypeAndLabelListView] for [PhoneEntity]s.
 */
class PhonesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeAndLabelListView<PhoneEntity.Type, PhoneEntity>(
    context, attributeSet, defStyleAttr,
    dataFactory = NewPhoneFactory,
    dataViewFactory = PhoneViewFactory,
    defaultUnderlyingDataTypes = listOf(
        // The other non-custom types are excluded in the native Contacts app so we'll do the same.
        PhoneEntity.Type.MOBILE,
        PhoneEntity.Type.HOME,
        PhoneEntity.Type.WORK,
        PhoneEntity.Type.MAIN,
        PhoneEntity.Type.OTHER
    )
)

private object PhoneViewFactory :
    DataEntityWithTypeAndLabelView.Factory<PhoneEntity.Type, PhoneEntity> {
    override fun create(
        context: Context
    ): DataEntityWithTypeAndLabelView<PhoneEntity.Type, PhoneEntity> =
        DataEntityWithTypeAndLabelView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_PHONE,
            dataFieldHintResId = R.string.contacts_ui_phone_number_hint,
            dataTypeFactory = PhoneTypeFactory
        )
}