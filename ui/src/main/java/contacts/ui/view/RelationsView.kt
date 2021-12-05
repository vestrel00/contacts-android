package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.MutableRelation
import contacts.core.entities.RelationEntity
import contacts.ui.R
import contacts.ui.entities.MutableRelationFactory
import contacts.ui.entities.RelationTypeFactory

/**
 * A [DataEntityWithTypeListView] for [MutableRelation]s.
 */
class RelationsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeListView<RelationEntity.Type, MutableRelation>(
    context, attributeSet, defStyleAttr,
    dataFactory = MutableRelationFactory,
    dataViewFactory = RelationViewFactory,
    defaultUnderlyingDataTypes = RelationEntity.Type.values().filter { !it.isCustomType }
)

private object RelationViewFactory :
    DataEntityWithTypeView.Factory<RelationEntity.Type, MutableRelation> {
    override fun create(context: Context): DataEntityWithTypeView<RelationEntity.Type, MutableRelation> =
        DataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_TEXT,
            dataFieldHintResId = R.string.contacts_ui_relation_hint,
            dataTypeFactory = RelationTypeFactory
        )
}