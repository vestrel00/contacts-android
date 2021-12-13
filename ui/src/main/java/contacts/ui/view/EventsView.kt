package contacts.ui.view

import android.app.DatePickerDialog
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.DatePicker
import contacts.core.entities.EventDate
import contacts.core.entities.EventEntity
import contacts.core.entities.MutableEventEntity
import contacts.ui.R
import contacts.ui.entities.EventTypeFactory
import contacts.ui.entities.NewEventFactory
import java.util.*

/**
 * A [DataEntityWithTypeAndLabelListView] for [EventEntity]s.
 */
class EventsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DataEntityWithTypeAndLabelListView<EventEntity.Type, EventEntity>(
    context, attributeSet, defStyleAttr,
    dataFactory = NewEventFactory,
    dataViewFactory = EventsViewFactory,
    defaultUnderlyingDataTypes = EventEntity.Type.values().filter { !it.isCustomType }
)

private class EventView(context: Context) :
    DataEntityWithTypeAndLabelView<EventEntity.Type, EventEntity>(
        context,
        dataFieldInputType = InputType.TYPE_NULL,
        dataFieldHintResId = R.string.contacts_ui_event_hint,
        dataFieldIsFocusable = false,
        dataTypeFactory = EventTypeFactory
    ), DatePickerDialog.OnDateSetListener {

    override fun onDataFieldClicked() {
        showDatePickerDialog()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val data = data // reassignment required for null-check and casting successfully.
        if (data != null && data is MutableEventEntity) {
            data.date = EventDate.from(year, month, dayOfMonth)
        }
        setDataField()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val monthOfYear = calendar[Calendar.MONTH]
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]

        DatePickerDialog(context, this, year, monthOfYear, dayOfMonth).show()
    }
}

private object EventsViewFactory :
    DataEntityWithTypeAndLabelView.Factory<EventEntity.Type, EventEntity> {
    override fun create(context: Context): DataEntityWithTypeAndLabelView<EventEntity.Type, EventEntity> =
        EventView(context)
}