package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.entities.Im
import contacts.entities.MutableIm
import contacts.ui.R
import contacts.ui.entities.ImFactory
import contacts.ui.entities.ImsTypeFactory

/**
 * A [CommonDataEntityWithTypeListView] for [MutableIm]s.
 */
class ImsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityWithTypeListView<Im.Protocol, MutableIm>(
    context, attributeSet, defStyleAttr,
    dataFactory = ImFactory,
    dataViewFactory = ImViewFactory,
    defaultUnderlyingDataTypes = Im.Protocol.values().filter { !it.isCustomType }
)

private object ImViewFactory :
    CommonDataEntityWithTypeView.Factory<Im.Protocol, MutableIm> {
    override fun create(context: Context): CommonDataEntityWithTypeView<Im.Protocol, MutableIm> =
        CommonDataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_TEXT,
            dataFieldHintResId = R.string.contacts_ui_im_hint,
            dataTypeFactory = ImsTypeFactory
        )
}