package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.ImEntity
import contacts.ui.R
import contacts.ui.entities.ImsTypeFactory
import contacts.ui.entities.NewImFactory

/**
 * A [DataEntityWithTypeAndLabelListView] for [ImEntity]s.
 */
class ImsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeAndLabelListView<ImEntity.Protocol, ImEntity>(
    context, attributeSet, defStyleAttr,
    dataFactory = NewImFactory,
    dataViewFactory = ImViewFactory,
    defaultUnderlyingDataTypes = ImEntity.Protocol.values().filter { !it.isCustomType }
)

private object ImViewFactory :
    DataEntityWithTypeAndLabelView.Factory<ImEntity.Protocol, ImEntity> {
    override fun create(
        context: Context
    ): DataEntityWithTypeAndLabelView<ImEntity.Protocol, ImEntity> =
        DataEntityWithTypeAndLabelView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_TEXT,
            dataFieldHintResId = R.string.contacts_ui_im_hint,
            dataTypeFactory = ImsTypeFactory
        )
}