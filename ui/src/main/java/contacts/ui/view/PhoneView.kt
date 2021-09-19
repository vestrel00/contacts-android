package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
import contacts.entities.MutablePhone
import contacts.entities.Phone
import contacts.ui.R
import contacts.ui.entities.PhoneTypeFactory

/**
 * A [RelativeLayout] that displays a [MutablePhone] and handles the modifications to it.
 *
 * Setting the [data] will automatically update the views. Any modifications in the views will also
 * be made to the [data].
 *
 * ## Note
 *
 * This is a very simple view that is not styled or made to look good. Consumers of the library may
 * choose to use this as is or simply as a reference on how to implement this part of native
 * Contacts app.
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 *
 * The community may contribute by styling and adding more features and customizations with these
 * views if desired.
 *
 * ## Developer Notes
 *
 * I usually am a proponent of passive views and don't add any logic to views. However, I will make
 * an exception for this basic view that I don't really encourage consumers to use.
 */
class PhoneView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityWithTypeView<MutablePhone, Phone.Type>(
    context, MutablePhone(), PhoneTypeFactory, attributeSet, defStyleAttr
) {

    // Not using any view binding libraries or plugins just for this.
    override val dataField: EditText
    override val dataTypeField: Spinner
    override val dataDeleteButton: View

    init {
        inflate(context, R.layout.view_mutable_common_data_entity_with_type, this)

        dataField = findViewById(R.id.dataField)
        dataTypeField = findViewById(R.id.dataTypeField)
        dataDeleteButton = findViewById(R.id.dataDeleteButton)

        dataField.setHint(R.string.contacts_ui_phone_number_hint)
        dataField.inputType = InputType.TYPE_CLASS_PHONE

        initViews()
    }

    override val dataValue: String?
        get() = data.number

    override fun setDataValue(value: String?) {
        data.number = value
    }

    override fun setDataTypeAndTypeLabelValue(type: Phone.Type, typeLabel: String?) {
        data.type = type
        data.label = if (type.isCustomType) typeLabel else null
    }
}