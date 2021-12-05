package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.ImEntity
import contacts.core.entities.MutableIm
import contacts.ui.R
import contacts.ui.entities.ImsTypeFactory
import contacts.ui.entities.MutableImFactory

/**
 * A [DataEntityWithTypeListView] for [MutableIm]s.
 */
class ImsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeListView<ImEntity.Protocol, MutableIm>(
    context, attributeSet, defStyleAttr,
    dataFactory = MutableImFactory,
    dataViewFactory = ImViewFactory,
    defaultUnderlyingDataTypes = ImEntity.Protocol.values().filter { !it.isCustomType }
)

private object ImViewFactory :
    DataEntityWithTypeView.Factory<ImEntity.Protocol, MutableIm> {
    override fun create(context: Context): DataEntityWithTypeView<ImEntity.Protocol, MutableIm> =
        DataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_TEXT,
            dataFieldHintResId = R.string.contacts_ui_im_hint,
            dataTypeFactory = ImsTypeFactory
        )
}