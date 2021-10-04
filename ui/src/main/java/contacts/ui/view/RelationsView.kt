package contacts.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import contacts.core.entities.MutableRelation
import contacts.core.entities.Relation
import contacts.ui.R
import contacts.ui.entities.RelationFactory
import contacts.ui.entities.RelationTypeFactory

/**
 * A [CommonDataEntityWithTypeListView] for [MutableRelation]s.
 */
class RelationsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityWithTypeListView<Relation.Type, MutableRelation>(
    context, attributeSet, defStyleAttr,
    dataFactory = RelationFactory,
    dataViewFactory = RelationViewFactory,
    defaultUnderlyingDataTypes = Relation.Type.values().filter { !it.isCustomType }
)

private object RelationViewFactory :
    CommonDataEntityWithTypeView.Factory<Relation.Type, MutableRelation> {
    override fun create(context: Context): CommonDataEntityWithTypeView<Relation.Type, MutableRelation> =
        CommonDataEntityWithTypeView(
            context,
            dataFieldInputType = InputType.TYPE_CLASS_TEXT,
            dataFieldHintResId = R.string.contacts_ui_relation_hint,
            dataTypeFactory = RelationTypeFactory
        )
}