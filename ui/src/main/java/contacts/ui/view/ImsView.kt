package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.Im
import contacts.core.entities.MutableIm
import contacts.ui.R
import contacts.ui.entities.ImFactory
import contacts.ui.entities.ImsTypeFactory

/**
 * A [DataEntityWithTypeListView] for [MutableIm]s.
 */
class ImsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeListView<Im.Protocol, MutableIm>(
    context, attributeSet, defStyleAttr,
    dataFactory = ImFactory,
    dataViewFactory = ImViewFactory,
    defaultUnderlyingDataTypes = Im.Protocol.values().filter { !it.isCustomType }
)

private object ImViewFactory :
    DataEntityWithTypeView.Factory<Im.Protocol, MutableIm> {
    override fun create(context: Context): DataEntityWithTypeView<Im.Protocol, MutableIm> =
        DataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_TEXT,
            dataFieldHintResId = R.string.contacts_ui_im_hint,
            dataTypeFactory = ImsTypeFactory
        )
}