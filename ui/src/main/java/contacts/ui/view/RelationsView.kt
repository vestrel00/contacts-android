package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.RelationEntity
import contacts.ui.R
import contacts.ui.entities.NewRelationFactory
import contacts.ui.entities.RelationTypeFactory

/**
 * A [DataEntityWithTypeAndLabelListView] for [RelationEntity]s.
 */
class RelationsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeAndLabelListView<RelationEntity.Type, RelationEntity>(
    context, attributeSet, defStyleAttr,
    dataFactory = NewRelationFactory,
    dataViewFactory = RelationViewFactory,
    defaultUnderlyingDataTypes = RelationEntity.Type.entries.filter { !it.isCustomType }
)

private object RelationViewFactory :
    DataEntityWithTypeAndLabelView.Factory<RelationEntity.Type, RelationEntity> {
    override fun create(
        context: Context
    ): DataEntityWithTypeAndLabelView<RelationEntity.Type, RelationEntity> =
        DataEntityWithTypeAndLabelView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_TEXT,
            dataFieldHintResId = R.string.contacts_ui_relation_hint,
            dataTypeFactory = RelationTypeFactory
        )
}